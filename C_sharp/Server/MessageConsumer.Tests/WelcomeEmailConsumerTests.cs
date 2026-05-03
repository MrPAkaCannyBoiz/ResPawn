using FluentEmail.Core;
using FluentEmail.Core.Models;
using MassTransit;
using MessageConsumer.Consumers;
using MessageConsumer.Events;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging.Abstractions;
using Moq;
using Xunit;

namespace MessageConsumer.Tests;

public class WelcomeEmailConsumerTests
{
    private readonly Mock<IFluentEmail> _mockFluentEmail;

    public WelcomeEmailConsumerTests()
    {
        _mockFluentEmail = new Mock<IFluentEmail>();
        _mockFluentEmail
            .Setup(f => f.To(It.IsAny<string>(), It.IsAny<string?>()))
            .Returns(_mockFluentEmail.Object);
        _mockFluentEmail
            .Setup(f => f.To(It.IsAny<string>()))
            .Returns(_mockFluentEmail.Object);
        _mockFluentEmail
            .Setup(f => f.Subject(It.IsAny<string>()))
            .Returns(_mockFluentEmail.Object);
        _mockFluentEmail
            .Setup(f => f.Body(It.IsAny<string>(), It.IsAny<bool>()))
            .Returns(_mockFluentEmail.Object);
    }

    [Fact]
    public async Task Consume_SuccessfulSend_LogsSuccess()
    {
        _mockFluentEmail
            .Setup(f => f.SendAsync(It.IsAny<CancellationToken?>()))
            .ReturnsAsync(new SendResponse { MessageId = "1" });

        var scopeFactory = BuildScopeFactory();
        var consumer = new WelcomeEmailConsumer(
            NullLogger<WelcomeEmailConsumer>.Instance, scopeFactory);

        var mockContext = new Mock<ConsumeContext<WelcomeEmailEvent>>();
        mockContext.Setup(c => c.Message)
            .Returns(new WelcomeEmailEvent("test@example.com", "John", "Doe"));

        await consumer.Consume(mockContext.Object);

        _mockFluentEmail.Verify(f => f.To("test@example.com"), Times.Once);
        _mockFluentEmail.Verify(f => f.SendAsync(It.IsAny<CancellationToken?>()), Times.Once);
    }

    [Fact]
    public async Task Consume_FailedSend_ThrowsInvalidOperationException()
    {
        var failedResponse = new SendResponse();
        failedResponse.ErrorMessages.Add("SMTP down");

        _mockFluentEmail
            .Setup(f => f.SendAsync(It.IsAny<CancellationToken?>()))
            .ReturnsAsync(failedResponse);

        var scopeFactory = BuildScopeFactory();
        var consumer = new WelcomeEmailConsumer(
            NullLogger<WelcomeEmailConsumer>.Instance, scopeFactory);

        var mockContext = new Mock<ConsumeContext<WelcomeEmailEvent>>();
        mockContext.Setup(c => c.Message)
            .Returns(new WelcomeEmailEvent("test@example.com", "John", "Doe"));

        await Assert.ThrowsAsync<InvalidOperationException>(() => consumer.Consume(mockContext.Object));
    }

    private IServiceScopeFactory BuildScopeFactory()
    {
        var services = new ServiceCollection();
        services.AddScoped(_ => _mockFluentEmail.Object);
        var provider = services.BuildServiceProvider();
        return provider.GetRequiredService<IServiceScopeFactory>();
    }
}
