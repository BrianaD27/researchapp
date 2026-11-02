import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import StudentNavBar from "../../components/common/StudentNavBar";
import apiClient from "../../api/client";
import { fit, isRecommended, type FitStudent } from "../../api/match";
import { usePresence } from "../../api/presence";
import ActiveDot from "../../components/common/ActiveDot";
import ResearchTeam from "../../components/common/ResearchTeam";
import UpcomingCard from "../../components/calendar/UpcomingCard";
import MessageButton from "../../components/common/MessageButton";

interface Opportunity {
  id: number;
  title: string;
  description: string | null;
  department: string | null;
  requiredMajors: string[] | null;
  requiredClassifications: string[] | null;
  requiredSkills: string[] | null;
  availability: string | null;
  minimumGpa: number | null;
  applicationDeadline: string | null;
  applicationOpenDate: string | null;
  startDate: string | null;
  endDate: string | null;
  professorName: string | null;
  professorId: number | null;
}

function dateLabel(value: string | null) {
  if (!value) return "Not specified";
  const parts = value.split("-");
  return parts.length === 3 ? `${parts[1]}/${parts[2]}/${parts[0]}` : value;
}

function listLabel(values: string[] | null, fallback: string) {
  return values?.length ? values.join(", ") : fallback;
}

export default function StudentDiscoverPage() {
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  const [studentId, setStudentId] = useState<number | null>(null);
  const [savedIds, setSavedIds] = useState<number[]>([]);
  const [appliedIds, setAppliedIds] = useState<number[]>([]);
  const [actionsReady, setActionsReady] = useState(false);
  const [actionBusy, setActionBusy] = useState(false);
  const [actionError, setActionError] = useState("");
  const [actionMessage, setActionMessage] = useState("");
  const [me, setMe] = useState<FitStudent | null>(null);

  useEffect(() => {
    let active = true;
    setActionsReady(false);
    setActionError("");

    async function loadStudentActions() {
      try {
        const profile = await apiClient.get<{ id: number } & FitStudent>("/students/me");
        if (active) setMe(profile.data);
        const id = profile.data.id;
        const [saved, applications] = await Promise.all([
          apiClient.get<{ opportunityId: number }[]>(`/saved-opportunities/student/${id}`),
          apiClient.get<{ researchOpportunityId: number }[]>(`/applications/student/${id}`),
        ]);
        if (!active) return;
        setStudentId(id);
        setSavedIds(saved.data.map((item) => item.opportunityId));
        setAppliedIds(applications.data.map((item) => item.researchOpportunityId));
        setActionsReady(true);
      } catch (caught) {
        if (!active) return;
        setStudentId(null);
        setActionError(isAxiosError(caught) && caught.response?.status === 404
          ? "Save your student profile first, then return here."
          : "Could not load your saved opportunities and applications. Click Reload to retry.");
      }
    }

    void loadStudentActions();
    return () => { active = false; };
  }, [reload]);

  useEffect(() => {
    setActionMessage("");
    setActionError("");
  }, [selectedId]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    apiClient.get<Opportunity[]>("/research-opportunities")
      .then(({ data }) => {
        if (!Array.isArray(data)) throw new Error("Unexpected response");
        if (!active) return;
        setOpportunities(data);
        setSelectedId((current) =>
          data.some((item) => item.id === current) ? current : data[0]?.id ?? null
        );
      })
      .catch((caught: unknown) => {
        if (!active) return;
        setOpportunities([]);
        setError(isAxiosError(caught) && !caught.response
          ? "Cannot reach the backend. Check that Java is running."
          : "Could not load opportunities. Please try again.");
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [reload]);

  async function changeOpportunity(action: "apply" | "save", opportunityId: number) {
    if (!studentId || !actionsReady || actionBusy) return;
    setActionBusy(true);
    setActionError("");
    setActionMessage("");

    try {
      if (action === "apply") {
        await apiClient.post("/applications", {
          studentId,
          researchOpportunityId: opportunityId,
        });
        const { data } = await apiClient.get<{ researchOpportunityId: number }[]>(
          `/applications/student/${studentId}`
        );
        setAppliedIds(data.map((item) => item.researchOpportunityId));
        setActionMessage("Application submitted and loaded back successfully.");
      } else {
        const removing = savedIds.includes(opportunityId);
        if (removing) {
          await apiClient.delete(
            `/saved-opportunities/student/${studentId}/opportunity/${opportunityId}`
          );
        } else {
          await apiClient.post("/saved-opportunities", {
            studentId,
            opportunityId,
          });
        }
        const { data } = await apiClient.get<{ opportunityId: number }[]>(
          `/saved-opportunities/student/${studentId}`
        );
        setSavedIds(data.map((item) => item.opportunityId));
        setActionMessage(removing ? "Opportunity removed from saved." : "Opportunity saved.");
      }
    } catch (caught) {
      setActionError(isAxiosError(caught) && caught.response?.status === 403
        ? "You do not have permission to change this student's records."
        : "The change could not be confirmed. Click Reload to check its current status before trying again.");
      setActionsReady(false);
    } finally {
      setActionBusy(false);
    }
  }

  const filtered = opportunities.filter((item) => {
    const text = [
      item.title, item.description, item.department, item.professorName,
      ...(item.requiredSkills ?? []), ...(item.requiredMajors ?? []),
      ...(item.requiredClassifications ?? []),
    ].join(" ").toLowerCase();
    return text.includes(search.trim().toLowerCase())
      && (!status || item.availability?.toLowerCase() === status);
  });
  const selected = filtered.find((item) => item.id === selectedId) ?? filtered[0];

  const online = usePresence("professors",
    opportunities.map((item) => item.professorId).filter((id): id is number => typeof id === "number"));

  // Recommended for you: open projects you haven't applied to that fit your
  // major, classification, skills, and GPA.
  const today = new Date().toISOString().slice(0, 10);
  const recommended = me ? opportunities
    .filter((item) => !appliedIds.includes(item.id)
      && item.availability?.toLowerCase() !== "closed"
      && (!item.applicationDeadline || item.applicationDeadline >= today))
    .map((item) => ({ item, result: fit(me, item) }))
    .filter(({ result }) => isRecommended(result))
    .sort((a, b) => b.result.score - a.result.score)
    .slice(0, 3) : [];

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <StudentNavBar />
      <main className="max-w-7xl mx-auto p-5 grid grid-cols-1 lg:grid-cols-2 gap-6">
        <section>
          <h1 className="text-white text-2xl mb-4">Discover Opportunities</h1>
          <form
            onSubmit={(event) => { event.preventDefault(); setSearch(query); }}
            className="flex gap-3 mb-4"
          >
            <input
              aria-label="Search opportunities"
              placeholder="Search opportunities, professors, or skills..."
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              className="min-w-0 flex-1 bg-white border rounded-lg p-3"
            />
            <button className="bg-orange-600 text-white rounded-lg px-5 cursor-pointer">
              Search
            </button>
          </form>

          <div className="flex flex-wrap gap-3 mb-4">
            <select
              aria-label="Opportunity status"
              value={status}
              onChange={(event) => setStatus(event.target.value)}
              className="bg-white border rounded-lg p-3"
            >
              <option value="">All statuses</option>
              <option value="open">Open</option>
              <option value="closed">Closed</option>
            </select>
            <button
              type="button"
              onClick={() => { setQuery(""); setSearch(""); setStatus(""); }}
              className="bg-white rounded-lg px-4 cursor-pointer"
            >
              Clear filters
            </button>
            <button
              type="button"
              disabled={loading}
              onClick={() => setReload((value) => value + 1)}
              className="bg-white rounded-lg px-4 cursor-pointer disabled:opacity-50"
            >
              Reload
            </button>
          </div>

          {loading ? <p role="status" className="text-white">Loading opportunities...</p> : error ? (
            <p role="alert" className="bg-white text-red-700 rounded-lg p-4">{error}</p>
          ) : (
            <>
              {recommended.length > 0 && (
                <div className="bg-white/10 rounded-2xl p-4 mb-4">
                  <h2 className="text-white text-lg font-semibold mb-2">Recommended for you</h2>
                  <div className="flex flex-col gap-2">
                    {recommended.map(({ item, result }) => (
                      <button key={item.id} type="button"
                        onClick={() => { setQuery(""); setSearch(""); setStatus(""); setSelectedId(item.id); }}
                        className="bg-white rounded-xl p-3 text-left cursor-pointer border-2 border-green-500">
                        <p className="font-semibold">{item.title}</p>
                        <p className="text-sm text-gray-600">{item.professorName || "Faculty not specified"}</p>
                        <p className="text-xs text-green-800 mt-1">Why: {result.reasons.join(" | ")}</p>
                      </button>
                    ))}
                  </div>
                </div>
              )}
              <p role="status" className="text-white mb-3">{filtered.length} results</p>
              {!filtered.length && (
                <p className="bg-white rounded-lg p-4">
                  {opportunities.length ? "No opportunities match your search." : "No opportunities have been posted yet."}
                </p>
              )}
              <div className="flex flex-col gap-3">
                {filtered.map((item) => (
                  <button
                    type="button"
                    key={item.id}
                    aria-pressed={selected?.id === item.id}
                    onClick={() => setSelectedId(item.id)}
                    className={`bg-white rounded-2xl p-4 text-left border-4 cursor-pointer ${
                      selected?.id === item.id ? "border-orange-500" : "border-transparent"
                    }`}
                  >
                    <h2 className="text-xl font-semibold">{item.title}</h2>
                    <p className="text-gray-600">
                      {item.professorName || "Faculty not specified"}
                      {item.professorId != null && <> <ActiveDot active={Boolean(online[item.professorId])} label={false} /></>}
                      {" "}| {item.department || "Department not specified"}
                    </p>
                    <p className="font-semibold mt-2">{item.availability || "Status not specified"}</p>
                  </button>
                ))}
              </div>
            </>
          )}
        </section>

        {!loading && !error && selected && (
          <section className="bg-white rounded-xl p-6 self-start">
            <h2 className="text-2xl font-bold mb-2">{selected.title}</h2>
            <p className="flex items-center gap-2">
              {selected.professorName || "Faculty not specified"}
              {selected.professorId != null && <ActiveDot active={Boolean(online[selected.professorId])} />}
              {selected.professorId != null && <MessageButton kind="professor" targetId={selected.professorId} />}
            </p>
            <p className="text-gray-600 mb-5">{selected.department}</p>
            <div className="-mt-3 mb-5"><ResearchTeam professorId={selected.professorId} /></div>
            {selected.professorId != null && (
              <div className="mb-5 bg-[#F4F6FA] rounded-lg p-4"><UpcomingCard professorId={selected.professorId} compact /></div>
            )}
            <dl className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {[
                ["Status", selected.availability || "Not specified"],
                ["Minimum GPA", selected.minimumGpa == null ? "Not specified" : String(selected.minimumGpa)],
                ["Classification", listLabel(selected.requiredClassifications, "Not specified")],
                ["Applications Open", selected.applicationOpenDate ? dateLabel(selected.applicationOpenDate) : "Already open"],
                ["Applications Close", dateLabel(selected.applicationDeadline)],
                ["Project Starts", dateLabel(selected.startDate)],
                ["Project Ends", dateLabel(selected.endDate)],
              ].map(([label, value]) => (
                <div key={label} className="bg-gray-100 rounded-lg p-3">
                  <dt className="text-gray-600">{label}</dt>
                  <dd className="font-semibold">{value}</dd>
                </div>
              ))}
            </dl>
            <h3 className="font-semibold mt-5">Target majors</h3>
            <p>{listLabel(selected.requiredMajors, "Not specified")}</p>
            <h3 className="font-semibold mt-5">Required skills</h3>
            <p>{listLabel(selected.requiredSkills, "Not specified")}</p>
            <div className="border-t mt-5 pt-5">
              {!actionsReady && !actionError && (
                <p role="status">Loading your application and saved status...</p>
              )}
              {actionError && (
                <p role="alert" className="text-red-700 mb-3">{actionError}</p>
              )}
              {actionMessage && (
                <p role="status" className="text-green-800 mb-3">{actionMessage}</p>
              )}
              <div className="flex flex-wrap gap-3">
                <button
                  type="button"
                  disabled={!actionsReady || actionBusy || appliedIds.includes(selected.id)
                    || selected.availability?.toLowerCase() !== "open"
                    || Boolean(selected.applicationOpenDate && selected.applicationOpenDate > new Date().toLocaleDateString("en-CA"))
                    || Boolean(selected.applicationDeadline
                      && selected.applicationDeadline < new Date().toLocaleDateString("en-CA"))}
                  onClick={() => void changeOpportunity("apply", selected.id)}
                  className="bg-green-700 text-white font-semibold rounded-lg px-5 py-3 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {appliedIds.includes(selected.id) ? "Applied" : selected.availability?.toLowerCase() !== "open" || Boolean(selected.applicationDeadline && selected.applicationDeadline < new Date().toLocaleDateString("en-CA")) ? "Applications Closed" : selected.applicationOpenDate && selected.applicationOpenDate > new Date().toLocaleDateString("en-CA") ? `Opens ${dateLabel(selected.applicationOpenDate)}` : "Apply"}
                </button>
                <button
                  type="button"
                  disabled={!actionsReady || actionBusy}
                  onClick={() => void changeOpportunity("save", selected.id)}
                  className="bg-blue-700 text-white font-semibold rounded-lg px-5 py-3 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {savedIds.includes(selected.id) ? "Remove Saved" : "Save Opportunity"}
                </button>
              </div>
              {actionBusy && <p role="status" className="mt-3">Saving changes...</p>}
            </div>
            <h3 className="font-semibold mt-5">Description</h3>
            <p className="whitespace-pre-wrap">{selected.description || "No description provided."}</p>
          </section>
        )}
      </main>
    </div>
  );
}