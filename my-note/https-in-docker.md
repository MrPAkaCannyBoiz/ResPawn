# HTTPS Inside Docker — What You Need to Know

## The Problem

When two containers communicate over HTTPS, the receiving container's certificate must pass validation on the calling side. Two things can cause this to fail:

### 1. Hostname Mismatch
A certificate is issued for a specific hostname (e.g. `localhost`). Inside Docker, containers reach each other via their **service name** (e.g. `grpc-client-dotnet-services`), not `localhost`. If the cert's Subject Alternative Name (SAN) doesn't include the service name, validation fails with a hostname mismatch error.

### 2. Untrusted CA
A self-signed certificate is not signed by any Certificate Authority (CA) that the OS trusts by default. Even if the hostname matches, the calling container will reject the cert unless it has been explicitly added to its trust store.

---

## Do You Need a CA-Signed Cert?

**No.** You do not need a cert from a public CA (like Let's Encrypt). Public CA certs also require a publicly reachable domain, so they're not practical for internal Docker networking.

You have two self-managed options:

### Option A: Self-signed cert with the correct SAN
1. Regenerate the `.pfx` cert with a SAN that includes the Docker service hostname (e.g. `grpc-client-dotnet-services`).
2. In the Blazor Dockerfile, copy the `.crt` (public part) into the image and install it into the container's trusted root store:
   ```dockerfile
   COPY self_signed_certs/mycert.crt /usr/local/share/ca-certificates/mycert.crt
   RUN update-ca-certificates
   ```
3. This makes the OS (and .NET's `HttpClient`) trust the cert.

### Option B: Private CA (more scalable)
1. Create your own root CA certificate.
2. Issue service certificates signed by your CA.
3. Install the **CA cert** (not the service cert) into every container that needs to call HTTPS services.
4. Any cert signed by your CA is automatically trusted — no per-cert updates needed when you rotate service certs.

---

## Why HTTP for Internal Traffic Is the Standard Pattern

Managing certs inside a private Docker network adds complexity with little security benefit — all traffic is already inside a trusted private network. The common industry practice is:

- **HTTPS at the edge** — reverse proxy / load balancer (e.g. nginx, Traefik) terminates TLS for external clients.
- **Plain HTTP internally** — service-to-service calls inside the Docker network use HTTP.

This is what was implemented in this project: WebAPI listens on:
- `:6760` — HTTPS, for external/local dev access
- `:6761` — HTTP, for internal container traffic (BlazorApp → WebAPI)
