using System.Diagnostics.CodeAnalysis;
using System.Text.Json;
using Confluent.Kafka;
using FluentEmail.Core;
using KafkaConsumer.Events;
using KafkaConsumer.ServiceInterfaces;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Polly;
using Polly.Retry;

namespace KafkaConsumer.Services;

public class EmailConsumerService : IEmailConsumerService
{
    private readonly ILogger<EmailConsumerService> _logger;
    private readonly ConsumerConfig _consumerConfig;
    private readonly ProducerConfig _producerConfig;
    private readonly string _topic = "welcomeEmail";
    private readonly string _dltTopic = "welcomeEmail-dlt";

    private readonly ResiliencePipeline _pipeline;
    private IFluentEmail _fluentEmail;

    public EmailConsumerService(IConfiguration config, ILogger<EmailConsumerService> logger, IFluentEmail fluentEmail)
        : this(config, logger, fluentEmail, null) { }

    internal EmailConsumerService(IConfiguration config, ILogger<EmailConsumerService> logger,
        IFluentEmail fluentEmail, ResiliencePipeline? pipeline)
    {
        _logger = logger;
        _fluentEmail = fluentEmail;
        var bootstrapServer = config["Kafka:BootstrapServers"] ?? "localhost:9092";
        _consumerConfig = new ConsumerConfig
        {
            BootstrapServers = bootstrapServer,
            GroupId = config["Kafka:GroupId:Email"],
            AutoOffsetReset = AutoOffsetReset.Earliest,
            EnableAutoCommit = false
        };
        _producerConfig = new ProducerConfig
        {
            BootstrapServers = bootstrapServer
        };
        _pipeline = pipeline ?? new ResiliencePipelineBuilder()
            .AddRetry(new RetryStrategyOptions
            {
                MaxRetryAttempts = 3,
                Delay = TimeSpan.FromSeconds(2),
                BackoffType = DelayBackoffType.Exponential,
                ShouldHandle = new PredicateBuilder().Handle<Exception>()
            }).Build();
    }

    [ExcludeFromCodeCoverage]
    public async Task ExecuteAsync(CancellationToken ct)
    {
        await Task.Yield();

        using var consumer = new ConsumerBuilder<string, string>(_consumerConfig).Build();
        using var dltProducer = new ProducerBuilder<string, string>(_producerConfig).Build();

        consumer.Subscribe(_topic);
        _logger.LogInformation($"Starting listening to kafka consumer for topic {_topic}");

        while (!ct.IsCancellationRequested)
        {
            try
            {
                var consumeResult = consumer.Consume(ct);
                await HandleEmailMessageAsync(
                    consumeResult.Message.Key,
                    consumeResult.Message.Value,
                    () => consumer.Commit(consumeResult),
                    async (key, value) => await dltProducer.ProduceAsync(
                        _dltTopic,
                        new Message<string, string> { Key = key, Value = value },
                        ct),
                    ct);
            }
            catch (OperationCanceledException)
            {
                break;
            }
            catch (Exception e)
            {
                _logger.LogError($"A fatal Kafka consumer error occurred: {e.Message}");
            }
        }
        consumer.Close();
    }

    internal async Task HandleEmailMessageAsync(
        string messageKey,
        string jsonMessage,
        Action commitOffset,
        Func<string, string, Task> publishToDlt,
        CancellationToken ct)
    {
        _logger.LogInformation($"Received JSON {jsonMessage}");
        try
        {
            WelcomeEmailEvent? emailEvent = null;
            await _pipeline.ExecuteAsync(async token =>
            {
                emailEvent = JsonSerializer.Deserialize<WelcomeEmailEvent>(jsonMessage);
                if (emailEvent != null)
                {
                    await _fluentEmail.To(emailEvent.Email)
                        .Header("Welcome To ResPawn!",
                            $"Hello {emailEvent.FirstName} {emailEvent.LastName}, welcome to ResPawn!")
                        .SendAsync();
                    _logger.LogInformation($"Sending welcome email to {emailEvent.Email}");
                }
                await Task.Delay(1000, token);
            }, ct);
            _logger.LogInformation($"Successfully sent email to {emailEvent!.Email}");
            commitOffset();
        }
        catch (Exception e)
        {
            _logger.LogWarning("Failed to sent email after retries, sending the dead-letter-topic" +
                               "Error: " + e.Message);
            await publishToDlt(messageKey, jsonMessage);
            _logger.LogInformation("Successfully send DLT");
        }
    }
}
