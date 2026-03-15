using KafkaConsumer.ServiceInterfaces;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace KafkaConsumer.Workers;

public class EmailWorker : BackgroundService
{
    private readonly IEmailConsumerService _emailConsumerService;

    public EmailWorker(IEmailConsumerService emailConsumerService)
    {
        _emailConsumerService = emailConsumerService;
    }

    protected override async Task ExecuteAsync(CancellationToken ct)
    {
        await _emailConsumerService.ExecuteAsync(ct);
    }
}
