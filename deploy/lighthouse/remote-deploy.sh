#!/usr/bin/env bash
# Recreate the Campus Shop containers on the Lighthouse host.
# Run this on the server after app.jar.new, docker-compose.yml, and Caddyfile
# have been uploaded to $APP_DIR.
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/campus-shop}"
SITE_ADDRESS="${SITE_ADDRESS:?SITE_ADDRESS is required}"
FRONTEND_ORIGIN="${FRONTEND_ORIGIN:?FRONTEND_ORIGIN is required}"

cd "$APP_DIR"

if [[ ! -f app.jar.new ]]; then
  echo "app.jar.new was not found in $APP_DIR" >&2
  exit 1
fi

mv app.jar.new app.jar

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
