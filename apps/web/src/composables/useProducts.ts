import type { Product, ProductInput } from "@campus-shop/api-client";
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";

import {
  createProduct,
  deleteProduct,
  fetchProducts,
  updateProduct,
} from "../api/products";

type NoticeKind = "success" | "error";

interface Notice {
  kind: NoticeKind;
  text: string;
}

function errorMessage(error: unknown, fallback: string): string {
  return error instanceof Error && error.message ? error.message : fallback;
}

export function useProducts() {
  const products = ref<Product[]>([]);
  const isLoading = ref(true);
  const isSaving = ref(false);
  const loadError = ref("");
  const notice = ref<Notice | null>(null);

  const keyword = ref("");
  const category = ref("");

  const editingProduct = ref<Product | null>(null);
  const formRevision = ref(0);

  const totalStock = computed(() =>
    products.value.reduce((total, product) => total + (product.stock ?? 0), 0),
  );
  const lowStockCount = computed(
    () => products.value.filter((product) => (product.stock ?? 0) <= 5).length,
  );
  const categoryCount = computed(
    () => new Set(products.value.map((product) => product.category)).size,
  );
  const hasFilters = computed(() => Boolean(keyword.value.trim() || category.value));

  let searchTimer: ReturnType<typeof setTimeout> | undefined;
  let noticeTimer: ReturnType<typeof setTimeout> | undefined;
  let requestSequence = 0;

  function showNotice(kind: NoticeKind, text: string) {
    if (noticeTimer) {
      clearTimeout(noticeTimer);
    }
    notice.value = { kind, text };
    noticeTimer = setTimeout(() => {
      notice.value = null;
    }, 3200);
  }

  async function loadProducts() {
    const requestId = ++requestSequence;
    isLoading.value = true;
    loadError.value = "";

    try {
      const result = await fetchProducts({
        keyword: keyword.value.trim() || undefined,
        category: category.value || undefined,
      });

      if (requestId === requestSequence) {
        products.value = result;
      }
    } catch (error) {
      if (requestId === requestSequence) {
        loadError.value = errorMessage(error, "Could not load the catalog.");
      }
    } finally {
      if (requestId === requestSequence) {
        isLoading.value = false;
      }
    }
  }

  function scheduleSearch() {
    if (searchTimer) {
      clearTimeout(searchTimer);
    }
    searchTimer = setTimeout(loadProducts, 250);
  }

  watch(keyword, scheduleSearch);
  watch(category, () => {
    if (searchTimer) {
      clearTimeout(searchTimer);
    }
    void loadProducts();
  });

  function clearFilters() {
    keyword.value = "";
    category.value = "";
  }

  function startCreate() {
    editingProduct.value = null;
    formRevision.value += 1;
  }

  function startEdit(product: Product) {
    editingProduct.value = product;
    formRevision.value += 1;
  }

  function cancelForm() {
    editingProduct.value = null;
    formRevision.value += 1;
  }

  async function saveProduct(input: ProductInput) {
    if (isSaving.value) {
      return;
    }

    isSaving.value = true;
    const current = editingProduct.value;

    try {
      if (current) {
        await updateProduct(current.id, input);
        showNotice("success", `"${input.name}" was updated.`);
      } else {
        await createProduct(input);
        showNotice("success", `"${input.name}" was added to the catalog.`);
      }

      editingProduct.value = null;
      formRevision.value += 1;
      await loadProducts();
    } catch (error) {
      showNotice("error", errorMessage(error, "Could not save the product."));
    } finally {
      isSaving.value = false;
    }
  }

  async function removeProduct(product: Product) {
    const confirmed = window.confirm(
      `Delete "${product.name}"? This action cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    try {
      await deleteProduct(product.id);
      if (editingProduct.value?.id === product.id) {
        cancelForm();
      }
      showNotice("success", `"${product.name}" was deleted.`);
      await loadProducts();
    } catch (error) {
      showNotice("error", errorMessage(error, "Could not delete the product."));
    }
  }

  onMounted(() => {
    void loadProducts();
  });

  onBeforeUnmount(() => {
    if (searchTimer) {
      clearTimeout(searchTimer);
    }
    if (noticeTimer) {
      clearTimeout(noticeTimer);
    }
  });

  return {
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
  };
}
