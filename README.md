# ResPawn

> [!NOTE]
> **Project Continuation:** This repository is a personal project continuing from a third-semester university project (SEP3). The original was a multi-tier school assignment; this continuation evolves it into **production-grade infrastructure** — adding RabbitMQ, MassTransit, Docker, CI/CD with coverage gates, and enterprise security patterns. Production runs on a Hetzner VPS with `docker-compose`.

---

### Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Security Implemented](#security-implemented)
- [Testing & CI/CD](#testing--cicd)
- [Running Locally](#running-locally)
- [Production Deployment](#production-deployment--hetzner-vps)
- [Previously Planned (Azure — Cancelled)](#previously-planned--azure-paas--kubernetes--cancelled)
- [Features](#features)
- [Analyses & Designs](#analyses--designs)
- [TLS/SSL Certificate Setup Guide](#tlsssl-certificate-setup-guide)

---

## Overview

ResPawn-Shichiya is an online pawn shop platform modeled after typical e-commerce and chat systems, adapted so users can submit items for sale and receive a purchase offer from the shop.

Demo on YouTube: https://youtu.be/FjboqOlDwV8 — Credits to [OliverX04](https://github.com/OliverX04) for the voice-over.

---

## Architecture

The system is decomposed into **five containerized services** that communicate over gRPC and RabbitMQ:

```
[Blazor Frontend]
      |  HTTP (REST)
[.NET WebAPI]  ──── gRPC (TLS) ────  [Spring Boot gRPC Server]
                                              |
                                       RabbitMQ Produce
                                              |
[.NET Message Worker] ◄──────────────── [RabbitMQ]
      |                                        |
    Email                              [PostgreSQL]
```

| Service | Tech | Role |
|---|---|---|
| `grpc-server-springboot-service` | Java 25 / Spring Boot 4 | gRPC server, JPA/Hibernate, RabbitMQ producer (Spring AMQP) |
| `grpc-client-dotnet-services` | .NET 10 / C# | REST API, gRPC client, JWT auth |
| `blazor-frontend` | .NET 10 / Blazor | Web UI |
| `message-worker` | .NET 10 / C# | MassTransit RabbitMQ consumer — sends welcome emails via FluentEmail |
| `rabbitmq` | RabbitMQ | Message broker with fanout exchanges |

**Message flow example (registration):** WebAPI receives REST call → gRPC to Spring Boot → publishes `WelcomeEmailDto` as JSON to RabbitMQ `welcomeEmail` fanout exchange → MassTransit consumer picks up → sends email via SMTP/Mailgun. On failure: MassTransit exponential backoff (5 retries) → message moved to `welcomeEmail_error` queue.

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java / Spring Boot | 4.0.3 | gRPC server, REST endpoints, Spring Security, JPA |
| .NET / C# | 10.0 | WebAPI (gRPC client), Blazor frontend, message worker |
| gRPC + Protocol Buffers | — | High-performance typed inter-service RPC |
| RabbitMQ | latest | Async event-driven messaging with fanout exchanges |
| Spring AMQP | — | RabbitMQ producer on the Java side (Jackson JSON serialization) |
| MassTransit | 8.4 | RabbitMQ consumer transport abstraction on the C# side |
| PostgreSQL | latest | Relational database |
| Spring Data JPA / Hibernate | — | ORM for PostgreSQL |

### Infrastructure & Deployment
| Technology | Purpose |
|---|---|
| Docker / Docker Compose | Local development & production deployment |
| Hetzner VPS | Production hosting (docker-compose with prod override) |
| Kubernetes (minikube) | Local k8s demo/learning only (manifests in `/k8s`) |
| GitHub Actions | CI/CD: automated build, test, coverage check, artifact upload |

### Security
| Technology | Purpose |
|---|---|
| Environment variables (`.env.prod`) | Production secrets management (DB, JWT, RabbitMQ, SMTP) |
| JWT Bearer (`Microsoft.AspNetCore.Authentication.JwtBearer`) | Stateless API authentication |
| TLS/SSL (PKCS12 / `.p12`) | Encrypted gRPC channel between .NET and Spring Boot |
| Spring Security | Java-layer HTTP and gRPC security |
| MassTransit retry + error queues | Resilience: exponential backoff with automatic error queue routing |

---

## Security Implemented

### 1. TLS/SSL — gRPC Transport Encryption
The gRPC channel between the .NET WebAPI (client) and Spring Boot (server) is encrypted end-to-end using **one-way TLS** with a PKCS12 certificate:
- **Spring Boot** loads the keystore via `spring.ssl.bundle.jks.sep3` bound to environment variables.
- **.NET WebAPI** configures Kestrel with the same `.p12` certificate via `PFX_FILE_PATH` / `PFX_PASSWORD`.
- In production: certificates are passed via environment variables and volume mounts on the VPS.

### 2. JWT Bearer Authentication
- All protected API endpoints require a valid JWT token.
- Tokens are validated against issuer, audience, signing key — all loaded from environment variables.
- Token can also be read from a cookie (custom `JwtBearerEvents` on the .NET side).

### 3. Spring Security (Java)
- HTTP Basic auth guards the Spring Boot management layer (`sep3admin` user, password from env).
- `spring-boot-starter-security` applied to REST and gRPC layers.

### 4. Environment-Based Secrets Management
- All sensitive values (DB passwords, JWT keys, RabbitMQ credentials, SMTP passwords) are passed via environment variables using `.env.prod` on the VPS — **never hardcoded in code or committed files**.
- Dev uses a local `.env` with placeholder credentials; production uses `.env.prod` (gitignored).

### 5. RabbitMQ Credentials
- In production, RabbitMQ credentials are passed via environment variables (`RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`) in `.env.prod`.
- Locally, the default `guest`/`guest` credentials are used for the self-hosted RabbitMQ container.

### 6. Resilience — MassTransit Error Queues
- The MassTransit email consumer uses **exponential backoff** (5 retries, 2s–5min intervals).
- Permanently failing messages are automatically routed to `welcomeEmail_error` queue to prevent data loss without blocking the consumer.

### 7. BCrypt — One-Way Password Hashing
Passwords are **never stored in plain text**. Before persisting to PostgreSQL:
- **Registration:** `BCryptPasswordEncoder.encode()` hashes the password with a random salt (`RegisterCustomerServiceImpl.java`).
- **Login:** `BCrypt.checkpw()` verifies the submitted password against the stored hash — the original password is never recoverable (`CustomerLoginServiceImpl.java`, `ResellerLoginServiceImpl.java`).

### 8. Docker Security Hygiene
- Self-signed cert volumes are mounted **read-only** (`:ro`) in containers.
- Secrets are passed only as container environment variables — never baked into images.

---

## Testing & CI/CD

Three GitHub Actions workflows run on every push and pull request:

| Workflow | Trigger | What it does |
|---|---|---|
| `build-startup.yml` | All branches | Maven clean verify (compile + test + Jacoco coverage check) |
| `spring-boot-test.yml` | `main` branch | Maven verify + Jacoco HTML report upload |
| `dotnet-test.yml` | All branches (C# paths) | `dotnet test` with Coverlet + ReportGenerator |

**Coverage gates:**
- Java (Jacoco): **80% instruction coverage** enforced — build fails below threshold.
- .NET (Coverlet + ReportGenerator): **80% line coverage** enforced — build fails below threshold.
- Coverage HTML reports are uploaded as GitHub Actions artifacts on every run.

---

## Running Locally

### Prerequisites
- Docker Desktop
- `.env` file in the repo root (see [TLS/SSL Setup Guide](#tlsssl-certificate-setup-guide) for variables)

```bash
docker compose up --build
```

| Service | URL |
|---|---|
| Blazor Frontend | http://localhost:5195 |
| .NET WebAPI (HTTP) | http://localhost:6761 |
| .NET WebAPI (HTTPS) | https://localhost:6760 |
| Spring Boot REST | https://localhost:8080 |
| gRPC Server | https://localhost:6767 |
| RabbitMQ broker | localhost:5672 |
| RabbitMQ Management UI | http://localhost:15672 |
| PostgreSQL | localhost:5432 |

---

## Production Deployment — Hetzner VPS

Production runs on a **Hetzner VPS** using `docker-compose` with a production override file. This replaces the earlier Azure PaaS plan (see [below](#previously-planned--azure-paas--kubernetes--cancelled)).

### How It Works
The same `docker-compose.yml` used for local development is extended by `docker-compose.prod.yml`, which:
- Sets `ASPNETCORE_ENVIRONMENT=Production` and `SPRING_PROFILES_ACTIVE=rabbitmq,prod`
- Binds all ports to `127.0.0.1` (behind a reverse proxy for public HTTPS)
- Uses production RabbitMQ credentials from `.env.prod`
- Reads all secrets from `.env.prod` (gitignored, never committed)

### Deploying
```bash
# On the VPS:
cp .env.prod.example .env.prod   # fill in real credentials
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

### Production Stack
| Component | Solution |
|---|---|
| Hosting | Hetzner VPS |
| Message Broker | RabbitMQ (self-hosted in Docker on the VPS) |
| PostgreSQL | Self-hosted in Docker on the VPS |
| Secrets | `.env.prod` environment file |
| HTTPS | Reverse proxy (Caddy/nginx) with Let's Encrypt |

### Kubernetes (k8s/ — Local Only)
Kubernetes manifests in `/k8s` are kept for **local development with minikube** to demonstrate how services would run in a k8s environment:
- `java-t3-deployment.yaml` + `java-t3-service.yaml` — Spring Boot
- `csharp-t2-deployment.yaml` + `csharp-t2-service.yaml` — .NET WebAPI
- `postgres-statefulset.yaml` — PostgreSQL StatefulSet
- `kustomization.yaml` — Kustomize overlay pulling config/secrets from `.env`

---

## Previously Planned — Azure PaaS & Kubernetes (Cancelled)

> **Cancelled (April 2026):** The Azure cloud migration was abandoned due to cost. Running AKS, ACR, Key Vault, and Event Hubs/Confluent Cloud together was not cost-effective for this project. All Azure resources have been deleted. Production now uses a Hetzner VPS with docker-compose (see above).

<details>
<summary>Original Azure migration plan (for reference)</summary>

~~The local `docker-compose` stack was being migrated to a fully managed Azure cloud environment.~~

~~**Phase 1 — Managed Services:**
Azure Database for PostgreSQL Flexible Server, Azure Event Hubs (Kafka-compatible), Azure Key Vault for secrets & certificates.~~

~~**Phase 2 — Code Refactoring for Key Vault Auth:**
Java `spring-cloud-azure-starter-keyvault` and .NET `Azure.Extensions.AspNetCore.Configuration.Secrets` + `Azure.Identity` for passwordless auth via Managed Identity.~~

~~**Phase 3 — TLS Offloading:**
Remove self-signed certs from containers; Azure Ingress Controller handles HTTPS termination.~~

~~**Phase 4 — Container Registry & Deployment:**
Azure Container Registry (ACR) for images, Azure Container Apps (ACA) or AKS for orchestration with Managed Identity RBAC.~~

~~**AKS-specific manifests** (Workload Identity, SecretProviderClass for Key Vault CSI driver) were drafted in `k8s/aks/` but have been deleted.~~

</details>

---

## Features

- Customer Registration & Login
- Reseller Login
- Product/Item Upload
- Product/Item Inspection & Purchase
- Customer Profile (Get / Update)
- Address Lookup
- Welcome Email on Registration (async, event-driven via RabbitMQ + MassTransit)

---

## Working Process
- **UP (Unified Process)**
- **Kanban board:** [TeamHood](https://mrpdodaschool.teamhood.com/PHCAWO/Board/SPRNTS?view=KANBAN&token=Ym9hcmRWaWV3OzAxNjZkZTA3NGFlNzQ4M2Y4NGJlZjAzYTg4ZGEwY2Zl)

---

## Analyses & Designs

### Activity Diagram example (login) — [all diagrams](documents/analyses/activity_diagrams)
![Activity Diagram](documents/analyses/activity_diagrams/Log%20in.svg)

### System Sequence Diagram (SSD) example (login) — [all diagrams](documents/analyses/ssds)
![SSD Example](documents/analyses/ssds/login/LogIn.svg)

### Domain Model
![Domain Model](documents/analyses/DomainModel.svg)

### Entity Relationship Diagram (EER)
![EER](documents/images/EER.png)

### Global Relations Diagram (GR)
![GR](documents/designs/GR_ResPawnMarket.svg)

---

## TLS/SSL Certificate Setup Guide

### Overview
This project uses TLS/SSL to secure communication between:
- **Java gRPC Server** (port 6767): PKCS12 keystore for TLS encryption
- **C# WebAPI** (port 6760): One-way TLS handshake with the gRPC server using the same certificate

---

### Development (Self-Signed Certificate)

#### Configuration — Java gRPC Server
`java_projects/ResPawn/src/main/resources/application.properties`:
```properties
spring.grpc.server.ssl.secure=true
spring.grpc.server.ssl.bundle=sep3
spring.ssl.bundle.jks.sep3.keystore.location=${KEYSTORE_LOCATION}
spring.ssl.bundle.jks.sep3.keystore.password=${KEYSTORE_PASSWORD}
spring.ssl.bundle.jks.sep3.keystore.type=PKCS12
spring.ssl.bundle.jks.sep3.key.password=${KEY_PASSWORD}
```

#### Configuration — C# WebAPI
`C_sharp/Server/WebAPI/Program.cs`:
```csharp
builder.WebHost.ConfigureKestrel(options =>
{
    options.ListenLocalhost(6760, lo =>
    {
        lo.UseHttps(pfxFilePath, pfxPassword);
    });
});
```

#### Environment Variables

**Root `.env`** (Docker Compose reads this):
```env
POSTGRES_DB=respawn_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
DOCKER_DB_URL=jdbc:postgresql://portgres_respawn:5432/respawn_db
RELATIVE_KEY_PATH=/app/self_signed_certs/your_cert.p12
KEY_PASSWORD=your_keystore_password
PFX_FILE_PATH=/app/self_signed_certs/your_cert.p12
PFX_PASSWORD=your_keystore_password
JWT__KEY=your_long_jwt_signing_key
JWT__ISSUER=your_issuer
JWT__AUDIENCE=your_audience
JWT__SUBJECT=your_subject
SPRING_RABBITMQ_HOST=respawn_rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest
```

**Java local `.env`** (`java_projects/ResPawnMarket/.env`):
```env
DB_URL=jdbc:postgresql://localhost:5432/respawn_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
DB_DRIVER=org.postgresql.Driver
KEYSTORE_LOCATION=/path/to/your/cert.p12
KEYSTORE_PASSWORD=your_keystore_password
KEY_PASSWORD=your_keystore_password
IS_SSL_ENABLED=true
```

**C# local `.env`** (`C_sharp/Server/WebAPI/.env`):
```env
PFX_FILE_PATH=/path/to/your/cert.p12
PFX_PASSWORD=your_keystore_password
JWT__KEY=your_custom_long_string_jwt_key
JWT__Issuer=your_custom_jwt_issuer
JWT__Audience=your_custom_jwt_audience
JWT__Subject=your_custom_jwt_subject
```

---

### Production (Hetzner VPS)

Secrets are managed via a `.env.prod` file on the VPS (gitignored). The production override `docker-compose.prod.yml` sets all services to Production mode with production RabbitMQ credentials.

```bash
# Deploy:
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

> See `.env.prod.example` for the full list of required environment variables.
