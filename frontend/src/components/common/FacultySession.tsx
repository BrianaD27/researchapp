import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import { Navigate, Outlet, useNavigate } from "react-router-dom";
import apiClient from "../../api/client";
import NotificationBell from "./NotificationBell";
import TopBarLinks from "./TopBarLinks";

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

const ACTIVITY_KEY = "researchapp.lastActivity";
const IDLE_LIMIT_MS = 30 * 60 * 1000;
const IDLE_WARNING_MS = 60 * 1000;

function lastActivity(): number {
  const stored = Number(sessionStorage.getItem(ACTIVITY_KEY));
  return Number.isFinite(stored) && stored > 0 ? stored : 0;
}

function markActive() {
  sessionStorage.setItem(ACTIVITY_KEY, String(Date.now()));
}

function clearSession() {
  sessionStorage.removeItem("researchapp.accessToken");
  sessionStorage.removeItem("researchapp.refreshToken");
  sessionStorage.removeItem(ACTIVITY_KEY);
  sessionStorage.removeItem("researchapp.actingFor");
}

export default function FacultySession({ loginPath = "/FacultyLogin" }: { loginPath?: string }) {
  const navigate = useNavigate();
  const [valid, setValid] = useState(() => readSession() !== null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [idleSecondsLeft, setIdleSecondsLeft] = useState<number | null>(null);

  // Signs out after 30 minutes with no mouse, keyboard, scroll, or touch activity.
  useEffect(() => {
    if (!lastActivity()) markActive();
    let lastWrite = 0;
    function onActivity() {
      const now = Date.now();
      if (now - lastWrite < 5000) return;
      lastWrite = now;
      markActive();
    }
    const events = ["mousemove", "mousedown", "keydown", "scroll", "touchstart"] as const;
    events.forEach((name) => window.addEventListener(name, onActivity, { passive: true }));
    return () => events.forEach((name) => window.removeEventListener(name, onActivity));
  }, []);

  useEffect(() => {
    function checkSession() {
      const session = readSession();
      if (session && lastActivity() && Date.now() - lastActivity() >= IDLE_LIMIT_MS) {
        // Best effort server logout; the local session ends either way.
        apiClient.post("/v1/auth/logout", { username: session.username }).catch(() => undefined);
        clearSession();
        sessionStorage.setItem("researchapp.idleSignedOut", "1");
        setIdleSecondsLeft(null);
        setValid(false);
        return;
      }
      const active = session !== null;
      if (!active) clearSession();
      setValid(active);
      const remaining = IDLE_LIMIT_MS - (Date.now() - lastActivity());
      setIdleSecondsLeft(active && remaining <= IDLE_WARNING_MS ? Math.max(0, Math.ceil(remaining / 1000)) : null);
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
      {idleSecondsLeft !== null && (
        <div role="alert" className="bg-amber-100 border-b border-amber-300 px-6 py-3 flex flex-wrap items-center justify-center gap-3">
          <p className="font-semibold">
            For your security, you will be signed out in {idleSecondsLeft} seconds due to inactivity.
          </p>
          <button type="button" onClick={() => { markActive(); setIdleSecondsLeft(null); }}
            className="bg-[#1B51A4] text-white font-semibold rounded-lg px-4 py-2 cursor-pointer">
            Stay signed in
          </button>
        </div>
      )}
      <div className="bg-white border-b px-6 py-2 flex flex-wrap items-center justify-end gap-3">
        {error && <p role="alert" className="text-red-700 text-sm">{error}</p>}
        <TopBarLinks />
        <NotificationBell />
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