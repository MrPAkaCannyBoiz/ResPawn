FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

COPY C_sharp/Server/MessageConsumer/MessageConsumer.csproj ./dotnet_projects/Server/MessageConsumer/

RUN dotnet restore ./dotnet_projects/Server/MessageConsumer/

COPY C_sharp/Server/MessageConsumer ./dotnet_projects/Server/MessageConsumer/

WORKDIR /src/dotnet_projects/Server/MessageConsumer
RUN dotnet publish -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/runtime:10.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish ./
ENTRYPOINT ["dotnet", "MessageConsumer.dll"]
