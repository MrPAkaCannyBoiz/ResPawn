FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# Copy .csproj files for layer-cache restore
COPY C_sharp/Server/WebAPI/WebAPI.csproj ./dotnet_projects/Server/WebAPI/
COPY C_sharp/ReSpawnMarket.SDK/ReSpawnMarket.SDK.csproj ./dotnet_projects/ReSpawnMarket.SDK/
COPY C_sharp/Shared/ApiContracts/ApiContracts.csproj ./dotnet_projects/Shared/ApiContracts/

RUN dotnet restore ./dotnet_projects/Server/WebAPI/

# Copy source
COPY C_sharp/Server/WebAPI ./dotnet_projects/Server/WebAPI/
COPY C_sharp/ReSpawnMarket.SDK ./dotnet_projects/ReSpawnMarket.SDK/
COPY C_sharp/Shared ./dotnet_projects/Shared/

# proto files required by ReSpawnMarket.SDK gRPC client
COPY Shared_proto/ ./Shared_proto/

WORKDIR /src/dotnet_projects/Server/WebAPI
RUN dotnet publish -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish ./
EXPOSE 6760
EXPOSE 6761
ENTRYPOINT ["dotnet", "WebAPI.dll"]
