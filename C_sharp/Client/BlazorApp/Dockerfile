FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# we want to work with solution path dir, so copy the .csproj files
COPY C_sharp/Client/BlazorApp/BlazorApp.csproj ./dotnet_projects/Client/BlazorApp/
COPY C_sharp/Shared/ApiContracts/ApiContracts.csproj ./dotnet_projects/Shared/ApiContracts/

# restore the dependencies
RUN dotnet restore ./dotnet_projects/Client/BlazorApp/

# then copy the rest of the source code
COPY C_sharp/Client ./dotnet_projects/Client/
COPY C_sharp/Shared ./dotnet_projects/Shared/

WORKDIR /src/dotnet_projects/Client/BlazorApp
RUN dotnet publish -c Release -o /app/publish

# Stage 2: Run the application
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish ./
EXPOSE 5195
ENV ASPNETCORE_URLS=http://+:5195
ENTRYPOINT ["dotnet", "BlazorApp.dll"]
