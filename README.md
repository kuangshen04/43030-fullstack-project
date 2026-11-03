# Campus Shop

A deliberately small full-stack project for student demonstrations. It implements a
product catalog with complete CRUD: browse, search, filter, create, edit, and delete.

The project focuses on the shape of a modern web application rather than visual polish:
a Vue single-page app, a Spring Boot REST API, PostgreSQL persistence through MyBatis,
and a frontend API client generated from the backend's OpenAPI document.

> All repository content is in English: UI copy, documentation, comments, seed data,
> test data, and application messages.

![Campus Shop dashboard](docs/screenshot.png)

## Tech stack

| Layer | Technology |
| --- | --- |
| Frontend | Vue 3, TypeScript, Vite |
| API client | `openapi-typescript` + `openapi-fetch` |
| Backend | Spring Boot 3.5, Spring MVC, Bean Validation |
| Persistence | MyBatis XML mappers, Flyway migrations |
| Database | PostgreSQL 16 |
| API documentation | springdoc-openapi + Swagger UI |
| Monorepo | pnpm workspace |
| Local infrastructure | Docker Compose |

## Architecture

```text
┌──────────────────────────────────────────────────────────────────┐
│ Browser                                                          │
│  Vue 3 SPA (apps/web)                                            │
│    └─ @campus-shop/api-client                                    │
│         └─ openapi-fetch + generated OpenAPI types               │
└─────────────────────────────┬────────────────────────────────────┘
                              │ /api/* (Vite dev proxy)
┌─────────────────────────────▼────────────────────────────────────┐
│ Spring Boot API (apps/api)                                       │
│  Controller → Service → MyBatis Mapper → PostgreSQL              │
│  Bean Validation · Global exception handler · Flyway             │
└─────────────────────────────┬────────────────────────────────────┘
                              │ JDBC
                    ┌─────────▼─────────┐
                    │ PostgreSQL 16     │
                    │ products table    │
                    └───────────────────┘

OpenAPI type sync:
  Spring Boot /v3/api-docs
      → scripts/sync-openapi.mjs
      → openapi/openapi.json
      → packages/api-client/src/generated/schema.ts
      → typed Vue API calls
```

## Repository layout

```text
.
├─ apps/
│  ├─ api/                        # Spring Boot + MyBatis backend
│  │  ├─ .mvn/                    # Project-local Maven mirror configuration
│  │  ├─ src/main/java/           # Controllers, service, mapper, DTOs
│  │  ├─ src/main/resources/
│  │  │  ├─ db/migration/         # Flyway schema + demo seed data
│  │  │  └─ mapper/               # MyBatis XML SQL
│  │  └─ src/test/java/           # Web-layer tests
│  └─ web/                        # Vue 3 + Vite application
│     └─ src/
│        ├─ api/                  # Typed request functions
│        ├─ components/           # Product card and editor form
│        ├─ composables/          # Catalog state and actions
│        └─ App.vue               # Page composition
├─ packages/
│  └─ api-client/                 # Generated API types + fetch client
├─ openapi/openapi.json           # Snapshot of the backend OpenAPI document
├─ scripts/sync-openapi.mjs       # Schema/type generation script
├─ docker-compose.yml             # Local PostgreSQL service
└─ pnpm-workspace.yaml
```

## Prerequisites

- Node.js 20.19+ or 22.12+ (Node 22 LTS or newer recommended)
- pnpm 12+ (tested with 12.11.2)
- JDK 21+
- Maven 3.9+
- Docker Desktop or another Docker + Compose installation

## Quick start

From the repository root:

```bash
pnpm install
pnpm db:up
pnpm dev
```

Wait for both services to start, then open:

- App: http://localhost:5173
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

The database is prepared automatically by Flyway on the first API start, including
eight demo products. Stop the applications with `Ctrl+C`, then stop PostgreSQL with:

```bash
pnpm db:down
```

To remove the database volume and recreate the demo data from scratch:

```bash
pnpm db:reset
```

## Useful commands

| Command | Description |
| --- | --- |
| `pnpm dev` | Start the Spring Boot API and Vite dev server together |
| `pnpm dev:api` | Start only the backend |
| `pnpm dev:web` | Start only the frontend |
| `pnpm db:up` | Start PostgreSQL and wait for it to become healthy |
| `pnpm db:down` | Stop the local database container |
| `pnpm db:reset` | Delete the database volume and start a fresh database |
| `pnpm openapi:sync` | Fetch `/v3/api-docs` and regenerate schema + frontend types |
| `pnpm typecheck` | Type-check the Vue application and generated client |
| `pnpm test` | Run backend tests and frontend type checking |
| `pnpm build` | Build the Vue app and package the Spring Boot jar |

## OpenAPI type synchronization

The backend is the source of truth. Springdoc turns the controllers and DTOs into an
OpenAPI document, and the frontend types are generated from that document.

With the backend running:

