import { useState } from "react";
import { applyTheme, savedTheme, type Theme } from "../../api/theme";

// Light / dark mode switch in the top bar.
export default function ThemeToggle({ className = "text-sm border border-gray-400 rounded-lg px-3 py-1.5 cursor-pointer" }: {
  className?: string;
}) {
  const [theme, setTheme] = useState<Theme>(savedTheme());

  function flip() {
    const next: Theme = theme === "dark" ? "light" : "dark";
    applyTheme(next);
    setTheme(next);
  }

  return (
    <button type="button" onClick={flip} aria-pressed={theme === "dark"} className={className}>
      {theme === "dark" ? "Light mode" : "Dark mode"}
    </button>
  );
}
