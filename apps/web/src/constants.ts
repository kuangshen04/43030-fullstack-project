export const CATEGORIES = ["Electronics", "Lifestyle", "Books", "Stationery"] as const;

interface CategoryMeta {
  emoji: string;
  theme: string;
}

const CATEGORY_META: Record<string, CategoryMeta> = {
  Electronics: { emoji: "⌨️", theme: "electronics" },
  Lifestyle: { emoji: "🎒", theme: "lifestyle" },
  Books: { emoji: "📚", theme: "books" },
  Stationery: { emoji: "✏️", theme: "stationery" },
};

export function getCategoryMeta(category: string): CategoryMeta {
  return CATEGORY_META[category] ?? { emoji: "📦", theme: "default" };
}
