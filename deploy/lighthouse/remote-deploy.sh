#!/usr/bin/env bash
# Recreate the Campus Shop containers on the Lighthouse host.
# Run this on the server after api-source.tgz.new, docker-compose.yml, and
# Caddyfile have been uploaded to $APP_DIR.
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/campus-shop}"
SITE_ADDRESS="${SITE_ADDRESS:?SITE_ADDRESS is required}"
FRONTEND_ORIGIN="${FRONTEND_ORIGIN:?FRONTEND_ORIGIN is required}"
SOURCE_ARCHIVE="${SOURCE_ARCHIVE:-api-source.tgz.new}"

cd "$APP_DIR"

if [[ ! -f "$SOURCE_ARCHIVE" ]]; then
  echo "$SOURCE_ARCHIVE was not found in $APP_DIR" >&2
  exit 1
fi

# Stop the old stack before compiling to free memory on the 2 GB instance.
# Named Caddy volumes survive, so TLS certificates are not requested again.
sudo -n docker compose down --remove-orphans || true

# The GitHub runner is far from the server, so send a small source archive and
# compile here. Maven dependencies are cached in $APP_DIR/.m2 between releases.
sudo -n rm -rf "$APP_DIR/src"
mkdir -p "$APP_DIR/src" "$APP_DIR/.m2"
tar -xzf "$SOURCE_ARCHIVE" -C "$APP_DIR/src"

sudo -n docker run --rm \
  -v "$APP_DIR/src:/workspace" \
  -v "$APP_DIR/.m2:/root/.m2" \
  -w /workspace \
  maven:3.9-eclipse-temurin-21 \
  mvn -B -q -DskipTests package

JAR="$(find "$APP_DIR/src/target" -maxdepth 1 -name '*.jar' ! -name '*.original' | head -n 1)"
if [[ -z "$JAR" ]]; then
  echo "Maven did not produce a Spring Boot jar." >&2
  exit 1
fi
cp "$JAR" app.jar

# The database is intentionally disposable. A fresh password is generated on
# every deployment, and PostgreSQL has no persistent volume in the compose file.
DB_PASSWORD="$(openssl rand -hex 24)"
umask 077
{
  printf 'SITE_ADDRESS=%s\n' "$SITE_ADDRESS"
  printf 'CORS_ALLOWED_ORIGINS=%s\n' "$FRONTEND_ORIGIN"
  printf 'POSTGRES_PASSWORD=%s\n' "$DB_PASSWORD"
} > .env

# --force-recreate also replaces the PostgreSQL container, so its data is reset.
sudo -n docker compose pull
sudo -n docker compose up -d --force-recreate --remove-orphans
sudo -n docker compose ps
