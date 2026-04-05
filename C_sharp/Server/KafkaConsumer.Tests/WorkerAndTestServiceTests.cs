using Xunit;
using KafkaConsumer.ServiceInterfaces;
using KafkaConsumer.Services;
using KafkaConsumer.Workers;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging.Abstractions;
using Moq;

namespace KafkaConsumer.Tests;

public class EmailWorkerTests
{
    [Fact]
    public async Task ExecuteAsync_DelegatesToEmailConsumerService()
    {
        var mockService = new Mock<IEmailConsumerService>();
        using var cts = new CancellationTokenSource();
        cts.Cancel(); // cancel immediately so the worker stops

        mockService
            .Setup(s => s.ExecuteAsync(It.IsAny<CancellationToken>()))
            .Returns(Task.CompletedTask);

        var worker = new EmailWorker(mockService.Object);
        await worker.StartAsync(cts.Token);

        mockService.Verify(s => s.ExecuteAsync(It.IsAny<CancellationToken>()), Times.Once);
    }
}

public class TestWorkerTests
{
    [Fact]
    public async Task ExecuteAsync_DelegatesToTestService()
    {
        var mockService = new Mock<ITestService>();
        using var cts = new CancellationTokenSource();
        cts.Cancel();

        mockService
            .Setup(s => s.ExecuteAsync(It.IsAny<CancellationToken>()))
            .Returns(Task.CompletedTask);

        var worker = new TestWorker(mockService.Object);
        await worker.StartAsync(cts.Token);

        mockService.Verify(s => s.ExecuteAsync(It.IsAny<CancellationToken>()), Times.Once);
    }
}

public class TestServiceTests
{
    private readonly IConfiguration _config;

    public TestServiceTests()
    {
        _config = new ConfigurationBuilder()
            .AddInMemoryCollection(new Dictionary<string, string?>
            {
                ["Kafka:BootstrapServers"] = "localhost:9092",
                ["Kafka:GroupId:Test"] = "test-group"
            })
            .Build();
    }

    [Fact]
    public void HandleTestMessage_CommitsOffset()
    {
        var service = new TestService(_config, NullLogger<TestService>.Instance);
        bool committed = false;

        service.HandleTestMessage("hello", () => { committed = true; });

        Assert.True(committed);
    }

    [Fact]
    public void HandleTestMessage_WithAnyMessage_DoesNotThrow()
    {
        var service = new TestService(_config, NullLogger<TestService>.Instance);

        var ex = Record.Exception(() => service.HandleTestMessage("some-message", () => { }));

        Assert.Null(ex);
    }

    [Fact]
    public void Constructor_InitializesSuccessfully()
    {
        var service = new TestService(_config, NullLogger<TestService>.Instance);
        Assert.NotNull(service);
    }
}
