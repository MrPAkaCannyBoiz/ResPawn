using System.Diagnostics.CodeAnalysis;
using Confluent.Kafka;
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
            BootstrapServers = config["Kafka:BootstrapServers"],
            GroupId = config["Kafka:GroupId:Test"],
            AutoOffsetReset = AutoOffsetReset.Earliest
        };
    }

    [ExcludeFromCodeCoverage]
    public async Task ExecuteAsync(CancellationToken ct)
    {
        await Task.Yield();

        using var consumer = new ConsumerBuilder<string, string>(_config).Build();

        consumer.Subscribe(_topic);
        _logger.LogInformation($"Starting listening to kafka consumer for topic {_topic}");

        while (!ct.IsCancellationRequested)
        {
            var consumeResult = consumer.Consume(ct);
            HandleTestMessage(consumeResult.Message.Value, () => consumer.Commit(consumeResult));
            await Task.Delay(1000);
        }
    }

    internal void HandleTestMessage(string message, Action commitOffset)
    {
        _logger.LogInformation($"Received JSON {message}");
        commitOffset();
        _logger.LogInformation($"Test event is consumed!");
        Console.WriteLine("Test event is consumed!");
    }
}
