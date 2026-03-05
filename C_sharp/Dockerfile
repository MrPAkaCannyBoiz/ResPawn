# Stage 1: Build the application
# dotnet 10 sdk image
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# we want to work with solution path dir, so copy the .csprojfile
COPY C_sharp/Server/Entities/Entities.csproj ./dotnet_projects/Server/Entities/
COPY C_sharp/Server/RepositoryContracts/RepositoryContracts.csproj ./dotnet_projects/Server/RepositoryContracts/
COPY C_sharp/Server/WebAPI/WebAPI.csproj ./dotnet_projects/Server/WebAPI/
COPY C_sharp/ReSpawnMarket.SDK/ReSpawnMarket.SDK.csproj ./dotnet_projects/ReSpawnMarket.SDK/
COPY C_sharp/Shared/ApiContracts/ApiContracts.csproj ./dotnet_projects/Shared/ApiContracts/

# restore the dependencies
RUN dotnet restore ./dotnet_projects/Server/Entities/
RUN dotnet restore ./dotnet_projects/Server/RepositoryContracts/
RUN dotnet restore ./dotnet_projects/Server/WebAPI/
RUN dotnet restore ./dotnet_projects/Shared/ApiContracts/
RUN dotnet restore ./dotnet_projects/ReSpawnMarket.SDK/

# then copy the rest of the source code
COPY C_sharp/Server ./dotnet_projects/Server/
COPY C_sharp/Shared ./dotnet_projects/Shared/
COPY C_sharp/ReSpawnMarket.SDK ./dotnet_projects/ReSpawnMarket.SDK/

# also the proto files for gRPC client
COPY Shared_proto/ ./Shared_proto/


WORKDIR /src/dotnet_projects/ReSpawnMarket.SDK
RUN dotnet publish -c Release -o /app/publish
WORKDIR /src/dotnet_projects/Shared/ApiContracts
RUN dotnet publish -c Release -o /app/publish
WORKDIR /src/dotnet_projects/Server/WebAPI
RUN dotnet publish -c Release -o /app/publish


# Stage 2: Run the application
FROM mcr.microsoft.com/dotnet/aspnet:10.0
WORKDIR /app
COPY --from=build /app/publish/ ./
EXPOSE 7111
ENTRYPOINT ["dotnet", "WebAPI.dll"]
