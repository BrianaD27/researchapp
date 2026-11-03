import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import apiClient from "../../api/client";

interface WorkingFor {
  linkId: number;
  professorId: number;
  professorName: string;
  canPublish: boolean;
  canReview: boolean;
}
interface SessionInfo {
  username: string;
  role: string;
  workingFor: WorkingFor[];
}

export const ACTING_FOR_KEY = "researchapp.actingFor";

// Role-specific links in the signed-in top bar: "Working for" for Research
// Associates, the team page for faculty, and the Users page for admins.
export default function TopBarLinks() {
  const [session, setSession] = useState<SessionInfo | null>(null);

  useEffect(() => {
    let active = true;
    apiClient.get<SessionInfo>("/associates/session")
      .then(({ data }) => {
        if (!active) return;
        if (data.role === "ASSOCIATE" && data.workingFor.length) {
          const current = sessionStorage.getItem(ACTING_FOR_KEY);
          if (!data.workingFor.some((item) => String(item.professorId) === current)) {
            sessionStorage.setItem(ACTING_FOR_KEY, String(data.workingFor[0].professorId));
          }
        }
        setSession(data);
      })
      .catch(() => undefined);
    return () => { active = false; };
  }, []);

  if (!session) return null;
  const linkClass = "text-blue-700 font-semibold hover:underline";

  if (session.role === "ASSOCIATE") {
    if (!session.workingFor.length) {
      return <p className="text-sm text-gray-600">Research Associate: not on a professor's team yet</p>;
    }
    const current = sessionStorage.getItem(ACTING_FOR_KEY) ?? String(session.workingFor[0].professorId);
    return (
      <label className="flex items-center gap-2 text-sm">
        <span className="font-semibold">Working for</span>
        <select value={current}
          onChange={(event) => {
            sessionStorage.setItem(ACTING_FOR_KEY, event.target.value);
            window.location.reload();
          }}
          className="border rounded-lg px-2 py-1">
          {session.workingFor.map((item) => (
            <option key={item.linkId} value={item.professorId}>{item.professorName}</option>
          ))}
        </select>
      </label>
    );
  }

  // Research Associates moved into the account menu; admins get one Admin menu.
  if (session.role !== "ADMIN") return null;
  return <AdminMenu linkClass={linkClass} />;
}

// One dropdown for the admin pages instead of five separate links.
function AdminMenu({ linkClass }: { linkClass: string }) {
  const [open, setOpen] = useState(false);
  const box = useRef<HTMLDivElement>(null);

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

  const item = "block px-4 py-2 text-sm hover:bg-gray-100";
  return (
    <div ref={box} className="relative">
      <button type="button" onClick={() => setOpen((v) => !v)} aria-expanded={open} aria-haspopup="menu"
        className={`${linkClass} text-sm cursor-pointer`}>
        Admin
        <span aria-hidden="true"
          className={`inline-block w-2 h-2 ml-2 border-r-2 border-b-2 border-current ${open ? "-rotate-135 translate-y-0.5" : "rotate-45 -translate-y-0.5"}`} />
      </button>
      {open && (
        <div role="menu" onClick={() => setOpen(false)} className="absolute right-0 mt-2 w-52 bg-white border border-gray-300 rounded-lg shadow-lg py-1 z-50">
          <Link role="menuitem" to="/admin-dashboard" className={item}>Dashboard</Link>
          <Link role="menuitem" to="/admin-users" className={item}>Users</Link>
          <Link role="menuitem" to="/admin-reports" className={item}>Problem Reports</Link>
          <Link role="menuitem" to="/admin-conduct" className={item}>Conduct Reports</Link>
          <Link role="menuitem" to="/admin-partners" className={item}>Partner Schools</Link>
        </div>
      )}
    </div>
  );
}
