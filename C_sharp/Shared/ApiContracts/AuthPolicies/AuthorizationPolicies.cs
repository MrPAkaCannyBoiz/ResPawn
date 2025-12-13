using Microsoft.Extensions.DependencyInjection;
using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.AuthPolicies;

public static class AuthorizationPolicies
{
    public static void AddPolicies(IServiceCollection services)
    {
        services.AddAuthorizationCore(options =>
        {
            options.AddPolicy("CanSellProduct", policy =>
                policy.RequireAuthenticatedUser()
                      .RequireClaim("CanSell", "True", "true"));
        });
    }
}
