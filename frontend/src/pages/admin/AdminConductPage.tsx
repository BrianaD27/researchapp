import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";

type Status = "NEW" | "UNDER_REVIEW" | "CLOSED";
type Outcome = "NO_ACTION" | "WARNING_SENT" | "SUSPENDED";

interface ConductReport {
  id: number;
  status: Status;
  outcome: Outcome | null;
  reporterName: string;
  reporterUsername: string | null;
  reportedAccountId: number;
  reportedName: string;
  reportedUsername: string | null;
  reportedRole: string | null;
  reportedActive: boolean;
  messageBody: string | null;
  context: string | null;
  reason: string | null;
  adminNotes: string | null;
  warningSentAt: string | null;
  closedAt: string | null;
  closedBy: string | null;
  createdAt: string;
}

const STATUS_LABEL: Record<Status, string> = { NEW: "New", UNDER_REVIEW: "Under review", CLOSED: "Closed" };
const STATUS_STYLE: Record<Status, string> = {
  NEW: "bg-red-100 text-red-800", UNDER_REVIEW: "bg-amber-100 text-amber-800", CLOSED: "bg-gray-200 text-gray-700",
};
const OUTCOME_LABEL: Record<Outcome, string> = {
  NO_ACTION: "No action needed", WARNING_SENT: "Warning sent", SUSPENDED: "Account suspended",
};

type Filter = "OPEN" | "ALL" | Status;

function problem(caught: unknown, fallback: string) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    const data = caught.response.data as { error?: string } | undefined;
    if (data?.error) return data.error;
  }
  return fallback;
}

