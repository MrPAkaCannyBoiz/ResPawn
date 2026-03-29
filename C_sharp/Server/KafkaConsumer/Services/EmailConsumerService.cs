using System.Text.Json;
using Confluent.Kafka;
using KafkaConsumer.Events;
using KafkaConsumer.ServiceInterfaces;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Hosting;
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
    public EmailConsumerService(IConfiguration config, ILogger<EmailConsumerService> logger)
    {
        _logger = logger;
        // this _config act as the configuration for the Kafka consumer
        // like Spring Boot application.properties.
        // we can also use appsettings.json to store these configurations and read them in the constructor using IConfiguration
        var bootstrapServer = config["Kafka:BootstrapServers"] ?? "localhost:9092";
        _consumerConfig = new ConsumerConfig
        {
            BootstrapServers = bootstrapServer,
            GroupId = config["Kafka:GroupId:Email"], // A unique name for your .NET consumers
            AutoOffsetReset = AutoOffsetReset.Earliest,
            EnableAutoCommit = false
        };
        _producerConfig = new ProducerConfig
        {
            BootstrapServers = bootstrapServer
        };
        // Configure modern Polly (v8) Retry Pipeline for dlt
        _pipeline = new ResiliencePipelineBuilder()
            .AddRetry(
            new RetryStrategyOptions
            {
                MaxRetryAttempts = 3,
                Delay = TimeSpan.FromSeconds(2), // Wait 2 seconds
                BackoffType = DelayBackoffType.Exponential, // then 2s, 4s, 8s...
                ShouldHandle = new PredicateBuilder().Handle<Exception>()
            }).Build();
    }

    public async Task ExecuteAsync(CancellationToken ct)
    {
        // Add this to yield the background thread back to the runtime host initially
        await Task.Yield();
        
        using var consumer = new ConsumerBuilder<string, string>(_consumerConfig).Build();
        using var dltProducer = new ProducerBuilder<string, string>(_producerConfig).Build();
        
        consumer.Subscribe(_topic);
        _logger.LogInformation($"Starting listening to kafka consumer for topic {_topic}");
        
        while (!ct.IsCancellationRequested)
        {
            try
            {
                // Wait for a message. This is non-blocking for the rest of your app.
                var consumeResult = consumer.Consume(ct);
                // var jsonMessage = consumeResult.Message.Value;
                var stringMessage = consumeResult.Message.Value;
                // _logger.LogInformation($"Received JSON {jsonMessage}");
                _logger.LogInformation($"Received string {stringMessage}");

                // Parse the JSON from Java object to C# object.
                // You can use System.Text.Json or Newtonsoft.Json for this.
                // var emailEvent = JsonSerializer.Deserialize<WelcomeEmailEvent>(jsonMessage);
                try
                {
                    await _pipeline.ExecuteAsync(async token =>
                    {
                        // Simulate sending email (replace with actual email sending logic)
                        //TODO : add the real email sending logic here, e.g. using SMTP client or an email sending service API.
                        _logger.LogInformation($"Simulating sending welcome email to {stringMessage}");
                        await Task.Delay(1000, token); // Simulate time taken to send an email
                    }, ct);
                    _logger.LogInformation($"Successfully sent email to {stringMessage}");
                    // Manually commit the offset. 
                    // We do this whether the email succeeded OR if it was routed to the DLT.
                    // This guarantees the consumer unblocks and moves to the next user.
                    consumer.Commit(consumeResult); 
                }
                catch (Exception e)
                {
                    _logger.LogWarning("Failed to sent email after retries, sending the dead-letter-topic" +
                                       "Error: " + e.Message);

                    var dltMessage = new Message<string, string>
                    {
                        Key = consumeResult.Message.Key,
                        Value = consumeResult.Message.Value
                    };
                    
                    // send to DLT
                    await dltProducer.ProduceAsync(_dltTopic, dltMessage, ct);
                    _logger.LogInformation("Successfully send DLT");
                }
               
            }
            catch (OperationCanceledException)
            {
                break; // Exit gracefully on cancellation
            }
            catch (Exception e)
            {
                _logger.LogError($"A fatal Kafka consumer error occurred: {e.Message}");
            }
        }
        consumer.Close();
    }
}