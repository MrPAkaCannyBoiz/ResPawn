using System.Text.Json;
using ReSpawnMarket.SDK;
using ReSpawnMarket.SDK.ServiceInterfaces;
using ReSpawnMarket.SDK.Services;

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
builder.Services.AddScoped<IPurchaseService, PurchaseGrpcService>();

// adding custom extension(static) grpc sdk services
builder.Services.AddGrpcSdk();

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
