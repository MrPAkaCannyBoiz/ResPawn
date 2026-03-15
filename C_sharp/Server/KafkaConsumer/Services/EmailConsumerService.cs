using KafkaConsumer.ServiceInterfaces;

namespace KafkaConsumer.Services;

public class EmailConsumer : IEmailConsumer
{
    
    
    public Task ExecuteAsync(CancellationToken ct)
    {
        throw new NotImplementedException();
    }
}