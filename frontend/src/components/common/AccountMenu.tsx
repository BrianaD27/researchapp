import { useEffect, useRef, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { currentRole } from "../../api/session";
import ThemeToggle from "./ThemeToggle";

// One menu for account things: Profile, Research Associates, Dark mode,
// Report a Problem, and Log Out. Messages and the bell stay in the top bar.
export default function AccountMenu({ onLogout, busy }: { onLogout: () => void; busy: boolean }) {
  const [open, setOpen] = useState(false);
  const box = useRef<HTMLDivElement>(null);
  const location = useLocation();
  const role = currentRole();
  const profilePath = role === "STUDENT" ? "/my-profile"
    : role === "ASSOCIATE" ? "/associate-profile"
    : role === "PROFESSOR" || role === "ADMIN" ? "/faculty-profile" : null;

  // Close when clicking elsewhere or pressing Escape.
  useEffect(() => {
    if (!open) return;
    function onClick(event: MouseEvent) {
      if (box.current && !box.current.contains(event.target as Node)) setOpen(false);
    }
    function onKey(event: KeyboardEvent) {
      if (event.key === "Escape") setOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onClick);
      document.removeEventListener("keydown", onKey);
    };
  }, [open]);

  const item = "block w-full text-left px-4 py-2 text-sm hover:bg-gray-100 cursor-pointer";
  return (
    <div ref={box} className="relative">
      <button type="button" onClick={() => setOpen((v) => !v)} aria-expanded={open} aria-haspopup="menu"
        className="text-sm font-semibold border border-gray-400 rounded-lg px-3 py-1.5 cursor-pointer">
        My account
        <span aria-hidden="true"
          className={`inline-block w-2 h-2 ml-2 border-r-2 border-b-2 border-current ${open ? "-rotate-135 translate-y-0.5" : "rotate-45 -translate-y-0.5"}`} />
      </button>
      {open && (
        <div role="menu" onClick={() => setOpen(false)} className="absolute right-0 mt-2 w-56 bg-white border border-gray-300 rounded-lg shadow-lg py-1 z-50">
          {profilePath && <Link role="menuitem" to={profilePath} className={item}>Profile</Link>}
          {(role === "PROFESSOR" || role === "ADMIN") && (
            <Link role="menuitem" to="/faculty-associates" className={item}>Research Associates</Link>
          )}
          <ThemeToggle className={item} />
          <Link role="menuitem" to="/report-problem" state={{ from: location.pathname }} className={item}>
            Report a Problem
          </Link>
          <div className="border-t border-gray-200 my-1" />
          <button type="button" role="menuitem" disabled={busy} onClick={onLogout}
            className={`${item} text-red-700 font-semibold disabled:opacity-50`}>
            {busy ? "Logging out..." : "Log Out"}
          </button>
        </div>
      )}
    </div>
  );
}
