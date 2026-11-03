<script setup lang="ts">
import type { Product, ProductInput } from "@campus-shop/api-client";
import { reactive, ref } from "vue";

import { CATEGORIES } from "../constants";

const props = defineProps<{
  product: Product | null;
  submitting: boolean;
  error: string;
}>();

const emit = defineEmits<{
  submit: [input: ProductInput];
  cancel: [];
}>();

const form = reactive({
  name: props.product?.name ?? "",
  description: props.product?.description ?? "",
  price: props.product?.price ?? 0,
  stock: props.product?.stock ?? 0,
  category: props.product?.category ?? CATEGORIES[0],
});

const localError = ref("");

function handleSubmit() {
  localError.value = "";

  const name = form.name.trim();
  const price = Number(form.price);
  const stock = Number(form.stock);
  const category = form.category.trim();

  if (!name) {
    localError.value = "Please enter a product name.";
    return;
  }
  if (!Number.isFinite(price) || price < 0) {
    localError.value = "Price must be a positive number.";
    return;
  }
  if (!Number.isInteger(stock) || stock < 0) {
    localError.value = "Stock must be a whole number of zero or more.";
    return;
  }
  if (!category) {
    localError.value = "Please choose a category.";
    return;
  }

  emit("submit", {
    name,
    description: form.description.trim(),
    price,
    stock,
    category,
  });
}
</script>

<template>
  <div class="form-card">
    <div class="form-heading">
      <span class="eyebrow">{{ product ? "Editing product" : "New product" }}</span>
      <h2>{{ product ? product.name : "Add to the catalog" }}</h2>
      <p>
        {{
          product
            ? "Update the fields below and save your changes."
            : "Create a product and it will be stored in PostgreSQL."
        }}
      </p>
    </div>

    <p v-if="localError || error" class="form-error" role="alert">
      {{ localError || error }}
    </p>

    <form @submit.prevent="handleSubmit">
      <div class="field">
        <label for="product-name">Name</label>
        <input
          id="product-name"
          v-model="form.name"
          type="text"
          maxlength="120"
          placeholder="e.g. Study Desk Lamp"
          required
        />
      </div>

      <div class="field">
        <label for="product-description">Description</label>
        <textarea
          id="product-description"
          v-model="form.description"
          rows="3"
          maxlength="2000"
          placeholder="A short description for the catalog."
        ></textarea>
      </div>

      <div class="field-row">
        <div class="field">
          <label for="product-price">Price (USD)</label>
          <input
            id="product-price"
            v-model.number="form.price"
            type="number"
            min="0"
            step="0.01"
            required
          />
        </div>

        <div class="field">
          <label for="product-stock">Stock</label>
          <input
            id="product-stock"
            v-model.number="form.stock"
            type="number"
            min="0"
            step="1"
            required
          />
        </div>
      </div>

      <div class="field">
        <label for="product-category">Category</label>
        <select id="product-category" v-model="form.category">
          <option v-for="option in CATEGORIES" :key="option" :value="option">
            {{ option }}
          </option>
        </select>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? "Saving..." : product ? "Save changes" : "Create product" }}
        </button>
        <button type="button" class="btn" :disabled="submitting" @click="emit('cancel')">
          {{ product ? "Cancel" : "Reset" }}
        </button>
      </div>
    </form>

    <p class="form-note">
      <span aria-hidden="true">◇</span>
      Data is validated on the server before MyBatis writes it to PostgreSQL.
    </p>
  </div>
</template>

<style scoped>
.form-card {
  position: sticky;
  top: 92px;
  padding: 24px;
  border: 1px solid #e4e9f1;
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 16px 38px rgb(25 39 65 / 7%);
}

.form-heading {
  margin-bottom: 20px;
}

.eyebrow {
  display: inline-block;
  margin-bottom: 8px;
  color: #2a5bd7;
  font-size: 0.67rem;
  font-weight: 800;
  letter-spacing: 0.11em;
  text-transform: uppercase;
}

.form-heading h2 {
  overflow: hidden;
  margin: 0 0 7px;
  color: #162033;
  font-size: 1.25rem;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.form-heading p {
  margin: 0;
  color: #7a8699;
  font-size: 0.78rem;
  line-height: 1.55;
}

.form-error {
  margin: 0 0 16px;
  padding: 10px 12px;
  border: 1px solid #f2cdcb;
  border-radius: 9px;
  background: #fff5f4;
  color: #b3342f;
  font-size: 0.76rem;
  line-height: 1.45;
}

form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

label {
  color: #344054;
  font-size: 0.74rem;
  font-weight: 700;
}

.form-actions {
  display: flex;
  margin-top: 4px;
  gap: 9px;
}

.form-actions .btn {
  flex: 1;
}

.form-note {
  display: flex;
  margin: 20px 0 0;
  padding-top: 16px;
  align-items: flex-start;
  gap: 7px;
  border-top: 1px solid #edf0f5;
  color: #98a2b3;
  font-size: 0.68rem;
  line-height: 1.5;
}

.form-note span {
  color: #667eea;
}

@media (max-width: 980px) {
  .form-card {
    position: static;
  }
}
</style>
