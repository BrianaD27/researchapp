import { useEffect, useState, type FormEvent } from "react";
import { useLocation } from "react-router-dom";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import StudentNavBar from "../../components/common/StudentNavBar";
import PartnerNavBar from "../../components/common/PartnerNavBar";
import { currentRole } from "../../api/session";
import apiClient from "../../api/client";
import {
  REPORT_CATEGORIES, STATUS_LABEL, STATUS_STYLE, reportProblem, type ProblemReport,
} from "../../api/problemReports";

// Any signed-in user can report a problem. Admins get an email and a bell
// alert right away; the reporter sees status changes and replies here.
export default function ReportProblemPage() {
  const location = useLocation();
  const fromPage = (location.state as { from?: string } | null)?.from ?? "";
  const [role, setRole] = useState(currentRole);
  const [category, setCategory] = useState(REPORT_CATEGORIES[0]);
  const [description, setDescription] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [sent, setSent] = useState(false);
  const [mine, setMine] = useState<ProblemReport[]>([]);

  useEffect(() => {
    let active = true;
    apiClient.get<{ role: string }>("/associates/session")
      .then(({ data }) => { if (active) setRole(data.role); })
      .catch(() => undefined);
    apiClient.get<ProblemReport[]>("/reports/mine")
      .then(({ data }) => { if (active) setMine(data); })
      .catch(() => undefined);
    return () => { active = false; };
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setError("");
    if (description.trim().length < 10) {
      setError("Describe the problem in at least 10 characters.");
      return;
    }
    setBusy(true);
    try {
      const { data } = await apiClient.post<ProblemReport>("/reports", {
        category, description: description.trim(), page: fromPage || null,
      });
      setMine((current) => [data, ...current]);
      setDescription("");
      setSent(true);
    } catch (caught) {
      setError(reportProblem(caught, "The report was not sent. Please try again."));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      {role === "PARTNER" ? <PartnerNavBar /> : role === "STUDENT" ? <StudentNavBar /> : <FacultyNavBar />}
      <main className="max-w-3xl mx-auto p-5 flex flex-col gap-5">
        <section className="bg-white rounded-xl shadow-lg p-6 flex flex-col gap-4">
          <h1 className="text-2xl font-bold text-vsu-orange">Report a Problem</h1>
          {sent ? (
            <div role="status" className="flex flex-col gap-3">
              <p>Thanks. Your report was sent to the app admins. You will get a notification when they update it.</p>
              <button type="button" onClick={() => setSent(false)}
                className="self-start text-blue-700 underline cursor-pointer">
                Report something else
              </button>
            </div>
          ) : (
            <form onSubmit={submit} className="flex flex-col gap-4">
              {error && <p role="alert" className="text-red-700">{error}</p>}
              <label className="flex flex-col gap-1">
                What kind of problem?
                <select value={category} disabled={busy} onChange={(event) => setCategory(event.target.value)}
                  className="border border-gray-400 rounded-lg p-3">
                  {REPORT_CATEGORIES.map((item) => <option key={item} value={item}>{item}</option>)}
                </select>
              </label>
              <label className="flex flex-col gap-1">
                What happened?
                <textarea required rows={6} maxLength={2000} value={description} disabled={busy}
                  onChange={(event) => setDescription(event.target.value)}
                  placeholder="What were you trying to do, and what went wrong?"
                  className="border border-gray-400 rounded-lg p-3" />
                <span className="text-xs text-gray-500 self-end">{description.length}/2000</span>
              </label>
              <p className="text-sm text-gray-600">
                We also include {fromPage ? <>the page you were on ({fromPage}), </> : null}
                the time, your browser, and your username so admins can look into it.
              </p>
              <button type="submit" disabled={busy}
                className="bg-blue-700 text-white font-semibold rounded-lg p-3 cursor-pointer disabled:opacity-50">
                {busy ? "Sending..." : "Send Report"}
              </button>
            </form>
          )}
        </section>

        <section className="bg-white rounded-xl shadow-lg p-6 flex flex-col gap-3">
          <h2 className="text-xl font-bold">My reports</h2>
          {mine.length === 0 && <p className="text-gray-600">You have not sent any reports.</p>}
          {mine.map((report) => (
            <article key={report.id} className="border rounded-lg p-4 flex flex-col gap-2">
              <div className="flex flex-wrap items-center gap-2">
                <span className={`text-xs font-semibold rounded-full px-2 py-1 ${STATUS_STYLE[report.status]}`}>
                  {STATUS_LABEL[report.status]}
                </span>
                <span className="font-semibold">{report.category}</span>
                <span className="text-sm text-gray-500">{new Date(report.createdAt).toLocaleString()}</span>
              </div>
              <p className="whitespace-pre-wrap">{report.description}</p>
              {report.adminReply && (
                <p className="bg-gray-100 rounded-lg p-3 text-sm whitespace-pre-wrap">
                  <span className="font-semibold">Admin reply: </span>{report.adminReply}
                </p>
              )}
            </article>
          ))}
        </section>
      </main>
    </div>
  );
}
