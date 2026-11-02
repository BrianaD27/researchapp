import { useEffect, useState } from "react";
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

  if (session.role !== "PROFESSOR" && session.role !== "ADMIN") return null;
  return (
    <nav aria-label="Account tools" className="flex items-center gap-4 text-sm">
      {(session.role === "PROFESSOR" || session.role === "ADMIN") && (
        <Link to="/faculty-associates" className={linkClass}>Research Associates</Link>
      )}
      {session.role === "ADMIN" && <Link to="/admin-dashboard" className={linkClass}>Dashboard</Link>}
      {session.role === "ADMIN" && <Link to="/admin-users" className={linkClass}>Users</Link>}
      {session.role === "ADMIN" && <Link to="/admin-reports" className={linkClass}>Problem Reports</Link>}
      {session.role === "ADMIN" && <Link to="/admin-conduct" className={linkClass}>Conduct Reports</Link>}
      {session.role === "ADMIN" && <Link to="/admin-partners" className={linkClass}>Partner Schools</Link>}
    </nav>
  );
}
