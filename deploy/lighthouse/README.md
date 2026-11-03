# Lighthouse deployment

This directory contains the production configuration for the Spring Boot API on the
Tencent Cloud Lighthouse instance at `106.54.199.108`.

## Topology

```text
GitHub Pages                        Lighthouse (Ubuntu 24.04, Docker)
https://kuangshen04.github.io/      https://106.54.199.108
        │                                    │
        │  HTTPS + CORS                      ├─ caddy   :80/:443
        └──────────────────────────────────▶ │    └─ reverse proxy → api:8080
                                             ├─ api     eclipse-temurin:21-jre
                                             └─ postgres:16-alpine (no volume)
```

The TLS endpoint uses a Let's Encrypt **IP address certificate**, valid for six days and
renewed automatically by Caddy. No domain name is required. `default_sni` is configured
because clients intentionally omit SNI when connecting to an IP address.

The PostgreSQL container intentionally has no volume. Deployments run
`docker compose up -d --force-recreate`, so the database is recreated from Flyway
migrations with the seed data. Caddy uses named volumes for its account/certificate
state so TLS certificates survive redeployments.

This is adapted from the declarative Lighthouse experiment in the sibling project. It
does not use `ResetInstance` because this application needs multiple cooperating
containers, an image-free backend deployment, and stable ACME certificate storage. The
database remains ephemeral as required.

## Build and transfer strategy

The GitHub-hosted runner is geographically far from the Lighthouse instance, and a
30 MB jar upload over SSH was prohibitively slow. Instead:

1. GitHub Actions runs the backend tests.
2. It uploads a small archive of `apps/api` source, the Compose file, and the Caddyfile.
3. `remote-deploy.sh` stops the old stack and builds the executable jar on the server
   using the public `maven:3.9-eclipse-temurin-21` image.
4. Maven's dependency cache lives in `/opt/campus-shop/.m2` between releases.
5. The new API, PostgreSQL, and Caddy containers start together.

The first server-side build pulls the Maven image and dependencies and is slower.
Subsequent builds reuse both Docker images and the Maven cache. A measured redeploy with
a warm Maven image and cache is roughly 100 seconds.

## Files

| File | Purpose |
| --- | --- |
| `docker-compose.yml` | Caddy, Spring Boot, and PostgreSQL services |
| `Caddyfile` | Automatic HTTPS for the public IP and reverse proxy to the API |
| `remote-deploy.sh` | Runs on the server, builds the jar, and recreates the stack |
| `.env.example` | Manual deployment example; `.env` is generated on the server |
| `known_hosts` | Pins the Lighthouse SSH host key used by GitHub Actions |

## Manual deployment

Prerequisites: Docker and Docker Compose on the server, ports 80 and 443 open, and SSH
access for the `ubuntu` user with passwordless `sudo`.

Create a small source archive:

```bash
tar -czf api-source.tgz -C apps/api pom.xml src .mvn
```

Upload the archive and deployment files:

```bash
scp api-source.tgz \
  ubuntu@106.54.199.108:/opt/campus-shop/api-source.tgz.new
scp deploy/lighthouse/docker-compose.yml \
  deploy/lighthouse/Caddyfile \
  deploy/lighthouse/remote-deploy.sh \
  ubuntu@106.54.199.108:/opt/campus-shop/
```

Create the remote environment and deploy:

```bash
ssh ubuntu@106.54.199.108
cd /opt/campus-shop
export SITE_ADDRESS=106.54.199.108
export FRONTEND_ORIGIN=https://kuangshen04.github.io
bash remote-deploy.sh
```

Then verify:

```bash
curl -fsS https://106.54.199.108/api/products
curl -fsS https://106.54.199.108/v3/api-docs
curl -sSI -H "Origin: https://kuangshen04.github.io" \
  https://106.54.199.108/api/products
```

It takes roughly 30-90 seconds on a fresh deployment for the API and PostgreSQL to be
ready. On the first deployment, Caddy also needs time to request the IP certificate.
Automatic renewals happen inside the Caddy container roughly every four days.

## GitHub Actions deployment

The `deploy-api.yml` workflow runs the backend tests, uploads the source archive over
SSH, and runs `remote-deploy.sh`. It requires one repository secret,
`LIGHTHOUSE_SSH_KEY`, containing a private key whose public key is authorized for
`ubuntu@106.54.199.108`. The SSH host key is pinned in `known_hosts`.

The workflow is triggered by pushes to `master` that touch the API or `deploy/lighthouse`,
and can also be run manually from the Actions tab. Concurrent runs are serialized.

## Troubleshooting

```bash
# Service state and logs
cd /opt/campus-shop
sudo docker compose ps
sudo docker compose logs --tail=100 api
sudo docker compose logs --tail=100 caddy

# Container-internal network checks
sudo docker compose exec caddy wget -qO- http://api:8080/api/products

# Caddy certificate state
sudo docker compose logs caddy | grep -Ei 'certificate|acme|error'

# Maven build cache and source
ls -la /opt/campus-shop/.m2
ls -la /opt/campus-shop/src
```

If the public IP or SSH host key changes, update:

- `SITE_ADDRESS` in the deploy workflow and `.env.example`
- `known_hosts`
- the relevant IP-derived URL in the GitHub Pages workflow and README

If port 80 is occupied by an old container during migration, identify and remove it
before starting the new stack:

```bash
sudo docker ps --filter publish=80
sudo docker rm -f <old-container-name>
```
