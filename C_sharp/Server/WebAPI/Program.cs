using ApiContracts.AuthPolicies;
using Grpc.Net.Client;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.DataProtection.KeyManagement;
using Microsoft.AspNetCore.Server.Kestrel.Core;
using Microsoft.IdentityModel.Tokens;
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
builder.Services.AddScoped<IPurchaseService, PurchaseGrpcService>();
builder.Services.AddScoped<IUpdateCustomerService, UpdateCustomerGrpcService>();
builder.Services.AddScoped<IProductInspectionService, ProductInspectionGrpcService>();
builder.Services.AddScoped<IGetProductService, GetProductGrpcService>();
builder.Services.AddScoped<IUpdateCustomerService, UpdateCustomerGrpcService>();
builder.Services.AddScoped<IProductInspectionService, ProductInspectionGrpcService>();
builder.Services.AddScoped<IGetProductService, GetProductGrpcService>();
builder.Services.AddScoped<ICustomerLoginService, CustomerLoginGrpcService>();
builder.Services.AddScoped<IResellerLoginService, ResellerLoginGrpcService>();
builder.Services.AddScoped<ICustomerInspectionService, CustomerInspectionGrpcService>();
builder.Services.AddScoped<IGetAddressService, GetAddressGrpcService>();


// adding custom extension(static) grpc sdk services
builder.Services.AddGrpcSdk();

// Bind jwt settings from appsettings.json
DotNetEnv.Env.Load(); // load .env file (on webapi project root)
builder.Configuration.AddEnvironmentVariables(); // add environment variables to configuration
// this will override appsettings.json values with environment variables if they exist
var section = builder.Configuration; 

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
    .AddJwtBearer(JwtBearerDefaults.AuthenticationScheme, options =>
{
    options.TokenValidationParameters = new()
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer = section["Jwt:Issuer"],
        ValidAudience = section["Jwt:Audience"],
        IssuerSigningKey = new SymmetricSecurityKey(
            System.Text.Encoding.UTF8.GetBytes(section["Jwt:Key"] ?? ""))
    };
    options.Events = new JwtBearerEvents
    {
        OnMessageReceived = context =>
        {
            // Read JWT from cookie if present
            if (context.Request.Cookies.ContainsKey("JwtCookie"))
            {
                context.Token = context.Request.Cookies["JwtCookie"];
            }
            return Task.CompletedTask;
        }
    };
});
builder.Services.AddAuthorization(); // this is needed for jwt auth, remove if jwt is not used/doesn't work
AuthorizationPolicies.AddPolicies(builder.Services); // add custom authorization policies

// Configure Kestrel to use HTTPS with the specified .pfx certificate
// install the certificate to trusted root authorities
// use .env
var pfxFilePath = Environment.GetEnvironmentVariable("PFX_FILE_PATH") 
    ?? throw new InvalidOperationException("PFX_FILE_PATH environment variable is not set.");
var pfxPassword = Environment.GetEnvironmentVariable("PFX_PASSWORD") 
    ?? throw new InvalidOperationException("PFX_PASSWORD environment variable is not set.");

builder.WebHost.ConfigureKestrel(options =>
{
    options.ListenLocalhost(6760, lo =>
    {
        lo.UseHttps(pfxFilePath, pfxPassword); // key pair and its password
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


app.UseAuthentication(); // this is needed for jwt auth, remove if jwt is not used/doesn't work
app.UseAuthorization();

app.MapControllers();

app.Run();
