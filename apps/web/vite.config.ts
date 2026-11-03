import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";

function parsePort(value: string | undefined, fallback: number): number {
  const port = Number(value);
  return Number.isInteger(port) && port > 0 && port <= 65535 ? port : fallback;
}

function normalizeBasePath(value: string | undefined): string {
  const trimmed = (value ?? "").trim();
  if (!trimmed || trimmed === "/") {
    return "/";
  }
  return `/${trimmed.replace(/^\/+|\/+$/g, "")}/`;
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const backendPort = parsePort(env.SERVER_PORT, 8080);
  const backendTarget = (
    env.VITE_API_PROXY_TARGET || `http://localhost:${backendPort}`
  ).replace(/\/+$/, "");

  const proxy = {
    "/api": {
      target: backendTarget,
      changeOrigin: true,
    },
    "/swagger-ui": {
      target: backendTarget,
      changeOrigin: true,
    },
    "/v3/api-docs": {
      target: backendTarget,
      changeOrigin: true,
    },
  };

  return {
    base: normalizeBasePath(env.VITE_BASE_PATH),
    plugins: [vue()],
    server: {
      port: parsePort(env.VITE_DEV_SERVER_PORT, 5173),
      strictPort: true,
      proxy,
    },
    preview: {
      port: parsePort(env.VITE_PREVIEW_PORT, 4173),
      strictPort: true,
      proxy,
    },
  };
});
