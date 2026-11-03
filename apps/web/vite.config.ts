import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

const backendTarget = "http://localhost:8080";

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
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
    },
  },
});
