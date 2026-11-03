// Light or dark mode, remembered in this browser only.
const KEY = "researchapp.theme";

export type Theme = "light" | "dark";

export function savedTheme(): Theme {
  try {
    return localStorage.getItem(KEY) === "dark" ? "dark" : "light";
  } catch {
    return "light";
  }
}

export function applyTheme(theme: Theme) {
  document.documentElement.classList.toggle("theme-dark", theme === "dark");
  try {
    localStorage.setItem(KEY, theme);
  } catch {
    // Storage can be unavailable (private windows); the theme still applies for now.
  }
}
