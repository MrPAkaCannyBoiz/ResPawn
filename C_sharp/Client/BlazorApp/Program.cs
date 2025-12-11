using ApiContracts.AuthPolicies;
using BlazorApp.Auth;
using BlazorApp.Components;
using BlazorApp.Services;
using BlazorApp.Services.Concrete;
using BlazorApp.Services.Interface;
using Microsoft.AspNetCore.Components.Authorization;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

builder.Services.AddScoped(sp => new HttpClient
{
    BaseAddress = new Uri("https://localhost:6760/")
}
);

builder.Services.AddScoped<IRegisterCustomerService, HttpRegisterCustomerService>();
builder.Services.AddScoped<IUploadProductService, HttpUploadProductService>();
builder.Services.AddScoped<IUpdateCustomerService, HttpUpdateCustomerService>();
builder.Services.AddScoped<IGetProductService, HttpGetProductService>();
builder.Services.AddScoped<IPurchaseService, HttpPurchaseService>();
builder.Services.AddScoped<IProductInspectionService, HttpProductInspectionService>();
builder.Services.AddScoped<IGetCustomerService, HttpGetCustomerService>();
builder.Services.AddScoped<ICustomerInspectionService, HttpCustomerInspectionService>();
//builder.Services.AddScoped<SimpleAuthProvider>();
builder.Services.AddScoped<AuthenticationStateProvider, CustomAuthProvider>();
builder.Services.AddScoped<CustomAuthProvider>();
builder.Services.AddScoped<IAuthService, HttpJwtCustomerAuthLoginService>();
builder.Services.AddScoped<ICartService, CartService>();
builder.Services.AddScoped<IProductInspectionClient, HttpProductInspectionClient>();
builder.Services.AddScoped<IResellerAuthService, HttpJwtResellerAuthService>();


AuthorizationPolicies.AddPolicies(builder.Services); // add custom authorization policies

builder.Services.AddServerSideBlazor()
    .AddCircuitOptions(o => o.DetailedErrors = true);

builder.Services.AddAuthorizationCore(); // add authorization core for blazor wasm
builder.Services.AddAuthentication();

builder.Services.AddHttpService();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
    app.UseStatusCodePagesWithReExecute("/not-found");

}
//app.UseStatusCodePagesWithReExecute("/not-found", createScopeForErrors: true);

app.UseHttpsRedirection();

app.UseAntiforgery();

app.MapStaticAssets();
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.Run();
