using System.Data.SqlTypes;
using System.Text.Json;
using Confluent.Kafka;
using KafkaConsumer.Events;
using KafkaConsumer.ServiceInterfaces;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace KafkaConsumer.Services;

public class TestService : ITestService
{
    private readonly ILogger<TestService> _logger;
    private readonly ConsumerConfig _config;
    private readonly string _topic = "test";

    public TestService(IConfiguration config, ILogger<TestService> logger)
    {
        _logger = logger;
        _config = new ConsumerConfig
        {
            BootstrapServers = "localhost:9092",
            GroupId = "dotnet-test-workers-v2", // A unique name for your .NET consumers
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
            var message = consumeResult.Message.Value;
            
            _logger.LogInformation($"Received JSON {message}");

            consumer.Commit(consumeResult);
            _logger.LogInformation($"Test event is consumed!");
            Console.WriteLine("Test event is consumed!");
            await Task.Delay(1000); // Simulate time taken to send an email   
            
        }
    }
}