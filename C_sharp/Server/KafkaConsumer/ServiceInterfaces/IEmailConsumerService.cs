namespace KafkaConsumer.ServiceInterfaces;

public interface IEmailConsumerService
{
    Task ExecuteAsync(CancellationToken ct);
}