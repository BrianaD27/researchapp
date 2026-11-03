import { useCallback, useEffect, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";

interface AssociateView {
  linkId: number;
  userAccountId: number;
  username: string;
  email: string | null;
  canPublish: boolean;
  canReview: boolean;
  projectIds: number[];
}
interface ProjectOption { id: number; title: string }
interface RemovalView {
  id: number;
  opportunityId: number;
  opportunityTitle: string | null;
  requestedBy: string | null;
  reason: string | null;
  status: string;
  outcome: string | null;
  createdAt: string | null;
}
interface ActivityView {
  actor: string;
  action: string;
  detail: string | null;
  createdAt: string | null;
}
interface TeamOverview {
  professorId: number;
  professorName: string;
  associates: AssociateView[];
  pendingRemovals: RemovalView[];
  activity: ActivityView[];
  projects: ProjectOption[];
}
interface Context { admin: boolean; professorId: number | null; associate: boolean }
interface Professor { id: number; name: string }

const actionLabels: Record<string, string> = {
  POSTED_OPPORTUNITY: "Posted a project",
  EDITED_OPPORTUNITY: "Edited a project",
  DECIDED_APPLICATION: "Decided on an applicant",
  UPDATED_APPLICATION: "Updated an application",
  UPDATED_APPLICATION_NOTE: "Wrote a mentor note",
  REQUESTED_REMOVAL: "Asked to remove a project",
};

function problem(caught: unknown, fallback: string) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    const data = caught.response.data as { message?: string; error?: string } | undefined;
    if (caught.response.status === 403) return "Only this professor or an admin can manage this team.";
    if (caught.response.status === 401) return "Your login expired. Log in again.";
    if (data?.message || data?.error) return String(data.message ?? data.error);
  }
  return fallback;
}

function when(value: string | null) {
  return value ? new Date(value).toLocaleString() : "";
}

