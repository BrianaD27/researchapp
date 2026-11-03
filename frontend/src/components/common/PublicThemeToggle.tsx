import { useLocation } from "react-router-dom";
import ThemeToggle from "./ThemeToggle";

// Pages people see before signing in. Signed-in pages have the switch in the account menu.
const PUBLIC_PATHS = new Set([
  "/", "/FacultyLogin", "/FacultySignUp", "/StudentLogin", "/StudentSignUp", "/faculty-info",
  "/forgot-password", "/reset-password", "/verify-email", "/PartnerLogin", "/PartnerSignUp",
]);

// Floating light / dark switch on the login and sign-up pages, so people can
// pick before they have an account. The choice carries over after login.
export default function PublicThemeToggle() {
  const { pathname } = useLocation();
  if (!PUBLIC_PATHS.has(pathname)) return null;
  return (
    <div className="fixed bottom-4 right-4 z-50">
      <ThemeToggle className="bg-white text-black shadow-lg border border-gray-300 rounded-full px-4 py-2 text-sm font-semibold cursor-pointer" />
    </div>
  );
}
