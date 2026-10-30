import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import StudentNavBar from "../../components/common/StudentNavBar";
import apiClient from "../../api/client";

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
  startDate: string | null;
  endDate: string | null;
  professorName: string | null;
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
                      {item.professorName || "Faculty not specified"} | {item.department || "Department not specified"}
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
            <p>{selected.professorName || "Faculty not specified"}</p>
            <p className="text-gray-600 mb-5">{selected.department}</p>
            <dl className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {[
                ["Status", selected.availability || "Not specified"],
                ["Minimum GPA", selected.minimumGpa == null ? "Not specified" : String(selected.minimumGpa)],
                ["Classification", listLabel(selected.requiredClassifications, "Not specified")],
                ["Application deadline", dateLabel(selected.applicationDeadline)],
                ["Start date", dateLabel(selected.startDate)],
                ["End date", dateLabel(selected.endDate)],
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
            <h3 className="font-semibold mt-5">Description</h3>
            <p className="whitespace-pre-wrap">{selected.description || "No description provided."}</p>
          </section>
        )}
      </main>
    </div>
  );
}