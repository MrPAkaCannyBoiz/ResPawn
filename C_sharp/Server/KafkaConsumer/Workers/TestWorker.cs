using KafkaConsumer.ServiceInterfaces;
using Microsoft.Extensions.Hosting;

namespace KafkaConsumer.Workers;

public class TestWorker: BackgroundService
{
    private readonly ITestService _service;

    public TestWorker(ITestService service)
    {
        _service = service;
    }

    protected override async Task ExecuteAsync(CancellationToken ct)
    {
        await _service.ExecuteAsync(ct);
    }
}