// Confidential review of reported messages. Admins only.
export default function AdminConductPage() {
  const [reports, setReports] = useState<ConductReport[]>([]);
  const [drafts, setDrafts] = useState<Record<number, { status: Status; outcome: Outcome | ""; notes: string }>>({});
  const [filter, setFilter] = useState<Filter>("OPEN");
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [confirm, setConfirm] = useState<{ id: number; action: "warn" | "suspend" } | null>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  function draftFor(r: ConductReport) {
    return { status: r.status, outcome: (r.outcome ?? "") as Outcome | "", notes: r.adminNotes ?? "" };
  }

  function replace(updated: ConductReport) {
    setReports((current) => current.map((r) => (r.id === updated.id ? updated : r)));
    setDrafts((d) => ({ ...d, [updated.id]: draftFor(updated) }));
  }

  useEffect(() => {
    let active = true;
    apiClient.get<ConductReport[]>("/admin/conduct-reports")
      .then(({ data }) => {
        if (!active) return;
        setReports(data);
        setDrafts(Object.fromEntries(data.map((r) => [r.id, draftFor(r)])));
      })
      .catch((caught) => { if (active) setError(problem(caught, "Could not load conduct reports.")); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  async function save(r: ConductReport) {
    const draft = drafts[r.id] ?? draftFor(r);
    if (busyId !== null) return;
    if (draft.status === "CLOSED" && !draft.outcome) { setError("Pick an outcome before closing."); return; }
    setBusyId(r.id);
    setError("");
    setMessage("");
    try {
      const { data } = await apiClient.put<ConductReport>(`/admin/conduct-reports/${r.id}`, {
        status: draft.status, outcome: draft.outcome || null, adminNotes: draft.notes,
      });
      replace(data);
      setMessage(data.status === "CLOSED" && r.status !== "CLOSED"
        ? `Report #${data.id} closed. The person who reported it was told it has been reviewed.`
        : `Report #${data.id} saved.`);
    } catch (caught) {
      setError(problem(caught, "That change was not saved."));
    } finally {
      setBusyId(null);
    }
  }

  async function act(id: number, action: "warn" | "suspend") {
    if (busyId !== null) return;
    setBusyId(id);
    setError("");
    setMessage("");
    try {
      const { data } = await apiClient.post<ConductReport>(`/admin/conduct-reports/${id}/${action}`);
      replace(data);
      setConfirm(null);
      setMessage(action === "warn"
        ? `Warning sent to ${data.reportedName}. They were not told who reported them.`
        : `${data.reportedName}'s account is suspended and they were signed out.`);
    } catch (caught) {
      setError(problem(caught, "That action didn't go through."));
    } finally {
      setBusyId(null);
    }
  }

  const visible = reports.filter((r) =>
    filter === "ALL" ? true : filter === "OPEN" ? r.status !== "CLOSED" : r.status === filter);
  const openCount = reports.filter((r) => r.status !== "CLOSED").length;

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-5xl mx-auto p-5 flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-2xl text-white">Conduct Reports ({openCount} open)</h1>
            <p className="text-sm text-blue-100">
              Confidential. Reported people are never told who reported them.
            </p>
          </div>
          <select value={filter} onChange={(event) => setFilter(event.target.value as Filter)}
            aria-label="Show reports" className="rounded-lg px-3 py-2">
            <option value="OPEN">Open (New + Under review)</option>
            <option value="NEW">New</option>
            <option value="UNDER_REVIEW">Under review</option>
            <option value="CLOSED">Closed</option>
            <option value="ALL">All</option>
          </select>
        </div>
        <p className="bg-amber-50 border-l-4 border-amber-400 rounded p-3 text-sm">
          If a report involves harassment or discrimination, follow VSU's Title IX process.
          If someone may be in danger, contact VSU Police.
        </p>
        {error && <p role="alert" className="bg-red-100 text-red-800 rounded-lg p-3">{error}</p>}
        {message && <p role="status" className="bg-green-100 text-green-800 rounded-lg p-3">{message}</p>}
        {loading && <p className="text-white">Loading...</p>}
        {!loading && visible.length === 0 && !error && (
          <p className="bg-white rounded-lg p-4">No conduct reports here.</p>
        )}
        {visible.map((r) => {
          const draft = drafts[r.id] ?? draftFor(r);
          const busy = busyId !== null;
          return (
            <article key={r.id} className="bg-white rounded-xl shadow p-5 flex flex-col gap-3">
              <div className="flex flex-wrap items-center gap-2">
                <span className="text-sm text-gray-500">#{r.id}</span>
                <span className={`text-xs font-semibold rounded-full px-2 py-1 ${STATUS_STYLE[r.status]}`}>
                  {STATUS_LABEL[r.status]}
                </span>
                {r.outcome && r.status === "CLOSED" && (
                  <span className="text-xs font-semibold rounded-full px-2 py-1 bg-blue-100 text-blue-800">
                    {OUTCOME_LABEL[r.outcome]}
                  </span>
                )}
                <span className="text-sm text-gray-500">{new Date(r.createdAt).toLocaleString()}</span>
              </div>
              <dl className="grid sm:grid-cols-2 gap-2 text-sm">
                <div>
                  <dt className="text-gray-600">Reported person</dt>
                  <dd className="font-semibold">
                    {r.reportedName} {r.reportedRole && <span className="font-normal text-gray-600">({r.reportedRole.toLowerCase()})</span>}
                    {!r.reportedActive && <span className="ml-2 text-red-700">Suspended</span>}
                  </dd>
                </div>
                <div>
                  <dt className="text-gray-600">Reported by</dt>
                  <dd className="font-semibold">{r.reporterName}</dd>
                </div>
              </dl>
              <div>
                <p className="text-sm text-gray-600">Reported message</p>
                <p className="bg-red-50 border border-red-200 rounded-lg p-3 whitespace-pre-wrap break-words">{r.messageBody}</p>
              </div>
              {r.reason && <p className="text-sm"><span className="text-gray-600">What happened: </span>{r.reason}</p>}
              {r.context && (
                <details className="text-sm">
                  <summary className="cursor-pointer text-blue-700">Messages just before it</summary>
                  <pre className="whitespace-pre-wrap font-sans bg-gray-50 rounded-lg p-3 mt-2">{r.context}</pre>
                </details>
              )}
              {r.warningSentAt && (
                <p className="text-sm text-gray-600">Warning sent {new Date(r.warningSentAt).toLocaleString()}</p>
              )}
              {r.closedAt && (
                <p className="text-sm text-gray-600">Closed by {r.closedBy} on {new Date(r.closedAt).toLocaleString()}</p>
              )}

              <div className="flex flex-wrap gap-2 border-t pt-3">
                {confirm?.id === r.id ? (
                  <span className="flex flex-wrap items-center gap-2 text-sm">
                    {confirm.action === "warn"
                      ? `Send ${r.reportedName} a conduct warning?`
                      : `Suspend ${r.reportedName}'s account? They'll be signed out and can't log in.`}
                    <button type="button" disabled={busy} onClick={() => void act(r.id, confirm.action)}
                      className="bg-red-700 text-white rounded-lg px-3 py-1.5 cursor-pointer disabled:opacity-50">
                      {confirm.action === "warn" ? "Send warning" : "Suspend"}
                    </button>
                    <button type="button" onClick={() => setConfirm(null)} className="border rounded-lg px-3 py-1.5 cursor-pointer">
                      Cancel
                    </button>
                  </span>
                ) : (
                  <>
                    <button type="button" disabled={busy} onClick={() => setConfirm({ id: r.id, action: "warn" })}
                      className="border border-amber-500 text-amber-800 rounded-lg px-3 py-1.5 text-sm font-semibold cursor-pointer">
                      Send warning
                    </button>
                    {r.reportedActive && r.reportedRole !== "ADMIN" && (
                      <button type="button" disabled={busy} onClick={() => setConfirm({ id: r.id, action: "suspend" })}
                        className="border border-red-500 text-red-700 rounded-lg px-3 py-1.5 text-sm font-semibold cursor-pointer">
                        Suspend account
                      </button>
                    )}
                  </>
                )}
              </div>

              <div className="flex flex-col md:flex-row gap-3 md:items-end">
                <label className="flex flex-col gap-1 text-sm">
                  Status
                  <select value={draft.status} disabled={busy}
                    onChange={(event) => setDrafts((d) => ({ ...d, [r.id]: { ...draft, status: event.target.value as Status } }))}
                    className="border rounded-lg p-2">
                    <option value="NEW">New</option>
                    <option value="UNDER_REVIEW">Under review</option>
                    <option value="CLOSED">Closed</option>
                  </select>
                </label>
                {draft.status === "CLOSED" && (
                  <label className="flex flex-col gap-1 text-sm">
                    Outcome
                    <select value={draft.outcome} disabled={busy}
                      onChange={(event) => setDrafts((d) => ({ ...d, [r.id]: { ...draft, outcome: event.target.value as Outcome } }))}
                      className="border rounded-lg p-2">
                      <option value="">Pick one</option>
                      <option value="NO_ACTION">No action needed</option>
                      <option value="WARNING_SENT">Warning sent</option>
                      <option value="SUSPENDED">Account suspended</option>
                    </select>
                  </label>
                )}
                <label className="flex flex-col gap-1 text-sm flex-1">
                  Admin notes (private, never sent to anyone)
                  <textarea rows={2} maxLength={2000} value={draft.notes} disabled={busy}
                    onChange={(event) => setDrafts((d) => ({ ...d, [r.id]: { ...draft, notes: event.target.value } }))}
                    className="border rounded-lg p-2" />
                </label>
                <button type="button" disabled={busy} onClick={() => void save(r)}
                  className="bg-blue-700 text-white font-semibold rounded-lg px-4 py-2 cursor-pointer disabled:opacity-50">
                  {busyId === r.id ? "Saving..." : "Save"}
                </button>
              </div>
            </article>
          );
        })}
      </main>
    </div>
  );
}
