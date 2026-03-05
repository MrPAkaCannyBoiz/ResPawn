FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# we want to work with solution path dir, so copy the .csprojfile
COPY C_sharp/Client/BlazorApp/BlazorApp.csproj ./dotnet_projects/Client/Blazor/
COPY 