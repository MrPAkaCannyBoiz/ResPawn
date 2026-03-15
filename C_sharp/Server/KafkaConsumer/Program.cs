// See https://aka.ms/new-console-template for more information

using KafkaConsumer.ServiceInterfaces;
using KafkaConsumer.Services;
using KafkaConsumer.Workers;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

var builder = Host.CreateApplicationBuilder(args);

builder.Services.AddHostedService<EmailWorker>();
builder.Services.AddHostedService<TestWorker>();
builder.Services.AddSingleton<IEmailConsumerService, EmailConsumerService>();
builder.Services.AddSingleton<ITestService, TestService>();

var host = builder.Build();

host.Run();
