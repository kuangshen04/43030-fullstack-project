<script setup lang="ts">
import type { Product } from "@campus-shop/api-client";
import { computed } from "vue";

import { getCategoryMeta } from "../constants";

const props = defineProps<{
  product: Product;
}>();

const emit = defineEmits<{
  edit: [product: Product];
  delete: [product: Product];
}>();

const currency = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

const dateFormat = new Intl.DateTimeFormat("en-US", {
  month: "short",
  day: "numeric",
  year: "numeric",
});

const meta = computed(() => getCategoryMeta(props.product.category));

const price = computed(() => currency.format(props.product.price));

const stockState = computed(() => {
  const stock = props.product.stock;
  if (stock === 0) {
    return { className: "out", label: "Out of stock" };
  }
  if (stock <= 5) {
    return { className: "low", label: `Low stock · ${stock} left` };
  }
  return { className: "ok", label: `${stock} in stock` };
});

const description = computed(
  () => props.product.description || "No description has been added yet.",
);

const createdAt = computed(() =>
  dateFormat.format(new Date(props.product.createdAt)),
);
</script>

<template>
  <article class="product-card">
    <div class="product-visual" :class="`theme-${meta.theme}`">
      <span class="category-badge">{{ product.category }}</span>
      <span class="category-icon" aria-hidden="true">{{ meta.emoji }}</span>
      <span class="product-id">#{{ String(product.id).padStart(3, "0") }}</span>
    </div>

    <div class="product-body">
      <div class="title-row">
        <h3>{{ product.name }}</h3>
        <strong class="price">{{ price }}</strong>
      </div>

      <p class="description">{{ description }}</p>

      <div class="card-meta">
        <span class="stock" :class="stockState.className">
          <span class="stock-dot" aria-hidden="true"></span>
          {{ stockState.label }}
        </span>
        <span class="created">Added {{ createdAt }}</span>
      </div>

      <div class="card-actions">
        <button type="button" class="btn btn-small" @click="emit('edit', product)">
          Edit
        </button>
        <button
          type="button"
          class="btn btn-small btn-danger-ghost"
          @click="emit('delete', product)"
        >
          Delete
        </button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.product-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e5e9f1;
  border-radius: 18px;
  box-shadow: 0 8px 28px rgb(25 39 65 / 5%);
  transition:
    transform 160ms ease,
    box-shadow 160ms ease,
    border-color 160ms ease;
}

.product-card:hover {
  transform: translateY(-2px);
  border-color: #d6ddea;
  box-shadow: 0 16px 36px rgb(25 39 65 / 9%);
}

.product-visual {
  position: relative;
  display: flex;
  min-height: 112px;
  padding: 14px 16px;
  align-items: flex-start;
  justify-content: space-between;
  overflow: hidden;
  background: #eef2ff;
  isolation: isolate;
}

.product-visual::before,
.product-visual::after {
  position: absolute;
  z-index: -1;
  content: "";
  border-radius: 999px;
  background: rgb(255 255 255 / 45%);
}

.product-visual::before {
  width: 150px;
  height: 150px;
  top: -95px;
  right: -20px;
}

.product-visual::after {
  width: 100px;
  height: 100px;
  right: 48px;
  bottom: -80px;
}

.theme-electronics {
  background: linear-gradient(135deg, #e6efff, #dce7ff);
}

.theme-lifestyle {
  background: linear-gradient(135deg, #fdf0e7, #fbe4d5);
}

.theme-books {
  background: linear-gradient(135deg, #ecf7ee, #dff0e4);
}

.theme-stationery {
  background: linear-gradient(135deg, #f7edf8, #efe0f4);
}

.theme-default {
  background: linear-gradient(135deg, #eef2f7, #e5eaf1);
}

.category-badge {
  display: inline-flex;
  padding: 5px 9px;
  align-items: center;
  border: 1px solid rgb(255 255 255 / 70%);
  border-radius: 999px;
  background: rgb(255 255 255 / 65%);
  color: #36445c;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.category-icon {
  position: absolute;
  right: 22px;
  bottom: 14px;
  font-size: 2.7rem;
  line-height: 1;
  filter: drop-shadow(0 5px 10px rgb(25 39 65 / 12%));
}

.product-id {
  position: absolute;
  right: 16px;
  top: 16px;
  color: rgb(54 68 92 / 48%);
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.product-body {
  display: flex;
  flex: 1;
  padding: 17px 18px 16px;
  flex-direction: column;
  gap: 11px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

h3 {
  margin: 0;
  color: #162033;
  font-size: 1rem;
  font-weight: 700;
  line-height: 1.35;
}

.price {
  flex-shrink: 0;
  color: #1f4ed8;
  font-size: 1rem;
  font-weight: 800;
  letter-spacing: -0.01em;
}

.description {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: #667085;
  font-size: 0.83rem;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.card-meta {
  display: flex;
  margin-top: auto;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #98a2b3;
  font-size: 0.7rem;
}

.stock {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #475467;
  font-weight: 650;
}

.stock-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentcolor;
}

.stock.out {
  color: #c2413b;
}

.stock.low {
  color: #b56a11;
}

.stock.ok {
  color: #17815b;
}

.card-actions {
  display: flex;
  padding-top: 12px;
  gap: 8px;
  border-top: 1px solid #eef1f6;
}

.card-actions button {
  flex: 1;
}
</style>
