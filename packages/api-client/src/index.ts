import createClient from "openapi-fetch";

import type { components, paths } from "./generated/schema";

/**
 * Creates a typed HTTP client for the Campus Shop API.
 *
 * Local development uses an empty base URL so requests stay root-relative and
 * flow through the Vite proxy. A split deployment (for example, GitHub Pages
 * frontend + Lighthouse backend) passes an absolute API origin here.
 */
export function createApiClient(baseUrl = "") {
  const normalizedBaseUrl = baseUrl.trim().replace(/\/+$/, "");

  return createClient<paths>({
    ...(normalizedBaseUrl ? { baseUrl: normalizedBaseUrl } : {}),
    headers: {
      Accept: "application/json",
    },
  });
}

export type Product = components["schemas"]["Product"];
export type ProductInput = components["schemas"]["ProductInput"];
export type ApiError = components["schemas"]["ApiError"];

export type { components, paths };
