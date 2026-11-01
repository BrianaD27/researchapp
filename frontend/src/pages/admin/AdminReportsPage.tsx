import { useEffect, useState } from "react";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";
import {
  STATUS_LABEL, STATUS_STYLE, reportProblem, type ProblemReport,
} from "../../api/problemReports";

type Filter = "OPEN" | "ALL" | ProblemReport["status"];

// Admin view of every problem report: change status and reply. The reporter
// gets a bell alert and email when either changes.
export default function AdminReportsPage() {
  const [reports, setReports] = useState<ProblemReport[]>([]);
  const [drafts, setDrafts] = useState<Record<number, { status: ProblemReport["status"]; reply: string }>>({});
  const [filter, setFilter] = useState<Filter>("OPEN");
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  function load(data: ProblemReport[]) {
    setReports(data);
    setDrafts(Object.fromEntries(data.map((r) => [r.id, { status: r.status, reply: r.adminReply ?? "" }])));
  }

  useEffect(() => {
    let active = true;
    apiClient.get<ProblemReport[]>("/reports")
      .then(({ data }) => { if (active) load(data); })
      .catch((caught) => { if (active) setError(reportProblem(caught, "Could not load reports.")); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  async function save(report: ProblemReport) {
    const draft = drafts[report.id];
    if (!draft || busyId !== null) return;
    setBusyId(report.id);
    setError("");
    setMessage("");
    try {
      const { data } = await apiClient.put<ProblemReport>(`/reports/${report.id}`, {
        status: draft.status, reply: draft.reply.trim() || null,
      });
      setReports((current) => current.map((r) => (r.id === data.id ? data : r)));
      setMessage(`Report #${data.id} saved. ${data.reporterUsername} was notified.`);
    } catch (caught) {
      setError(reportProblem(caught, "That change was not saved."));
    } finally {
      setBusyId(null);
    }
  }

  const visible = reports.filter((r) =>
    filter === "ALL" ? true : filter === "OPEN" ? r.status !== "FIXED" : r.status === filter);
  const openCount = reports.filter((r) => r.status !== "FIXED").length;

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-5xl mx-auto p-5 flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl text-white">Problem Reports ({openCount} open)</h1>
          <select value={filter} onChange={(event) => setFilter(event.target.value as Filter)}
            className="rounded-lg px-3 py-2">
            <option value="OPEN">Open (New + In progress)</option>
            <option value="NEW">New</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="FIXED">Fixed</option>
            <option value="ALL">All</option>
          </select>
        </div>
        {error && <p role="alert" className="bg-red-100 text-red-800 rounded-lg p-3">{error}</p>}
        {message && <p role="status" className="bg-green-100 text-green-800 rounded-lg p-3">{message}</p>}
        {loading && <p className="text-white">Loading...</p>}
        {!loading && visible.length === 0 && !error && (
          <p className="bg-white rounded-lg p-4">No reports here.</p>
        )}
        {visible.map((report) => {
          const draft = drafts[report.id] ?? { status: report.status, reply: report.adminReply ?? "" };
          return (
            <article key={report.id} className="bg-white rounded-xl shadow p-5 flex flex-col gap-3">
              <div className="flex flex-wrap items-center gap-2">
                <span className="text-sm text-gray-500">#{report.id}</span>
                <span className={`text-xs font-semibold rounded-full px-2 py-1 ${STATUS_STYLE[report.status]}`}>
                  {STATUS_LABEL[report.status]}
                </span>
                <span className="font-semibold">{report.category}</span>
                <span className="text-sm text-gray-600">from {report.reporterUsername}</span>
                <span className="text-sm text-gray-500">{new Date(report.createdAt).toLocaleString()}</span>
              </div>
              <p className="whitespace-pre-wrap">{report.description}</p>
              <p className="text-xs text-gray-500 break-all">
                Page: {report.page ?? "not recorded"} | Browser: {report.browser ?? "not recorded"}
              </p>
              <div className="flex flex-col md:flex-row gap-3 md:items-end">
                <label className="flex flex-col gap-1 text-sm">
                  Status
                  <select value={draft.status} disabled={busyId !== null}
                    onChange={(event) => setDrafts((d) => ({
                      ...d, [report.id]: { ...draft, status: event.target.value as ProblemReport["status"] },
                    }))}
                    className="border rounded-lg p-2">
                    <option value="NEW">New</option>
                    <option value="IN_PROGRESS">In progress</option>
                    <option value="FIXED">Fixed</option>
                  </select>
                </label>
                <label className="flex flex-col gap-1 text-sm flex-1">
                  Reply to {report.reporterUsername} (optional)
                  <textarea rows={2} maxLength={2000} value={draft.reply} disabled={busyId !== null}
                    onChange={(event) => setDrafts((d) => ({ ...d, [report.id]: { ...draft, reply: event.target.value } }))}
                    className="border rounded-lg p-2" />
                </label>
                <button type="button" disabled={busyId !== null} onClick={() => void save(report)}
                  className="bg-blue-700 text-white font-semibold rounded-lg px-4 py-2 cursor-pointer disabled:opacity-50">
                  {busyId === report.id ? "Saving..." : "Save"}
                </button>
              </div>
            </article>
          );
        })}
      </main>
    </div>
  );
}
