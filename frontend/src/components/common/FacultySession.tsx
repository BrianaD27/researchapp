import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import { Navigate, Outlet, useNavigate } from "react-router-dom";
import apiClient from "../../api/client";

function readSession(): { username: string; expires: number } | null {
  try {
    const token = sessionStorage.getItem("researchapp.accessToken");
    if (!token) return null;
    const part = token.split(".")[1];
    if (!part) return null;
    const base64 = part.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
    const bytes = Uint8Array.from(atob(padded), (character) => character.charCodeAt(0));
    const claims = JSON.parse(new TextDecoder().decode(bytes));
    if (typeof claims.sub !== "string" || typeof claims.exp !== "number") {
      return null;
    }
    if (claims.exp * 1000 <= Date.now()) return null;
    return { username: claims.sub, expires: claims.exp * 1000 };
  } catch {
    return null;
  }
}

function clearSession() {
  sessionStorage.removeItem("researchapp.accessToken");
  sessionStorage.removeItem("researchapp.refreshToken");
}

export default function FacultySession({ loginPath = "/FacultyLogin" }: { loginPath?: string }) {
  const navigate = useNavigate();
  const [valid, setValid] = useState(() => readSession() !== null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    function checkSession() {
      const active = readSession() !== null;
      if (!active) clearSession();
      setValid(active);
    }

    checkSession();
    const timer = window.setInterval(checkSession, 1000);
    window.addEventListener("focus", checkSession);
    window.addEventListener("pageshow", checkSession);
    return () => {
      window.clearInterval(timer);
      window.removeEventListener("focus", checkSession);
      window.removeEventListener("pageshow", checkSession);
    };
  }, []);

  async function logout() {
    if (busy) return;
    const session = readSession();
    if (!session) {
      clearSession();
      setValid(false);
      return;
    }

    setBusy(true);
    setError("");
    try {
      await apiClient.post("/v1/auth/logout", { username: session.username });
      clearSession();
      setValid(false);
      navigate(loginPath, { replace: true });
    } catch (caught) {
      if (isAxiosError(caught) && caught.response?.status === 401) {
        clearSession();
        setValid(false);
      } else {
        setError("Logout could not be completed. Check that Java is running, then try again.");
      }
    } finally {
      setBusy(false);
    }
  }

  if (!valid) return <Navigate to={loginPath} replace />;

  return (
    <>
      <div className="bg-white border-b px-6 py-2 flex flex-wrap items-center justify-end gap-3">
        {error && <p role="alert" className="text-red-700 text-sm">{error}</p>}
        <button
          type="button"
          disabled={busy}
          onClick={() => void logout()}
          className="bg-red-700 text-white font-semibold rounded-lg px-4 py-2 cursor-pointer disabled:opacity-50"
        >
          {busy ? "Logging out..." : "Log Out"}
        </button>
      </div>
      <Outlet />
    </>
  );
}