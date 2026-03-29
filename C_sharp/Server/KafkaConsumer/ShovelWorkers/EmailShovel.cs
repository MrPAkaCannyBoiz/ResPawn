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

    private const string MainTopic = "welcomeEmail";
    private const string DltTopic = "welcomeEmail-dlt";
    private const string DeadDeadTopic = "welcomeEmail-failed";
    private const int MaxAttempts = 5;

    public EmailShovel(IConfiguration config ,ILogger<EmailShovel> logger)
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
                var message = consumeResult.Message;
                
            // 1. Check the "Sticky Note" (Kafka Headers) for previous retries
                int currentRetryCount = 0;
                if (message.Headers != null && message.Headers.TryGetLastBytes("RetryCount", out var retryBytes))
                {
                    currentRetryCount = BitConverter.ToInt32(retryBytes);
                }

                // 2. Decide where to shovel the message
                if (currentRetryCount >= MaxAttempts)
                {
                    _logger.LogWarning($"Message {message.Value} failed {MaxAttempts} times. Moving to permanent failure queue.");
                    await producer.ProduceAsync(DeadDeadTopic, new Message<string, string> { Key = message.Key, Value = message.Value }, ct);
                }
                else
                {
                    _logger.LogInformation($"Shoveling message {message.Value} back to main queue. Attempt {currentRetryCount + 1}");
                    
                    // Create a new header list, update the retry count, and attach it
                    var headers = message.Headers ?? new Headers();
                    headers.Remove("RetryCount"); 
                    headers.Add("RetryCount", BitConverter.GetBytes(currentRetryCount + 1));

                    var shoveledMessage = new Message<string, string>
                    {
                        Key = message.Key,
                        Value = message.Value,
                        Headers = headers
                    };

                    // Add a delay here so we don't instantly spam the main queue if the API is still down
                    // (e.g., wait 5 minutes between processing DLT messages)
                    await Task.Delay(TimeSpan.FromMinutes(5), ct);

                    // Send it back to the main topic
                    await producer.ProduceAsync(MainTopic, shoveledMessage, ct);
                }

                // 3. Commit the DLT offset so we don't read this specific failure again
                consumer.Commit(consumeResult);
            }
            catch (OperationCanceledException)
            {
                break; // Graceful shutdown
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error in Shovel Worker: {ex.Message}");
            }
            
        }
    }
}