<script setup lang="ts">
import { computed } from "vue";

import ProductCard from "./components/ProductCard.vue";
import ProductForm from "./components/ProductForm.vue";
import { CATEGORIES } from "./constants";
import { useProducts } from "./composables/useProducts";

const {
  products,
  isLoading,
  isSaving,
  loadError,
  notice,
  keyword,
  category,
  editingProduct,
  formRevision,
  totalStock,
  lowStockCount,
  categoryCount,
  hasFilters,
  loadProducts,
  clearFilters,
  startCreate,
  startEdit,
  cancelForm,
  saveProduct,
  removeProduct,
} = useProducts();

const resultLabel = computed(() => {
  const count = products.value.length;
  return `${count} ${count === 1 ? "product" : "products"} shown`;
});

const formError = computed(() => (notice.value?.kind === "error" ? notice.value.text : ""));
</script>

<template>
  <div class="app-frame">
    <header class="topbar">
      <a class="brand" href="/" aria-label="Campus Shop home">
        <span class="brand-mark" aria-hidden="true">C</span>
        <span>
          <strong>Campus Shop</strong>
          <small>Full-stack catalog</small>
        </span>
      </a>

      <nav class="topnav" aria-label="Project links">
        <a href="/swagger-ui.html" target="_blank" rel="noreferrer">
          API docs
          <span aria-hidden="true">↗</span>
        </a>
        <span class="status-pill">
          <span class="status-dot" aria-hidden="true"></span>
          Live demo
        </span>
      </nav>
    </header>

    <main class="shell">
      <section class="hero">
        <div class="hero-copy">
          <span class="hero-eyebrow">Student full-stack showcase</span>
          <h1>A small catalog.<br /><em>A complete stack.</em></h1>
          <p>
            Browse, add, edit, and remove products through a typed API. This teaching
            project connects a Vue interface to Spring Boot, PostgreSQL, and MyBatis.
          </p>
          <div class="stack-list" aria-label="Technology stack">
            <span>Vue 3</span>
            <span>Spring Boot</span>
            <span>PostgreSQL</span>
            <span>MyBatis</span>
            <span>OpenAPI</span>
          </div>
        </div>

        <div class="hero-panel" aria-hidden="true">
          <div class="panel-window">
            <div class="window-bar">
              <span></span><span></span><span></span>
              <small>catalog.flow</small>
            </div>
            <div class="flow-row">
              <span class="flow-index">01</span>
              <div>
                <strong>Vue client</strong>
                <small>Typed fetch calls</small>
              </div>
              <code>GET /api/products</code>
            </div>
            <div class="flow-line"></div>
            <div class="flow-row">
              <span class="flow-index">02</span>
              <div>
                <strong>Spring Boot</strong>
                <small>Validation + REST</small>
              </div>
              <code>ProductController</code>
            </div>
            <div class="flow-line"></div>
            <div class="flow-row">
              <span class="flow-index">03</span>
              <div>
                <strong>PostgreSQL</strong>
                <small>MyBatis mapper</small>
              </div>
              <code>products</code>
            </div>
          </div>
          <div class="panel-glow"></div>
        </div>
      </section>

      <section class="stats" aria-label="Catalog summary">
        <article class="stat-card">
          <span class="stat-label">Products</span>
          <strong>{{ products.length }}</strong>
          <small>in the current view</small>
        </article>
        <article class="stat-card">
          <span class="stat-label">Units in stock</span>
          <strong>{{ totalStock }}</strong>
          <small>across matching products</small>
        </article>
        <article class="stat-card stat-warning">
          <span class="stat-label">Low stock</span>
          <strong>{{ lowStockCount }}</strong>
          <small>at 5 units or fewer</small>
        </article>
        <article class="stat-card">
          <span class="stat-label">Categories</span>
          <strong>{{ categoryCount }}</strong>
          <small>represented in the view</small>
        </article>
      </section>

      <section class="workspace">
        <div class="catalog">
          <div class="section-heading">
            <div>
              <span class="section-kicker">Catalog</span>
              <h2>Featured products</h2>
              <p>{{ resultLabel }}</p>
            </div>
            <button type="button" class="btn btn-primary" @click="startCreate">
              <span aria-hidden="true">＋</span>
              New product
            </button>
          </div>

          <div class="toolbar">
            <div class="search-field">
              <span class="search-icon" aria-hidden="true">⌕</span>
              <label class="sr-only" for="product-search">Search products</label>
              <input
                id="product-search"
                v-model="keyword"
                type="search"
                placeholder="Search name or description..."
              />
            </div>

            <div class="category-field">
              <label class="sr-only" for="category-filter">Filter by category</label>
              <select id="category-filter" v-model="category">
                <option value="">All categories</option>
                <option v-for="option in CATEGORIES" :key="option" :value="option">
                  {{ option }}
                </option>
              </select>
            </div>

            <button
              v-if="hasFilters"
              type="button"
              class="btn btn-quiet"
              @click="clearFilters"
            >
              Clear
            </button>
            <button
              type="button"
              class="icon-button"
              title="Refresh catalog"
              aria-label="Refresh catalog"
              :disabled="isLoading"
              @click="loadProducts"
            >
              ↻
            </button>
          </div>

          <div v-if="loadError" class="load-error" role="alert">
            <div>
              <strong>Could not load the catalog.</strong>
              <p>{{ loadError }}</p>
            </div>
            <button type="button" class="btn btn-small" @click="loadProducts">
              Try again
            </button>
          </div>

          <div v-else-if="isLoading" class="product-grid" aria-busy="true">
            <div v-for="index in 4" :key="index" class="skeleton-card">
              <div class="skeleton-visual"></div>
              <div class="skeleton-lines">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>

          <div v-else-if="products.length === 0" class="empty-state">
            <span class="empty-icon" aria-hidden="true">◎</span>
            <h3>No products found</h3>
            <p>
              {{
                hasFilters
                  ? "Try a different search or clear the category filter."
                  : "Create your first product to start building the catalog."
              }}
            </p>
            <button
              v-if="hasFilters"
              type="button"
              class="btn"
              @click="clearFilters"
            >
              Clear filters
            </button>
            <button v-else type="button" class="btn btn-primary" @click="startCreate">
              Create a product
            </button>
          </div>

          <div v-else class="product-grid">
            <ProductCard
              v-for="product in products"
              :key="product.id"
              :product="product"
              @edit="startEdit"
              @delete="removeProduct"
            />
          </div>
        </div>

        <aside class="form-panel" aria-label="Product editor">
          <ProductForm
            :key="formRevision"
            :product="editingProduct"
            :submitting="isSaving"
            :error="formError"
            @submit="saveProduct"
            @cancel="cancelForm"
          />

          <div class="architecture-note">
            <span class="note-icon" aria-hidden="true">↳</span>
            <p>
              API types are generated from the Spring Boot OpenAPI document with
              <code>pnpm openapi:sync</code>.
            </p>
          </div>
        </aside>
      </section>
    </main>

    <footer class="footer">
      <p>Campus Shop · A minimal full-stack teaching project.</p>
      <p class="footer-stack">Vue 3 + Spring Boot 3 + PostgreSQL + MyBatis</p>
    </footer>

    <Transition name="toast">
      <div v-if="notice" class="toast" :class="notice.kind" role="status" aria-live="polite">
        <span aria-hidden="true">{{ notice.kind === "success" ? "✓" : "!" }}</span>
        {{ notice.text }}
      </div>
    </Transition>
  </div>
</template>
