using FluentEmail.Mailgun;
using MessageConsumer.Consumers;
using MassTransit;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

DotNetEnv.Env.Load();

var builder = Host.CreateApplicationBuilder(args);

builder.Services
    .AddFluentEmail("postmaster@sandboxa847ef46267c4a6b8d8e522f8d0b243c.mailgun.org")
    .AddMailGunSender("sandboxa847ef46267c4a6b8d8e522f8d0b243c.mailgun.org",
        builder.Configuration["Mailgun:ApiKey"]!, MailGunRegion.USA);

builder.Services.AddMassTransit(x =>
{
    x.AddConsumer<WelcomeEmailConsumer>();
    x.AddConsumer<TestEventConsumer>();

    x.UsingRabbitMq((context, cfg) =>
    {
        cfg.Host(builder.Configuration["RabbitMQ:Host"] ?? "localhost", "/", h =>
        {
            h.Username(builder.Configuration["RabbitMQ:Username"] ?? "guest");
            h.Password(builder.Configuration["RabbitMQ:Password"] ?? "guest");
        });

        cfg.ReceiveEndpoint("welcomeEmail", e =>
        {
            e.UseRawJsonDeserializer();
            e.ConfigureConsumer<WelcomeEmailConsumer>(context);
            e.UseMessageRetry(r => r.Exponential(
                retryLimit: 5,
                minInterval: TimeSpan.FromSeconds(2),
                maxInterval: TimeSpan.FromMinutes(5),
                intervalDelta: TimeSpan.FromSeconds(5)));
        });

        cfg.ReceiveEndpoint("test", e =>
        {
            e.UseRawJsonDeserializer();
            e.ConfigureConsumer<TestEventConsumer>(context);
        });
    });
});

var host = builder.Build();
host.Run();
