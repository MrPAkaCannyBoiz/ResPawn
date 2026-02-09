# Stage 1: Build the application
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /src

COPY Shared_proto /src/Shared_proto/
COPY java_projects/ReSpawnMarket/pom.xml /src/java_projects/ReSpawnMarket/
COPY java_projects/ReSpawnMarket/src /src/java_projects/ReSpawnMarket/src

# Set working directory to the service project
WORKDIR /src/java_projects/ReSpawnMarket
# skipping tests to avoid database connection issues during build time
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /src/java_projects/ReSpawnMarket/target/ReSpawnMarket-0.0.1-SNAPSHOT.jar /app/ReSpawnMarket.jar
# expose ports
# 8080 for the main application
# 6767 for the gRPC server
EXPOSE 8080 6767
ENTRYPOINT ["java", "-jar", "ReSpawnMarket.jar"]