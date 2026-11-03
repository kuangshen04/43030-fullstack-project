import { api } from "@campus-shop/api-client";
import type { Product, ProductInput } from "@campus-shop/api-client";

export interface ProductFilters {
  keyword?: string;
  category?: string;
}

export class ApiRequestError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiRequestError";
    this.status = status;
  }
}

/**
 * Converts the API's error body into a message that can be shown in the UI.
 * The shape originates from the generated OpenAPI types, while this helper
 * keeps runtime error handling defensive.
 */
function describeError(error: unknown, fallback: string): string {
  if (typeof error === "string" && error.trim()) {
    return error;
  }

  if (typeof error === "object" && error !== null) {
    const candidate = error as { message?: unknown; details?: unknown };
    const message = typeof candidate.message === "string" ? candidate.message : fallback;
    const details = Array.isArray(candidate.details)
      ? candidate.details.filter((detail): detail is string => typeof detail === "string")
      : [];

    return details.length > 0 ? `${message} ${details.join(" ")}` : message;
  }

  return fallback;
}

export async function fetchProducts(filters: ProductFilters = {}): Promise<Product[]> {
  const { data, error, response } = await api.GET("/api/products", {
    params: {
      query: {
        keyword: filters.keyword,
        category: filters.category,
      },
    },
  });

  if (!response.ok) {
    throw new ApiRequestError(
      describeError(error, `Could not load products (HTTP ${response.status}).`),
      response.status,
    );
  }

  return data ?? [];
}

export async function createProduct(input: ProductInput): Promise<Product> {
  const { data, error, response } = await api.POST("/api/products", {
    body: input,
  });

  if (!response.ok || !data) {
    throw new ApiRequestError(
      describeError(error, `Could not create the product (HTTP ${response.status}).`),
      response.status,
    );
  }

  return data;
}

export async function updateProduct(id: number, input: ProductInput): Promise<Product> {
  const { data, error, response } = await api.PUT("/api/products/{id}", {
    params: { path: { id } },
    body: input,
  });

  if (!response.ok || !data) {
    throw new ApiRequestError(
      describeError(error, `Could not update the product (HTTP ${response.status}).`),
      response.status,
    );
  }

  return data;
}

export async function deleteProduct(id: number): Promise<void> {
  const { error, response } = await api.DELETE("/api/products/{id}", {
    params: { path: { id } },
  });

  if (!response.ok) {
    throw new ApiRequestError(
      describeError(error, `Could not delete the product (HTTP ${response.status}).`),
      response.status,
    );
  }
}
