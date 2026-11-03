import createClient from "openapi-fetch";

import type { components, paths } from "./generated/schema";

/**
 * Typed HTTP client for the Campus Shop API.
 *
 * Requests use root-relative paths so the Vite dev proxy can forward `/api`
 * calls to Spring Boot. The types come from the OpenAPI document, not from
 * hand-written interfaces.
 */
export const api = createClient<paths>({
  headers: {
    Accept: "application/json",
  },
});

export type Product = components["schemas"]["Product"];
export type ProductInput = components["schemas"]["ProductInput"];
export type ApiError = components["schemas"]["ApiError"];

export type { components, paths };
