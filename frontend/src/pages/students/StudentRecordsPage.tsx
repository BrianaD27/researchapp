import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import StudentNavBar from "../../components/common/StudentNavBar";
import apiClient from "../../api/client";
import ActiveDot from "../../components/common/ActiveDot";
import MessageButton from "../../components/common/MessageButton";
import { usePresence } from "../../api/presence";

type Mode = "saved" | "applied" | "ongoing" | "completed";
interface Application {
  id: number;
  researchOpportunityId: number;
  researchOpportunityTitle: string;
  applicationStatus: "PENDING" | "ACCEPTED" | "REJECTED";
  opportunityStatus: string;
  appliedAt: string | null;
}
interface Bookmark {
  id: number;
  opportunityId: number;
  opportunityTitle: string;
  savedAt: string | null;
}
interface Opportunity {
  id: number;
  title: string;
  department: string | null;
  professorName: string | null;
  professorId: number | null;
  description: string | null;
  availability: string | null;
  minimumGpa: number | null;
  requiredMajors: string[] | null;
  requiredSkills: string[] | null;
  requiredClassifications: string[] | null;
  applicationOpenDate: string | null;
  applicationDeadline: string | null;
  startDate: string | null;
  endDate: string | null;
}
interface Row {
  id: number;
  opportunityId: number;
  title: string;
  status: string;
  date: string | null;
  opportunity: Opportunity | null;
  // Tracker step: 1 Submitted, 2 Decision, 3 Ongoing, 4 Completed. Absent for saved items.
  stage?: number;
  notSelected?: boolean;
}

const trackerSteps = ["Submitted", "Decision", "Ongoing", "Completed"];

function ApplicationTracker({ stage, notSelected }: { stage: number; notSelected: boolean }) {
  return (
    <ol className="flex items-start mb-6" aria-label="Application progress">
      {trackerSteps.map((label, index) => {
        const step = index + 1;
        const done = step <= stage;
        const stopped = notSelected && step === 2;
        const name = stopped ? "Not selected" : step === 2 && stage >= 2 ? "Accepted" : label;
        const dot = stopped ? "bg-red-600 border-red-600 text-white"
          : done ? "bg-orange-500 border-orange-500 text-white" : "bg-white border-gray-300 text-gray-400";
        return (
          <li key={label} className="flex-1 flex flex-col items-center text-center relative"
            aria-current={step === stage ? "step" : undefined}>
            {index > 0 && (
              <span className={`absolute top-4 right-1/2 w-full h-0.5 -z-0 ${done ? (stopped ? "bg-red-600" : "bg-orange-500") : "bg-gray-300"}`} />
            )}
            <span className={`relative z-10 h-8 w-8 rounded-full border-2 flex items-center justify-center text-sm font-bold ${dot}`}>
              {stopped ? "x" : step}
            </span>
            <span className={`mt-2 text-xs sm:text-sm ${done || stopped ? "font-semibold" : "text-gray-500"}`}>{name}</span>
          </li>
        );
      })}
    </ol>
  );
}
const titles = {
  saved: "Saved Opportunities",
  applied: "Applied Opportunities",
  ongoing: "Ongoing Research",
  completed: "Completed Research",
};
const decisions = {
  PENDING: "Pending",
  ACCEPTED: "Accepted",
  REJECTED: "Not Selected",
};
function failure(caught: unknown) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    if (caught.response.status === 401) return "Your login expired. Log in again.";
    if (caught.response.status === 403) return "You do not have permission to view or change these records.";
    return `Request failed (HTTP ${caught.response.status}). Refresh the page to try again.`;
  }
  return "Could not load your records. Refresh the page to try again.";
}
function dateLabel(value: string | null) {
  if (!value) return "Not specified";
  const date = value.slice(0, 10).split("-");
  return date.length === 3 ? `${date[1]}/${date[2]}/${date[0]}` : value;
}

