# Why We Removed Internal Self-Signed TLS (and Why Caddy Is Enough)

## The Problem with Self-Signed Certs Inside Docker

### What we had before

Every service that communicated internally — the Java gRPC server, the .NET WebAPI — was configured
with a self-signed `.pfx` / `.p12` certificate to encrypt traffic between containers on the same host.

### Why it wasn't worth keeping

**1. The cert couldn't actually authenticate anything.**
The whole point of TLS is two things: *encrypt* the channel and *verify* you're talking to who you
think you are. Because the cert is self-signed (no CA vouches for it), every consumer had to set
`TrustSelfSigned: true` — which disables identity verification entirely. You get the encryption
overhead without the authentication benefit.

**2. The Docker bridge network already isolates the traffic.**
Container-to-container traffic on a single Docker host travels over a private virtual network
(`bridge` or a named network like `caddy_net`). That traffic never touches the public internet,
never leaves the host machine's kernel, and is not reachable by any other process unless you
explicitly expose a port. Adding TLS on top of this is like locking a safe that's already inside
a locked room inside a locked building — it adds weight but not real security.

**3. The real threat model doesn't change with TLS here.**
The only way an attacker could intercept Docker bridge traffic is if they already have code
execution on the host OS. At that point, they can read process memory, environment variables,
and database dumps directly — your TLS session is the least of your problems. TLS protects
against network-level interception; Docker bridge traffic has no network-level exposure.

**4. Operational cost is high for zero gain.**
Self-signed certs expire. When they do, every service that mounts or reads the cert file breaks.
You have to:
- Regenerate the cert locally
- Re-encode it as base64 for CI secrets
- Redeploy every affected service
- Hope nothing else breaks

This is a recurring tax on a problem that doesn't need to exist.

**5. CI/CD becomes unnecessarily complex.**
GitHub Actions runners don't have your local cert files. The old workflow had to pass cert paths
as secrets, reconstruct the `self_signed_certs/` directory before `docker compose up`, and set
`NODE_TLS_REJECT_UNAUTHORIZED=0` to stop Node from rejecting the untrusted cert. All of that
complexity existed to support internal TLS that wasn't providing meaningful security.

---

## Why Caddy Is Enough

```
Browser ──HTTPS──▶ Caddy (Let's Encrypt cert) ──HTTP──▶ Docker network ──▶ Services
            ↑
     This is the only link
     that crosses untrusted
     public internet
```

Caddy sits at the edge and terminates TLS for all inbound traffic. It handles:

- **Automatic cert issuance** via the ACME protocol (Let's Encrypt / ZeroSSL)
- **Automatic cert renewal** — Caddy renews before expiry, zero intervention needed
- **HTTPS for the outside world** — the browser-to-Caddy link is always encrypted with a
  CA-signed cert that browsers trust out of the box

Once traffic passes through Caddy, it enters the private Docker network. From there, the request
reaches the .NET WebAPI over plain HTTP. The data is on a loopback/bridge interface that is:

- Not routable from the internet
- Not accessible to other hosts
- Isolated to the single VPS kernel

This is the standard architecture for single-host containerized services. Nginx, Caddy, Traefik —
all of them are designed to be the TLS termination point, with plain HTTP behind them.

### What this means for your setup

| Link | Protocol | Why |
|---|---|---|
| Browser → Caddy | HTTPS (Let's Encrypt) | Public internet — must be encrypted |
| Caddy → .NET WebAPI | HTTP on `127.0.0.1` | Private Docker network — no exposure |
| .NET WebAPI → Java gRPC | HTTP on Docker bridge | Private Docker network — no exposure |
| All other inter-service | HTTP on Docker bridge | Private Docker network — no exposure |

No cert files to manage. No renewal to schedule. No CI secrets for certs. Caddy handles the one
link that actually needs it.

---

## When CA-Signed Internal TLS Does Become Important

Self-signed or no internal TLS is fine for your current setup. Here are the scenarios where you
would need to revisit this decision and invest in proper internal PKI:

### 1. Services on different machines
Docker bridge networking only spans a single host. If your Java gRPC server and .NET WebAPI ever
run on separate VPS instances and communicate over the public (or even private cloud) network,
that traffic is now exposed. You would need proper TLS with CA-signed certs or mTLS.

### 2. Regulatory compliance
Standards like **PCI DSS**, **HIPAA**, and **SOC 2** explicitly require encryption for data in
transit — including internal service-to-service communication. "Internal" does not get you an
exemption. If you ever process payments or health data, internal TLS with proper certs is required.

### 3. Mutual TLS (mTLS) for service identity
TLS encrypts. mTLS also *authenticates* — each service presents a cert proving its identity, and
the other side verifies it. This matters in environments where you cannot trust that everything
on the internal network is legitimate (e.g., a compromised container, a misconfigured service
accidentally exposed). This is the foundation of zero-trust networking.

Tools for this: **cert-manager** (on Kubernetes), **HashiCorp Vault PKI**, **step-ca** (for
bare-metal / compose setups), **Istio / Linkerd** (service mesh with automatic mTLS).

### 4. Multi-tenant or shared infrastructure
If your VPS hosts other tenants' workloads (a shared cloud VM, not a dedicated server), the
isolation guarantees of the Docker bridge network weaken. In that scenario, encrypting
inter-service traffic adds a meaningful layer of defence.

### 5. Large team / insider threat model
On a solo or small-team project, the person with VPS access is also the person writing the
services. On a large team with role-based access, you may want cryptographic proof that only
authorised services can call each other — not just network-level isolation.

---

## Summary

| Situation | What you need |
|---|---|
| Single-host Docker, portfolio/personal project | Caddy at the edge, HTTP internally |
| Multi-host, services talk over network | TLS with CA-signed certs internally |
| Compliance requirement (PCI, HIPAA, SOC 2) | TLS + proper internal PKI |
| Zero-trust / large team | mTLS with a private CA (step-ca, Vault, cert-manager) |
| Kubernetes cluster | cert-manager + Istio/Linkerd for automatic mTLS |

For this project: Caddy handles the only link that matters. Internal HTTP is the right call.
