using FluentEmail.Core;
using KafkaConsumer.Events;
using MassTransit;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace KafkaConsumer.Consumers;

public class TestEventConsumer : IConsumer<TestEvent>
{
    private readonly ILogger<TestEventConsumer> _logger;
    private readonly IServiceScopeFactory _scopeFactory;
    

    public TestEventConsumer(ILogger<TestEventConsumer> logger, IServiceScopeFactory serviceScopeFactory)
    {
        _logger = logger;
        _scopeFactory = serviceScopeFactory;
    }

    public async Task Consume(ConsumeContext<TestEvent> context)
    {
        _logger.LogInformation("Test event consumed: {Message}", context.Message.Message);
        using var scope = _scopeFactory.CreateScope();
        var fluentEmail = scope.ServiceProvider.GetRequiredService<IFluentEmail>();

        var response = await fluentEmail
            .To("355491@viauc.dk")
            .Subject("Test from ResPawn email is working")
            .Body($"Hello world, welcome to ResPawn!")
            .SendAsync();

        if (response.Successful)
        {
            _logger.LogInformation("Successfully sent test email");
        }
        else
        {
            _logger.LogError("Failed to send test email");
            throw new InvalidOperationException(
                $"Email delivery failed: {string.Join(", ", response.ErrorMessages)}");
        }
    }
}
