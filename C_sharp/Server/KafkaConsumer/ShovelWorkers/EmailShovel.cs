using System.Diagnostics.CodeAnalysis;
using Confluent.Kafka;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace KafkaConsumer.ShovelWorkers;

public class EmailShovel : BackgroundService
{
    private readonly ILogger<EmailShovel> _logger;
    private readonly ConsumerConfig _consumerConfig;
    private readonly ProducerConfig _producerConfig;

    internal const string MainTopic = "welcomeEmail";
    internal const string DltTopic = "welcomeEmail-dlt";
    internal const string DeadDeadTopic = "welcomeEmail-failed";
    internal const int MaxAttempts = 5;

    public EmailShovel(IConfiguration config, ILogger<EmailShovel> logger)
    {
        _logger = logger;

        var bootstrapServers = config["Kafka:BootstrapServers"] ?? "localhost:9092";
        var groupId = config["Kafka:GroupId:ShovelEmail"];
        _consumerConfig = new ConsumerConfig()
        {
            BootstrapServers = bootstrapServers,
            GroupId = groupId,
            AutoOffsetReset = AutoOffsetReset.Earliest,
            EnableAutoCommit = false
        };
        _producerConfig = new ProducerConfig()
        {
            BootstrapServers = bootstrapServers
        };
    }

    [ExcludeFromCodeCoverage]
    protected override async Task ExecuteAsync(CancellationToken ct)
    {
        await Task.Yield();

        using var consumer = new ConsumerBuilder<string, string>(_consumerConfig).Build();
        using var producer = new ProducerBuilder<string, string>(_producerConfig).Build();

        consumer.Subscribe(DltTopic);
        _logger.LogInformation($"Shovel Worker started. Listening to {DltTopic}...");

        while (!ct.IsCancellationRequested)
        {
            try
            {
                var consumeResult = consumer.Consume(ct);
                await HandleShovelMessageAsync(
                    consumeResult.Message,
                    () => consumer.Commit(consumeResult),
                    async (topic, msg) => await producer.ProduceAsync(topic, msg, ct),
                    ct);
            }
            catch (OperationCanceledException)
            {
                break;
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error in Shovel Worker: {ex.Message}");
            }
        }
    }

    internal async Task HandleShovelMessageAsync(
        Message<string, string> message,
        Action commitOffset,
        Func<string, Message<string, string>, Task> publishToTopic,
        CancellationToken ct)
    {
        int currentRetryCount = 0;
        if (message.Headers != null && message.Headers.TryGetLastBytes("RetryCount", out var retryBytes))
        {
            currentRetryCount = BitConverter.ToInt32(retryBytes);
        }

        if (currentRetryCount >= MaxAttempts)
        {
            _logger.LogWarning($"Message {message.Value} failed {MaxAttempts} times. Moving to permanent failure queue.");
            await publishToTopic(DeadDeadTopic, new Message<string, string> { Key = message.Key, Value = message.Value });
        }
        else
        {
            _logger.LogInformation($"Shoveling message {message.Value} back to main queue. Attempt {currentRetryCount + 1}");

            var headers = message.Headers ?? new Headers();
            headers.Remove("RetryCount");
            headers.Add("RetryCount", BitConverter.GetBytes(currentRetryCount + 1));

            var shoveledMessage = new Message<string, string>
            {
                Key = message.Key,
                Value = message.Value,
                Headers = headers
            };

            await Task.Delay(TimeSpan.FromMinutes(5), ct);
            await publishToTopic(MainTopic, shoveledMessage);
        }

        commitOffset();
    }
}
