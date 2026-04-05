using Xunit;
using FluentEmail.Core;
using FluentEmail.Core.Models;
using KafkaConsumer.Services;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging.Abstractions;
using Moq;
using Polly;

namespace KafkaConsumer.Tests;

public class EmailConsumerServiceTests
{
    private readonly Mock<IFluentEmail> _mockFluentEmail;
    private readonly IConfiguration _config;

    public EmailConsumerServiceTests()
    {
        _mockFluentEmail = new Mock<IFluentEmail>();
        // Set up fluent chain: .To() → .Header() → .SendAsync()
        _mockFluentEmail
            .Setup(f => f.To(It.IsAny<string>(), It.IsAny<string?>()))
            .Returns(_mockFluentEmail.Object);
        _mockFluentEmail
            .Setup(f => f.Header(It.IsAny<string>(), It.IsAny<string>()))
            .Returns(_mockFluentEmail.Object);
        // FluentEmail 3.x SendAsync takes only an optional CancellationToken?
        _mockFluentEmail
            .Setup(f => f.SendAsync(It.IsAny<CancellationToken?>()))
            .ReturnsAsync(new SendResponse());

        _config = new ConfigurationBuilder()
            .AddInMemoryCollection(new Dictionary<string, string?>
            {
                ["Kafka:BootstrapServers"] = "localhost:9092",
                ["Kafka:GroupId:Email"] = "test-email-group"
            })
            .Build();
    }

    private EmailConsumerService CreateService()
        => new(_config, NullLogger<EmailConsumerService>.Instance, _mockFluentEmail.Object, ResiliencePipeline.Empty);

    [Fact]
    public async Task HandleEmailMessageAsync_ValidJson_SendsEmailAndCommitsOffset()
    {
        var service = CreateService();
        var json = """{"email":"test@example.com","firstname":"John","lastname":"Doe"}""";
        bool offsetCommitted = false;
        string? dltValue = null;

        await service.HandleEmailMessageAsync(
            "msg-key",
            json,
            () => { offsetCommitted = true; },
            (_, value) => { dltValue = value; return Task.CompletedTask; },
            CancellationToken.None);

        Assert.True(offsetCommitted);
        Assert.Null(dltValue);
        _mockFluentEmail.Verify(f => f.To("test@example.com", null), Times.Once);
    }

    [Fact]
    public async Task HandleEmailMessageAsync_InvalidJson_PublishesToDltWithoutCommit()
    {
        var service = CreateService();
        var json = "not-valid-json";
        bool offsetCommitted = false;
        string? dltKey = null;

        await service.HandleEmailMessageAsync(
            "msg-key",
            json,
            () => { offsetCommitted = true; },
            (key, _) => { dltKey = key; return Task.CompletedTask; },
            CancellationToken.None);

        Assert.False(offsetCommitted);
        Assert.Equal("msg-key", dltKey);
        _mockFluentEmail.Verify(f => f.To(It.IsAny<string>(), It.IsAny<string?>()), Times.Never);
    }

    [Fact]
    public async Task HandleEmailMessageAsync_NullEmailField_CommitsOffset()
    {
        var service = CreateService();
        var json = """{"email":null,"firstname":"John","lastname":"Doe"}""";
        bool offsetCommitted = false;

        await service.HandleEmailMessageAsync(
            "msg-key",
            json,
            () => { offsetCommitted = true; },
            (_, _) => Task.CompletedTask,
            CancellationToken.None);

        Assert.True(offsetCommitted);
    }

    [Fact]
    public async Task HandleEmailMessageAsync_EmailSendFails_PublishesToDlt()
    {
        _mockFluentEmail
            .Setup(f => f.SendAsync(It.IsAny<CancellationToken?>()))
            .ThrowsAsync(new Exception("SMTP unavailable"));

        var service = CreateService();
        var json = """{"email":"test@example.com","firstname":"John","lastname":"Doe"}""";
        string? dltValue = null;

        await service.HandleEmailMessageAsync(
            "key1",
            json,
            () => { },
            (_, value) => { dltValue = value; return Task.CompletedTask; },
            CancellationToken.None);

        Assert.Equal(json, dltValue);
    }

    [Fact]
    public void Constructor_WithValidConfig_InitializesCorrectly()
    {
        var service = new EmailConsumerService(
            _config,
            NullLogger<EmailConsumerService>.Instance,
            _mockFluentEmail.Object);

        Assert.NotNull(service);
    }
}
