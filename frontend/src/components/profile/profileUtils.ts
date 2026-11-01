// Helpers shared by the student and faculty profile pages.

export function mediaUrl(path: string) {
  return new URL(path, "http://localhost:8080").href;
}

export function initialsOf(name: string) {
  const parts = name.replace(/^Dr\.?\s+/i, "").trim().split(/\s+/).filter(Boolean);
  return parts.slice(0, 2).map((part) => part[0]?.toUpperCase() ?? "").join("") || "?";
}

export function formatDate(value: string | null | undefined) {
  if (!value) return null;
  const [year, month, day] = value.split("-").map(Number);
  if (!year || !month || !day) return value;
  return new Date(year, month - 1, day).toLocaleDateString(undefined, {
    month: "short", day: "numeric", year: "numeric",
  });
}

export function hasText(value: string | null | undefined): value is string {
  return Boolean(value && value.trim());
}

export const actionClass =
  "bg-vsu-orange text-gray-900 font-bold rounded-lg px-5 py-3 min-h-11 inline-flex items-center hover:bg-orange-400";
