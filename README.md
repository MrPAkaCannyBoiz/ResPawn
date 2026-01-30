# ResPawnMarket
SEP3 (Heterogeneous System) VIA Uni Horsens project

> [!NOTE]
> **Project Continuation:** This repository serves as the archive for the third semester, SEP3 project. 
> I **(Can/Kanku)** personally love this project and can see this project can be further develop is web app into **production level**. 
Therefore I am continuing active development (update security, improve architectures, migrating to Angular/React, adding Kafka, Docker, etc.) in the **[Re-birth-sPawnMarket](https://github.com/MrPAkaCannyBoiz/Re-birth-sPawnMarket)** repository. Please check it out for the latest updates!

ResPawnMarket is an online pawn shop clone modeled based on typical e-commerce platforms. We've adapted the standard online shopping experience to create a pawn shop environment where users can sell their items to the store and receive payment if the shop accepts their offer.

# Learning Outcome
- gRPC (Server in Spring Boot (Java)/ Client in .NET (C#)
- RESTful API
- TLS/SSL via asymmetric keys pairs
- http to https via asymmetric keys pairs
- Blazor

# Working Process
- UP (Unified Process)
- Kanban **[Link Here (TeamHood)](https://mrpdodaschool.teamhood.com/PHCAWO/Board/SPRNTS?view=KANBAN&token=Ym9hcmRWaWV3OzAxNjZkZTA3NGFlNzQ4M2Y4NGJlZjAzYTg4ZGEwY2Zl)**

# TLS/SSL Certificate Setup Guide

## Overview
This project uses TLS/SSL to secure communication between:
- **Java gRPC Server** (port 6767): Uses . p12 certificate for TLS encryption
- **C# WebAPI** (port 6760): Performs One-way TLS handshake with gRPC server using the same certificate

This guide explains how to set up certificates for both development (self-signed) and production (Let's Encrypt). 

---

## Development Environment (Self-Signed Certificate)

### Current Setup

The project currently uses a **self-signed certificate** in `. p12` format.  This is appropriate for local development and testing.

#### Configuration Files

**Java gRPC Server** (`java_projects/ReSpawnMarket/src/main/resources/application.properties`):
``` properties
spring.grpc.server.ssl.secure=true
spring.grpc.server.ssl.bundle=sep3
spring.ssl.bundle.jks.sep3.keystore.location=${KEYSTORE_LOCATION}
spring.ssl.bundle.jks.sep3.keystore.password=${KEYSTORE_PASSWORD}
spring.ssl.bundle.jks.sep3.keystore.type=PKCS12
spring.ssl.bundle.jks.sep3.key.password=${KEY_PASSWORD}
```
**C# WebAPI** (`C_sharp/Server/WebAPI/Program.cs`):
``` Program.cs 
builder.WebHost.ConfigureKestrel(options =>
{
    options.ListenLocalhost(6760, lo =>
    {
        lo.UseHttps(pfxFilePath, pfxPassword);
    });
});
```
#### Environment Variables
Create a `.env` file in the Java/C# project root (do NOT commit to git):

**Java (gRPC Server) TLS** (`java_projects/ResPawnMarket`):
Create `.env` file in `java_projects/ResPawnMarket`
``` .env
DB_URL=your/database/url
DB_USERNAME=your/database/username
DB_PASSWORD=your/database/password
DB_DRIVER=org.postgresql.Driver
SSL_KEYSTORE_PATH=/path/to/your/keystore/file
SSL_KEYSTORE_PASSWORD=your_keystore_password
KEYSTORE_LOCATION=/path/to/your/certificate.p12
KEYSTORE_PASSWORD=your_keystore_password
KEY_PASSWORD=your_keystore_password
IS_SSL_ENABLED=true
```

**C# (REST/gRPC Client TLS)** (`C_sharp/Server/WebAPI`):
Create `.env` file in `C_sharp/Server/WebAPI`
``` .env
PFX_FILE_PATH=/path/to/your/certificate.p12
PFX_PASSWORD=your_keystore_password

JWT__KEY=your_custom_long_string_jwt_key
JWT__Issuer=your_custom_jwt_issuer
JWT__Audience=your_custom_jwt_audience
JWT__Subject=your_custom_jwt_subject
```
