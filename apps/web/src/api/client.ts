import { createApiClient } from "@campus-shop/api-client";

/**
 * Empty in local development, so the Vite proxy handles `/api`.
 * In production this is the public backend origin, for example
 * `https://api.example.com` (no `/api` suffix).
 */
const configuredApiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? "").trim();

export const api = createApiClient(configuredApiBaseUrl);

const normalizedApiBaseUrl = configuredApiBaseUrl.replace(/\/+$/, "");
const configuredDocsUrl = (import.meta.env.VITE_API_DOCS_URL ?? "").trim();

export const apiDocsUrl =
  configuredDocsUrl ||
  (normalizedApiBaseUrl
    ? `${normalizedApiBaseUrl}/swagger-ui.html`
    : "/swagger-ui.html");
