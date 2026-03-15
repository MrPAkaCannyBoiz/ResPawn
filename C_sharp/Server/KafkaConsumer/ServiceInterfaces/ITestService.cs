namespace KafkaConsumer.ServiceInterfaces;

public interface ITestService
{
    Task ExecuteAsync(CancellationToken ct);
}