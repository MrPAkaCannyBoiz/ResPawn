namespace KafkaConsumer.ServiceInterfaces;

public interface IEmailConsumer
{
    Task ExecuteAsync(CancellationToken ct);
}