# Stage 1: Build the application
# dotnet 10 sdk image
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# we want to work with solution path dir, so copy the .csprojfile
COPY Server/Entities/Entities.csproj ./dotnet_projects/Server/
COPY Server/RepositoriesContracts/RepositoriesContracts.csproj ./dotnet_projects/Server/
COPY Server/WebAPI/WebAPI.csproj ./dotnet_projects/Server/
COPY Shared/ApiContracts/ApiContracts.csproj ./dotnet_projects/Shared/ 

# restore the dependencies
RUN dotnet restore ./dotnet_projects/Server/
RUN dotnet restore ./dotnet_projects/Shared/

# then copy the rest of the source code
COPY Server ./dotnet_projects/ResPawnShichiya_CS/Server/
COPY Shared ./dotnet_projects/ResPawnShichiya_CS/Shared/

# also the proto files for gRPC client
COPY Shared_proto /app/Shared_proto/

WORKDIR /src/dotnet_projects/ResPawnShichiya_CS/Server
RUN dotnet publish -c Release -o /app/publish
WORKDIR /src/dotnet_projects/ResPawnShichiya_CS/Shared
RUN dotnet publish -c Release -o /app/publish

# Stage 2: Run the application
FROM mcr.microsoft.com/dotnet/aspnet:10.0
WORKDIR /app
COPY --from=build /app/publish/ ./
EXPOSE 7111
ENTRYPOINT ["dotnet", "WebAPI.dll"]
