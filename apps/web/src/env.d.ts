/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string;
  readonly VITE_API_DOCS_URL?: string;
  readonly VITE_API_PROXY_TARGET?: string;
  readonly VITE_BASE_PATH?: string;
  readonly VITE_DEV_SERVER_PORT?: string;
  readonly VITE_PREVIEW_PORT?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
