using Xunit;
using Confluent.Kafka;
using KafkaConsumer.ShovelWorkers;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging.Abstractions;

namespace KafkaConsumer.Tests;

public class EmailShovelTests
{
    private readonly IConfiguration _config;

    public EmailShovelTests()
    {
        _config = new ConfigurationBuilder()
            .AddInMemoryCollection(new Dictionary<string, string?>
            {
                ["Kafka:BootstrapServers"] = "localhost:9092",
                ["Kafka:GroupId:ShovelEmail"] = "test-shovel-group"
            })
            .Build();
    }

    private EmailShovel CreateShovel() =>
        new(_config, NullLogger<EmailShovel>.Instance);

    private static Message<string, string> BuildMessage(string value, int? retryCount = null)
    {
        var headers = new Headers();
        if (retryCount.HasValue)
            headers.Add("RetryCount", BitConverter.GetBytes(retryCount.Value));

        return new Message<string, string>
        {
            Key = "test-key",
            Value = value,
            Headers = headers
        };
    }

    [Fact]
    public async Task HandleShovelMessageAsync_BelowMaxAttempts_ReshelvesToMainTopic()
    {
        var shovel = CreateShovel();
        var message = BuildMessage("payload", retryCount: 2);
        string? publishedTopic = null;
        Message<string, string>? publishedMessage = null;
        bool committed = false;

        using var cts = new CancellationTokenSource();
        cts.Cancel(); // cancel immediately so Task.Delay(5min) is skipped

        await shovel.HandleShovelMessageAsync(
            message,
            () => { committed = true; },
            (topic, msg) => { publishedTopic = topic; publishedMessage = msg; return Task.CompletedTask; },
            cts.Token);

        Assert.Equal(EmailShovel.MainTopic, publishedTopic);
        Assert.True(committed);
        // Verify retry count was incremented to 3
        publishedMessage!.Headers.TryGetLastBytes("RetryCount", out var bytes);
        Assert.Equal(3, BitConverter.ToInt32(bytes));
    }

    [Fact]
    public async Task HandleShovelMessageAsync_AtMaxAttempts_MovesToDeadQueue()
    {
        var shovel = CreateShovel();
        var message = BuildMessage("payload", retryCount: EmailShovel.MaxAttempts);
        string? publishedTopic = null;
        bool committed = false;

        await shovel.HandleShovelMessageAsync(
            message,
            () => { committed = true; },
            (topic, _) => { publishedTopic = topic; return Task.CompletedTask; },
            CancellationToken.None);

        Assert.Equal(EmailShovel.DeadDeadTopic, publishedTopic);
        Assert.True(committed);
    }

    [Fact]
    public async Task HandleShovelMessageAsync_NoRetryHeader_TreatsAsFirstAttempt()
    {
        var shovel = CreateShovel();
        var message = new Message<string, string> { Key = "k", Value = "v" }; // no headers
        string? publishedTopic = null;

        using var cts = new CancellationTokenSource();
        cts.Cancel();

        await shovel.HandleShovelMessageAsync(
            message,
            () => { },
            (topic, _) => { publishedTopic = topic; return Task.CompletedTask; },
            cts.Token);

        Assert.Equal(EmailShovel.MainTopic, publishedTopic);
    }

    [Fact]
    public async Task HandleShovelMessageAsync_ExceedMaxAttempts_MovesToDeadQueue()
    {
        var shovel = CreateShovel();
        // retryCount = 10 > MaxAttempts (5)
        var message = BuildMessage("payload", retryCount: 10);
        string? publishedTopic = null;

        await shovel.HandleShovelMessageAsync(
            message,
            () => { },
            (topic, _) => { publishedTopic = topic; return Task.CompletedTask; },
            CancellationToken.None);

        Assert.Equal(EmailShovel.DeadDeadTopic, publishedTopic);
    }

    [Fact]
    public void Constructor_InitializesSuccessfully()
    {
        var shovel = CreateShovel();
        Assert.NotNull(shovel);
    }
}
