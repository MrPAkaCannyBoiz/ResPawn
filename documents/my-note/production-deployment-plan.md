# ResPawn Production Deployment Plan

This document outlines the step-by-step strategy for migrating the `docker-compose` based local development environment to an Azure cloud production environment.

## 1. Architectural Decisions

### Solving the Local Secrets Problem
**Solution: Azure Key Vault**
Azure Key Vault is the industry standard in Azure for managing both Secrets (database passwords, API keys, JWT signing keys) and Certificates (PFX, PEM, JKS). 
*   **For Secrets:** Both the Java Spring Boot app and C# Web API will dynamically fetch secrets at startup using Azure Managed Identities, meaning no `.env` files containing real passwords will be deployed.
*   **For Certificates:** Instead of mounting a local `./self_signed_certs` volume, real CA-signed certificates can be uploaded to Key Vault. We can also let the Azure hosting platform handle SSL termination automatically.

### Solving the Database Problem
**Solution: Azure Database for PostgreSQL - Flexible Server**
Do not use "Azure SQL Database", as that is Microsoft's proprietary SQL Server engine and would require rewriting Java/C# data access code. Instead, use Azure Database for PostgreSQL - Flexible Server, which provides a fully managed PaaS version of PostgreSQL with 100% compatibility with the existing code.

---

## 2. Execution Plan

### Phase 1: Provision Managed Services (The Cloud Backends)
1. **Database:** Deploy an **Azure Database for PostgreSQL - Flexible Server**. Run `./scripts/init.sql` against it to set up the production schemas.
2. **Message Broker:** Deploy **Azure Event Hubs** (using its Kafka-compatible endpoint on port 9093) to replace the local KRaft container.
3. **Secrets Management:** Deploy an **Azure Key Vault**. Manually add the new PostgreSQL connection string, Event Hubs SASL password, and JWT keys directly in the Azure Portal as Secrets.

### Phase 2: Refactor Code for Key Vault Authentication
1. **C# WebAPI & KafkaConsumer:** Add the `Azure.Extensions.AspNetCore.Configuration.Secrets` and `Azure.Identity` packages. Configure `DefaultAzureCredential` to connect to Key Vault securely without hardcoded credentials.
2. **Java Spring Boot Server:** Add the `azure-keyvault-secrets-spring-boot-starter` dependency to the `/java_projects/ResPawn/pom.xml`. Update `application.properties` to map database properties to the Azure Key Vault secrets.

### Phase 3: Rethink SSL/TLS Certificates for Containers
In production container environments, it is usually better to offload SSL instead of hardcoding self-signed certs into the application layer:
1. Remove the self-signed certificates (`./self_signed_certs`) from the containers.
2. Have the C# WebAPI and Java Server communicate over pure HTTP (or unsecured gRPC) **internally** within a secure Azure cloud virtual network.
3. Use the Azure platform's built-in **Ingress Controller** to handle HTTPS for the outside world (terminating the SSL at the edge using a free Azure Managed Certificate or one pulled from Key Vault). 

### Phase 4: Container Registry & Deployment
1. **Azure Container Registry (ACR):** Create an ACR. Setup GitHub Actions to build the Java, C# WebAPI, C# Kafka Consumer, and Blazor Dockerfiles, then push these images to the registry.
2. **Azure Container Apps (ACA):** Deploy the containers here. ACA is the modern, serverless cloud equivalent of `docker-compose`. 
    *   Enable **System-Assigned Managed Identity** for each Container App instance.
    *   Grant those identities `Key Vault Secrets User` access in Key Vault's IAM (RBAC).
    *   Configure the Java App, WebAPI, Blazor frontend, and Worker to talk to each other using internal Azure Container Apps DNS routing.
