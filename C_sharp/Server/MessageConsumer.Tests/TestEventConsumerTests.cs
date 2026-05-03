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

public class TestEventConsumerTests
{
    private readonly Mock<IFluentEmail> _mockFluentEmail;

    public TestEventConsumerTests()
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
    public async Task Consume_SuccessfulSend_Completes()
    {
        _mockFluentEmail
            .Setup(f => f.SendAsync(It.IsAny<CancellationToken?>()))
            .ReturnsAsync(new SendResponse { MessageId = "1" });

        var scopeFactory = BuildScopeFactory();
        var consumer = new TestEventConsumer(
            NullLogger<TestEventConsumer>.Instance, scopeFactory);

        var mockContext = new Mock<ConsumeContext<TestEvent>>();
        mockContext.Setup(c => c.Message).Returns(new TestEvent("hello world"));

        await consumer.Consume(mockContext.Object);

        _mockFluentEmail.Verify(f => f.SendAsync(It.IsAny<CancellationToken?>()), Times.Once);
    }

    [Fact]
    public async Task Consume_FailedSend_ThrowsInvalidOperationException()
    {
        var failedResponse = new SendResponse();
        failedResponse.ErrorMessages.Add("Connection refused");

        _mockFluentEmail
            .Setup(f => f.SendAsync(It.IsAny<CancellationToken?>()))
            .ReturnsAsync(failedResponse);

        var scopeFactory = BuildScopeFactory();
        var consumer = new TestEventConsumer(
            NullLogger<TestEventConsumer>.Instance, scopeFactory);

        var mockContext = new Mock<ConsumeContext<TestEvent>>();
        mockContext.Setup(c => c.Message).Returns(new TestEvent("hello"));

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
