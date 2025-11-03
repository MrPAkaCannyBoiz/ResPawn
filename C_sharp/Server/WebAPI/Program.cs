using FileRepository;
using RepositoryContracts;
using ReSpawnMarket.SDK;
using ReSpawnMarket.SDK.ServiceInterfaces;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddScoped<CustomerInterface, CustomerFileRespository>();
builder.Services.AddScoped<IRegisterCustomerService, CustomerRegisterGrpcService>();

// adding grpc sdk services
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
