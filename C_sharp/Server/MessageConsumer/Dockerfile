FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# KafkaConsumer has no ProjectReferences — only NuGet packages
COPY C_sharp/Server/KafkaConsumer/KafkaConsumer.csproj ./dotnet_projects/Server/KafkaConsumer/

RUN dotnet restore ./dotnet_projects/Server/KafkaConsumer/

COPY C_sharp/Server/KafkaConsumer ./dotnet_projects/Server/KafkaConsumer/

WORKDIR /src/dotnet_projects/Server/KafkaConsumer
RUN dotnet publish -c Release -o /app/publish

# Use runtime (not aspnet) — KafkaConsumer is a generic host with no HTTP server
FROM mcr.microsoft.com/dotnet/runtime:10.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish ./
ENTRYPOINT ["dotnet", "KafkaConsumer.dll"]