```bash
pnpm openapi:sync
```

This command writes:

1. `openapi/openapi.json` — a readable snapshot of the current contract.
2. `packages/api-client/src/generated/schema.ts` — TypeScript `paths` and `components`
   types used by `openapi-fetch`.

The generated types are committed so the project can be explored without first running
the backend. Do not edit `schema.ts` manually. When a backend DTO or endpoint changes,
run `pnpm openapi:sync` and fix any TypeScript errors in the Vue app.

The API wrapper in `packages/api-client/src/index.ts` exports `Product`, `ProductInput`,
and `ApiError` directly from the generated schema. This is what makes the frontend
request bodies and responses type-safe at compile time.

## API

All endpoints are prefixed with `/api`.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/products` | List products; supports `keyword` and `category` query parameters |
| `GET` | `/api/products/{id}` | Fetch one product |
| `POST` | `/api/products` | Create a product |
| `PUT` | `/api/products/{id}` | Update a product |
| `DELETE` | `/api/products/{id}` | Delete a product |

Example request:

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Study Desk Lamp",
    "description": "A warm LED lamp with three brightness levels.",
    "price": 32.50,
    "stock": 15,
    "category": "Lifestyle"
  }'
```

Validation failures return HTTP `400` with a consistent error shape:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "details": ["name: Name is required."]
}
```

## Data model

The single `products` table is created by
`apps/api/src/main/resources/db/migration/V1__create_products.sql`:

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `BIGSERIAL` | Primary key |
| `name` | `VARCHAR(120)` | Required |
| `description` | `TEXT` | Defaults to an empty string |
| `price` | `NUMERIC(10, 2)` | Must be non-negative |
| `stock` | `INTEGER` | Must be non-negative |
| `category` | `VARCHAR(40)` | Indexed |
| `created_at` | `TIMESTAMPTZ` | Set on insert |
| `updated_at` | `TIMESTAMPTZ` | Updated when a product changes |

Flyway runs pending migrations automatically on API startup.

## Suggested classroom demo

1. Open the app and point out the Vue → Spring Boot → MyBatis → PostgreSQL flow.
2. Search for a product and filter the catalog by category.
3. Create a product, then show the new row in PostgreSQL if desired.
4. Submit an invalid form to show client-side and server-side validation.
5. Edit a product's stock and price.
6. Delete a product (a confirmation dialog appears).
7. Open Swagger UI and execute `GET /api/products`.
8. Show `openapi/openapi.json` and explain how `pnpm openapi:sync` regenerates the
   TypeScript client types.
9. Run `pnpm test` to demonstrate the backend tests and frontend type check.

To inspect the database directly:

```bash
docker compose exec postgres psql -U campus -d campus_shop
```

```sql
SELECT id, name, price, stock, category FROM products ORDER BY id;
```

## Configuration

The API reads these environment variables, with development defaults:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/campus_shop` |
| `DB_USERNAME` | `campus` |
| `DB_PASSWORD` | `campus123` |
| `SERVER_PORT` | `8080` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` |

The frontend currently uses root-relative URLs and Vite's `/api` proxy. The proxy target
is set in `apps/web/vite.config.ts`.

### Maven mirror note

`apps/api/.mvn/settings.xml` routes Maven downloads through the Huawei Cloud mirror. It
was added because Maven Central was unreachable from the environment where the project
was created. Remove the `apps/api/.mvn/` directory if your network can reach Maven
Central normally. Maven commands should be run from `apps/api` (or through the pnpm
scripts) so that this project-local configuration is picked up.

## Troubleshooting

**The API exits because it cannot connect to PostgreSQL**

Run `pnpm db:up` and confirm the container is healthy with `docker compose ps`.

**Port 5432 is already in use**

Change the host port in `docker-compose.yml` (for example, `"5433:5432"`) and set
`DB_URL=jdbc:postgresql://localhost:5433/campus_shop` when starting the API.

**OpenAPI sync fails with a connection error**

Start the backend first (`pnpm dev:api`) and confirm
http://localhost:8080/v3/api-docs responds. Then run `pnpm openapi:sync` again.

**Maven cannot resolve dependencies**

Check network access to the configured mirror, or remove `apps/api/.mvn/` and use your
own Maven `settings.xml`.

**The frontend loads but the catalog shows an error**

Check the backend terminal and http://localhost:8080/api/products. The Vite dev server
proxies `/api` to port `8080`.

## Intentional limitations

This is a teaching project, not a production starter:

- No authentication or authorization.
- No pagination; the endpoint returns the filtered catalog in one response.
- No image uploads; category gradients are used as simple visual placeholders.
- Prices are displayed in USD.
- The database credentials and CORS settings are development defaults.
- There are web-layer tests rather than a full end-to-end/Testcontainers suite.

Natural next steps would be adding authentication, pagination, integration tests with
Testcontainers, and a production deployment configuration.
