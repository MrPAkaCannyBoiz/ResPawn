using Grpc.Net.Client;
using Microsoft.AspNetCore.Server.Kestrel.Core;
using Microsoft.JSInterop.Infrastructure;
using ReSpawnMarket.SDK;
using ReSpawnMarket.SDK.ServiceInterfaces;
using ReSpawnMarket.SDK.Services;
using System.Text.Json;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();
builder.Services.AddControllers()
    .AddJsonOptions(o =>
    {
        o.JsonSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
        o.JsonSerializerOptions.PropertyNameCaseInsensitive = true;
    });

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// registering grpc services (real implementations)
builder.Services.AddScoped<IRegisterCustomerService, CustomerRegisterGrpcService>();
builder.Services.AddScoped<IGetCustomerService, GetCustomerGrpcService>();
builder.Services.AddScoped<IUploadProductService, UploadProductGrpcService>();

// adding custom extension(static) grpc sdk services
builder.Services.AddGrpcSdk();

// Configure Kestrel to use HTTPS with the specified .pfx certificate
// use .env
DotNetEnv.Env.Load();
var pfxFilePath = Environment.GetEnvironmentVariable("PFX_FILE_PATH") 
    ?? throw new InvalidOperationException("PFX_FILE_PATH environment variable is not set.");
var pfxPassword = Environment.GetEnvironmentVariable("PFX_PASSWORD") 
    ?? throw new InvalidOperationException("PFX_PASSWORD environment variable is not set.");
builder.WebHost.ConfigureKestrel(options =>
{
    options.ListenLocalhost(6760, lo =>
    {
        lo.UseHttps(pfxFilePath, pfxPassword);
    });
});

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
   app.UseSwagger();
   app.UseSwaggerUI();
}
else
{
    app.UseHttpsRedirection();
}



app.UseAuthorization();

app.MapControllers();

app.Run();