export default function ProfessorAssociatesPage() {
  const [context, setContext] = useState<Context | null>(null);
  const [professors, setProfessors] = useState<Professor[]>([]);
  const [professorId, setProfessorId] = useState<number | null>(null);
  const [team, setTeam] = useState<TeamOverview | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [username, setUsername] = useState("");
  const [canPublish, setCanPublish] = useState(true);
  const [canReview, setCanReview] = useState(true);
  const [confirmRemove, setConfirmRemove] = useState<number | null>(null);

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const { data } = await apiClient.get<Context>("/faculty-review/context");
        if (!active) return;
        setContext(data);
        if (data.associate) {
          setError("This page is for professors. Research Associates are managed by the professors they work for.");
        } else if (data.admin) {
          const { data: list } = await apiClient.get<Professor[]>("/professors");
          if (!active) return;
          setProfessors(list);
          setProfessorId(data.professorId ?? list[0]?.id ?? null);
        } else if (data.professorId === null) {
          setError("This account has no linked faculty profile yet.");
        } else {
          setProfessorId(data.professorId);
        }
      } catch (caught) {
        if (active) setError(problem(caught, "Could not load this page. Refresh to try again."));
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, []);

  const loadTeam = useCallback(async (id: number) => {
    try {
      const { data } = await apiClient.get<TeamOverview>(`/associates/professor/${id}`);
      setTeam(data);
    } catch (caught) {
      setTeam(null);
      setError(problem(caught, "Could not load the team."));
    }
  }, []);

  useEffect(() => {
    if (professorId === null) return;
    const timer = window.setTimeout(() => void loadTeam(professorId), 0);
    return () => window.clearTimeout(timer);
  }, [professorId, loadTeam]);

  async function run(action: () => Promise<{ data: TeamOverview }>, done: string) {
    if (busy) return;
    setBusy(true);
    setError("");
    setMessage("");
    try {
      const { data } = await action();
      setTeam(data);
      setMessage(done);
    } catch (caught) {
      setError(problem(caught, "That change was not saved."));
    } finally {
      setBusy(false);
      setConfirmRemove(null);
    }
  }

  function add(event: FormEvent) {
    event.preventDefault();
    if (professorId === null || !username.trim()) return;
    void run(() => apiClient.post<TeamOverview>(`/associates/professor/${professorId}`,
      { username: username.trim(), canPublish, canReview }), `${username.trim()} was added to the team.`)
      .then(() => setUsername(""));
  }

  const box = "bg-white rounded-xl p-6";
  const button = "rounded-lg px-4 py-2 text-sm font-semibold cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed";

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-5xl mx-auto p-5 flex flex-col gap-5">
        <h1 className="text-2xl text-white">Research Associates</h1>
        {loading && <p role="status" className="text-white">Loading...</p>}
        {error && <p role="alert" className="bg-white text-red-700 rounded-lg p-4">{error}</p>}
        {message && <p role="status" className="bg-white text-green-800 rounded-lg p-4">{message}</p>}

        {context?.admin && professors.length > 0 && (
          <label className="text-white flex flex-col gap-1 max-w-sm">
            Professor
            <select value={professorId ?? ""} onChange={(event) => { setMessage(""); setError(""); setProfessorId(Number(event.target.value)); }}
              className="text-black bg-white rounded-lg p-2">
              {professors.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
            </select>
          </label>
        )}

        {team && <>
          <section className={box}>
            <h2 className="text-xl font-semibold mb-1">Add an associate to {team.professorName}'s team</h2>
            <p className="text-sm text-gray-600 mb-4">
              The account must already be a Research Associate (an admin sets that on the Users page).
              Associates can work for up to 7 professors and switch between them with "Working for".
            </p>
            <form onSubmit={add} className="flex flex-col sm:flex-row sm:items-end gap-3 flex-wrap">
              <label className="flex flex-col gap-1">
                Username
                <input value={username} onChange={(event) => setUsername(event.target.value)}
                  className="border rounded-lg p-2" autoComplete="off" required />
              </label>
              <label className="flex items-center gap-2">
                <input type="checkbox" checked={canPublish} onChange={(event) => setCanPublish(event.target.checked)} />
                Post and edit projects
              </label>
              <label className="flex items-center gap-2">
                <input type="checkbox" checked={canReview} onChange={(event) => setCanReview(event.target.checked)} />
                Review applicants
              </label>
              <button type="submit" disabled={busy || !username.trim() || (!canPublish && !canReview)}
                className={`${button} bg-blue-700 text-white`}>Add to team</button>
            </form>
          </section>

          <section className={box}>
            <h2 className="text-xl font-semibold mb-3">Team ({team.associates.length})</h2>
            {!team.associates.length && <p className="text-gray-600">No associates yet.</p>}
            <ul className="flex flex-col gap-3">
              {team.associates.map((item) => (
                <li key={item.linkId} className="border rounded-lg p-4 flex flex-col md:flex-row md:flex-wrap md:items-center gap-3 justify-between">
                  <div>
                    <p className="font-semibold">{item.username}</p>
                    {item.email && <p className="text-sm text-gray-600">{item.email}</p>}
                  </div>
                  <div className="flex flex-wrap items-center gap-4">
                    <label className="flex items-center gap-2">
                      <input type="checkbox" checked={item.canPublish} disabled={busy}
                        onChange={(event) => void run(() => apiClient.put<TeamOverview>(`/associates/${item.linkId}`,
                          { canPublish: event.target.checked, canReview: item.canReview }), "Permissions updated.")} />
                      Post and edit projects
                    </label>
                    <label className="flex items-center gap-2">
                      <input type="checkbox" checked={item.canReview} disabled={busy}
                        onChange={(event) => void run(() => apiClient.put<TeamOverview>(`/associates/${item.linkId}`,
                          { canPublish: item.canPublish, canReview: event.target.checked }), "Permissions updated.")} />
                      Review applicants
                    </label>
                    {confirmRemove === item.linkId ? (
                      <>
                        <button type="button" disabled={busy} className={`${button} bg-red-700 text-white`}
                          onClick={() => void run(() => apiClient.delete<TeamOverview>(`/associates/${item.linkId}`),
                            `${item.username} was removed from the team.`)}>Confirm remove</button>
                        <button type="button" className={`${button} border`} onClick={() => setConfirmRemove(null)}>Cancel</button>
                      </>
                    ) : (
                      <button type="button" disabled={busy} className={`${button} border border-red-700 text-red-700`}
                        onClick={() => setConfirmRemove(item.linkId)}>Remove</button>
                    )}
                  </div>
                  {team.projects.length > 0 && (
                    <fieldset className="w-full md:basis-full border-t border-gray-200 pt-3">
                      <legend className="text-sm font-semibold">Works on</legend>
                      <div className="flex flex-wrap gap-x-5 gap-y-2 mt-1">
                        {team.projects.map((project) => (
                          <label key={project.id} className="flex items-center gap-2 text-sm">
                            <input type="checkbox" checked={item.projectIds.includes(project.id)} disabled={busy}
                              onChange={(event) => {
                                const next = event.target.checked
                                  ? [...item.projectIds, project.id]
                                  : item.projectIds.filter((id) => id !== project.id);
                                void run(() => apiClient.put<TeamOverview>(`/associates/${item.linkId}/projects`,
                                  { projectIds: next }), "Projects updated.");
                              }} />
                            {project.title}
                          </label>
                        ))}
                      </div>
                    </fieldset>
                  )}
                </li>
              ))}
            </ul>
          </section>

          <section className={box}>
            <h2 className="text-xl font-semibold mb-3">Removal requests waiting for you ({team.pendingRemovals.length})</h2>
            {!team.pendingRemovals.length && <p className="text-gray-600">Nothing waiting.</p>}
            <ul className="flex flex-col gap-3">
              {team.pendingRemovals.map((item) => (
                <li key={item.id} className="border rounded-lg p-4 flex flex-col gap-2">
                  <p><span className="font-semibold">{item.requestedBy}</span> asked to remove
                    <span className="font-semibold"> "{item.opportunityTitle}"</span></p>
                  {item.reason && <p className="text-gray-700">Reason: {item.reason}</p>}
                  <p className="text-sm text-gray-500">{when(item.createdAt)}</p>
                  <p className="text-sm text-gray-600">
                    If students have applied to or saved it, approving closes the project instead of deleting it.
                  </p>
                  <div className="flex gap-3">
                    <button type="button" disabled={busy} className={`${button} bg-red-700 text-white`}
                      onClick={() => void run(() => apiClient.post<TeamOverview>(`/associates/removal-requests/${item.id}/approve`),
                        "Removal approved.")}>Approve removal</button>
                    <button type="button" disabled={busy} className={`${button} border`}
                      onClick={() => void run(() => apiClient.post<TeamOverview>(`/associates/removal-requests/${item.id}/deny`),
                        "Removal declined. The project stays up.")}>Decline</button>
                  </div>
                </li>
              ))}
            </ul>
          </section>

          <section className={box}>
            <h2 className="text-xl font-semibold mb-3">Recent associate activity</h2>
            {!team.activity.length && <p className="text-gray-600">No activity yet.</p>}
            <ul className="divide-y">
              {team.activity.map((item, index) => (
                <li key={index} className="py-2">
                  <p><span className="font-semibold">{item.actor}</span> {(actionLabels[item.action] ?? item.action).toLowerCase()}
                    {item.detail ? `: ${item.detail}` : ""}</p>
                  <p className="text-sm text-gray-500">{when(item.createdAt)}</p>
                </li>
              ))}
            </ul>
          </section>
        </>}
      </main>
    </div>
  );
}
