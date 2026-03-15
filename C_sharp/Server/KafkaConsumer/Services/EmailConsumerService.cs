using System.Text.Json;
using Confluent.Kafka;
using KafkaConsumer.Events;
using KafkaConsumer.ServiceInterfaces;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace KafkaConsumer.Services;

public class EmailConsumerService : IEmailConsumerService
{
    private readonly ILogger<EmailConsumerService> _logger;
    private readonly ConsumerConfig _config;
    private readonly string _topic = "welcomeEmail";

    public EmailConsumerService(IConfiguration config, ILogger<EmailConsumerService> logger)
    {
        _logger = logger;
        // this _config act as the configuration for the Kafka consumer
        // like Spring Boot application.properties.
        // we can also use appsettings.json to store these configurations and read them in the constructor using IConfiguration
        _config = new ConsumerConfig
        {
            BootstrapServers = config["Kafka:BootstrapServers"] ?? "localhost:9092",
            GroupId = config["Kafka:GroupId:Email"], // A unique name for your .NET consumers
            AutoOffsetReset = AutoOffsetReset.Earliest
        };
    }

    public async Task ExecuteAsync(CancellationToken ct)
    {
        // Add this to yield the background thread back to the runtime host initially
        await Task.Yield();
        
        using var consumer = new ConsumerBuilder<string, string>(_config).Build();
        
        consumer.Subscribe(_topic);
        _logger.LogInformation($"Starting listening to kafka consumer for topic {_topic}");
        
        while (!ct.IsCancellationRequested)
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
            
             // Simulate sending email (replace with actual email sending logic)
            {
                //TODO : add the real email sending logic here, e.g. using SMTP client or an email sending service API.
                _logger.LogInformation($"Simulating sending welcome email to {stringMessage}");
                await Task.Delay(1000); // Simulate time taken to send an email
            }
        }
    }
}