export default function StudentRecordsPage({ mode }: { mode: Mode }) {
  const [rows, setRows] = useState<Row[]>([]);
  const [studentId, setStudentId] = useState<number | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [uncertain, setUncertain] = useState(false);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let active = true;
    setLoading(true);
    setRows([]);
    setError("");
    setSelectedId(null);
    setQuery("");
    setStatus("");
    setMessage("");
    setActionError("");
    setUncertain(false);

    async function load() {
      let profileLoaded = false;
      try {
        const { data: profile } = await apiClient.get<{ id: number }>("/students/me");
        profileLoaded = true;
        let records: Omit<Row, "opportunity">[];
        if (mode === "saved") {
          const { data } = await apiClient.get<Bookmark[]>(`/saved-opportunities/student/${profile.id}`);
          records = data.map((item) => ({
            id: item.id, opportunityId: item.opportunityId,
            title: item.opportunityTitle, date: item.savedAt, status: "Saved",
          }));
        } else {
          const { data } = await apiClient.get<Application[]>(`/applications/student/${profile.id}`);
          records = data.filter((item) => {
            const completed = item.opportunityStatus === "COMPLETED"
              && item.applicationStatus === "ACCEPTED";
            const ongoing = item.applicationStatus === "ACCEPTED" && !completed;
            if (mode === "completed") return completed;
            if (mode === "ongoing") return ongoing;
            return !completed;
          }).map((item) => ({
            id: item.id, opportunityId: item.researchOpportunityId,
            title: item.researchOpportunityTitle, date: item.appliedAt,
            status: mode === "completed" ? "Completed"
              : mode === "ongoing" ? "Ongoing" : decisions[item.applicationStatus],
            stage: item.applicationStatus === "ACCEPTED"
              ? (item.opportunityStatus === "COMPLETED" ? 4 : 3)
              : item.applicationStatus === "REJECTED" ? 2 : 1,
            notSelected: item.applicationStatus === "REJECTED",
          }));
        }

        const loaded = await Promise.all(records.map(async (record): Promise<Row> => {
          try {
            const { data } = await apiClient.get<Opportunity>(`/research-opportunities/${record.opportunityId}`);
            return { ...record, opportunity: data };
          } catch (caught) {
            if (isAxiosError(caught) && caught.response?.status === 404) {
              return { ...record, opportunity: null };
            }
            throw caught;
          }
        }));
        if (!active) return;
        setStudentId(profile.id);
        setRows(loaded);
        setSelectedId(loaded[0]?.id ?? null);
      } catch (caught) {
        if (!active) return;
        setError(!profileLoaded && isAxiosError(caught) && caught.response?.status === 404
          ? "Save your student profile first, then return here."
          : failure(caught));
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, [mode]);

  const visible = rows.filter((row) =>
    (!status || row.status === status)
    && [row.title, row.opportunity?.department, row.opportunity?.professorName,
      ...(row.opportunity?.requiredSkills ?? [])]
      .join(" ").toLowerCase().includes(query.trim().toLowerCase())
  );
  const selected = visible.find((row) => row.id === selectedId) ?? visible[0];
  const opportunity = selected?.opportunity;
  // Green dots for the professors of these projects.
  const online = usePresence("professors",
    rows.map((row) => row.opportunity?.professorId).filter((id): id is number => typeof id === "number"));

  async function unsave() {
    if (!selected || studentId === null || busy || uncertain) return;
    const opportunityId = selected.opportunityId;
    setBusy(true);
    setActionError("");
    setMessage("");
    try {
      await apiClient.delete(`/saved-opportunities/student/${studentId}/opportunity/${opportunityId}`);
      const { data } = await apiClient.get<Bookmark[]>(`/saved-opportunities/student/${studentId}`);
      if (data.some((item) => item.opportunityId === opportunityId)) {
        throw new Error("Removal not confirmed");
      }
      setRows((current) => current.filter((row) => row.opportunityId !== opportunityId));
      setMessage("Opportunity removed from saved.");
    } catch (caught) {
      setUncertain(true);
      setActionError(failure(caught) + " Removal has not been confirmed. Refresh before trying again.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <StudentNavBar />
      <main className="max-w-7xl mx-auto p-5">
        <h1 className="text-white text-2xl mb-4">{titles[mode]}</h1>
        {loading && <p role="status" className="text-white">Loading your records...</p>}
        {error && <p role="alert" className="bg-white text-red-700 rounded-lg p-4">{error}</p>}
        {message && <p role="status" className="bg-white text-green-800 rounded-lg p-4 mb-4">{message}</p>}
        {actionError && <p role="alert" className="bg-white text-red-700 rounded-lg p-4 mb-4">{actionError}</p>}
        {!loading && !error && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
            <section>
              <input aria-label="Search your opportunities"
                placeholder="Search by project, faculty, department, or skill..."
                value={query} onChange={(event) => setQuery(event.target.value)}
                className="w-full bg-white border rounded-lg p-3 mb-3" />
              {mode === "applied" && <select aria-label="Application decision" value={status}
                onChange={(event) => setStatus(event.target.value)}
                className="bg-white rounded-lg p-3 mb-3">
                <option value="">All decisions</option>
                <option value="Pending">Pending</option>
                <option value="Accepted">Accepted</option>
                <option value="Not Selected">Not Selected</option>
              </select>}
              <p className="text-white mb-3">{visible.length} results</p>
              {!visible.length && <p className="bg-white rounded-lg p-4">
                {rows.length ? "No records match your search." : mode === "saved"
                  ? "You have not saved any opportunities yet."
                  : mode === "completed" ? "No research has been marked completed yet."
                  : mode === "ongoing" ? "You have no ongoing research right now."
                  : "You have not applied to any opportunities yet."}
              </p>}
              <div className="flex flex-col gap-3">
                {visible.map((row) => (
                  <button key={row.id} type="button" disabled={busy}
                    aria-pressed={selected?.id === row.id}
                    onClick={() => setSelectedId(row.id)}
                    className={`bg-white rounded-xl p-4 text-left border-4 cursor-pointer ${
                      selected?.id === row.id ? "border-orange-500" : "border-transparent"
                    }`}>
                    <h2 className="text-xl font-semibold">{row.title}</h2>
                    <p>{row.opportunity?.professorName}</p>
                    <p>{row.opportunity?.department}</p>
                    <p className="font-semibold mt-2">{row.status}</p>
                    <p className="text-sm text-gray-600">
                      {mode === "saved" ? "Saved" : "Applied"}: {dateLabel(row.date)}
                    </p>
                  </button>
                ))}
              </div>
              <Link to="/discover-opportunities" className="inline-block text-white underline mt-5">
                Discover opportunities
              </Link>
            </section>
            {selected && <section className="bg-white rounded-xl p-6">
              <h2 className="text-2xl font-semibold mb-2">{selected.title}</h2>
              <p className="font-semibold mb-4">{selected.status}</p>
              {selected.stage !== undefined && (
                <ApplicationTracker stage={selected.stage} notSelected={selected.notSelected === true} />
              )}
              {!opportunity ? <p>Project details are no longer available.</p> : <>
                <div className="flex flex-wrap items-center gap-3">
                  <p>{opportunity.professorName || "Faculty not specified"}</p>
                  {opportunity.professorId != null && <ActiveDot active={Boolean(online[opportunity.professorId])} />}
                  {opportunity.professorId != null && mode !== "saved" && (
                    <MessageButton kind="professor" targetId={opportunity.professorId} label="Message professor" />
                  )}
                </div>
                <p className="text-gray-600 mb-4">{opportunity.department}</p>
                <dl className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {[
                    ["Project status", opportunity.availability || "Not specified"],
                    ["Minimum GPA", opportunity.minimumGpa == null ? "Not specified" : String(opportunity.minimumGpa)],
                    ["Applications Open", dateLabel(opportunity.applicationOpenDate)],
                    ["Applications Close", dateLabel(opportunity.applicationDeadline)],
                    ["Project Starts", dateLabel(opportunity.startDate)],
                    ["Project Ends", dateLabel(opportunity.endDate)],
                  ].map(([label, value]) => (
                    <div key={label} className="bg-gray-100 rounded-lg p-3">
                      <dt className="text-gray-600">{label}</dt>
                      <dd className="font-semibold">{value}</dd>
                    </div>
                  ))}
                </dl>
                <h3 className="font-semibold mt-4">Target majors</h3>
                <p>{opportunity.requiredMajors?.join(", ") || "Not specified"}</p>
                <h3 className="font-semibold mt-4">Classifications</h3>
                <p>{opportunity.requiredClassifications?.join(", ") || "Any"}</p>
                <h3 className="font-semibold mt-4">Required skills</h3>
                <p>{opportunity.requiredSkills?.join(", ") || "None specified"}</p>
                <h3 className="font-semibold mt-4">Description</h3>
                <p className="whitespace-pre-wrap">{opportunity.description || "Not provided"}</p>
              </>}
              {mode === "saved" && <button type="button" disabled={busy || uncertain}
                onClick={() => void unsave()}
                className="bg-red-700 text-white font-semibold rounded-lg px-4 py-2 mt-5 cursor-pointer disabled:opacity-50">
                {busy ? "Removing..." : "Remove Saved"}
              </button>}
            </section>}
          </div>
        )}
      </main>
    </div>
  );
}
