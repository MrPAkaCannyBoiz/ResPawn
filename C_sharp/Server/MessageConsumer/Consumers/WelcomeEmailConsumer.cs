using FluentEmail.Core;
using KafkaConsumer.Events;
using MassTransit;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace KafkaConsumer.Consumers;

public class WelcomeEmailConsumer : IConsumer<WelcomeEmailEvent>
{
    private readonly ILogger<WelcomeEmailConsumer> _logger;
    private readonly IServiceScopeFactory _scopeFactory;

    public WelcomeEmailConsumer(ILogger<WelcomeEmailConsumer> logger, IServiceScopeFactory scopeFactory)
    {
        _logger = logger;
        _scopeFactory = scopeFactory;
    }

    public async Task Consume(ConsumeContext<WelcomeEmailEvent> context)
    {
        var emailEvent = context.Message;
        _logger.LogInformation("Received welcome email event for {Email}", emailEvent.Email);

        using var scope = _scopeFactory.CreateScope();
        var fluentEmail = scope.ServiceProvider.GetRequiredService<IFluentEmail>();

        var response = await fluentEmail
            .To(emailEvent.Email)
            .Subject("Welcome To ResPawn!")
            .Body($"Hello {emailEvent.FirstName} {emailEvent.LastName}, welcome to ResPawn!")
            .SendAsync();

        if (response.Successful)
        {
            _logger.LogInformation("Successfully sent welcome email to {Email}", emailEvent.Email);
        }
        else
        {
            _logger.LogError("Failed to send welcome email to {Email}: {Errors}",
                emailEvent.Email, string.Join(", ", response.ErrorMessages));
            throw new InvalidOperationException(
                $"Email delivery failed: {string.Join(", ", response.ErrorMessages)}");
        }
    }
}
