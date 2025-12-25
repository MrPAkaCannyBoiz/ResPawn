# ResPawnMarket
SEP3 (Heterogeneous System) VIA Uni Horsens project

ResPawnMarket is an online pawn shop clone modeled based on typical e-commerce platforms. We've adapted the standard online shopping experience to create a pawn shop environment where users can sell their items to the store and receive payment if the shop accepts their offer.

# TLS/SSL Certificate Setup Guide

## Overview

This project uses TLS/SSL to secure communication between:
- **Java gRPC Server** (port 6767): Uses . p12 certificate for TLS encryption
- **C# WebAPI** (port 6760): Performs TLS handshake with gRPC server using the same certificate

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
**Environment Variables**
Create a `.env` file in the Java/C# project root (do NOT commit to git):
```
# Java gRPC Server TLS
KEYSTORE_LOCATION=/path/to/your/certificate.p12
KEYSTORE_PASSWORD=your_keystore_password
KEY_PASSWORD=your_key_password

# C# WebAPI TLS
PFX_FILE_PATH=/path/to/your/certificate.p12
PFX_PASSWORD=your_keystore_password
```
