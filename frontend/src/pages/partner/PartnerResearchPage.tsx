import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import PartnerNavBar from "../../components/common/PartnerNavBar";
import apiClient from "../../api/client";

interface Research {
  id: number;
  title: string;
  department: string | null;
  description: string | null;
  professorName: string | null;
  professorDepartment: string | null;
  applicationDeadline: string | null;
  startDate: string | null;
  endDate: string | null;
  requiredMajors: string[];
  requiredSkills: string[];
}

function day(value: string | null) {
  if (!value) return null;
  const [year, month, date] = value.split("-").map(Number);
  return new Date(year, month - 1, date).toLocaleDateString([], { month: "short", day: "numeric", year: "numeric" });
}

// Partner schools (Level 1): browse VSU research, read only.
export default function PartnerResearchPage() {
  const [school, setSchool] = useState("");
  const [items, setItems] = useState<Research[]>([]);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    Promise.all([
      apiClient.get<{ schoolName: string }>("/partner/me"),
      apiClient.get<Research[]>("/partner/research"),
    ])
      .then(([me, research]) => {
        if (!active) return;
        setSchool(me.data.schoolName);
        setItems(research.data);
      })
      .catch((caught) => {
        if (!active) return;
        const reason = isAxiosError(caught) ? (caught.response?.data as { error?: string } | undefined)?.error : undefined;
        setError(reason ?? "Could not load VSU research. Check that Java is running.");
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  const q = query.trim().toLowerCase();
  const visible = items.filter((r) => !q || [r.title, r.department, r.description, r.professorName,
    ...r.requiredMajors, ...r.requiredSkills].join(" ").toLowerCase().includes(q));

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <PartnerNavBar />
      <main className="max-w-5xl mx-auto p-5 flex flex-col gap-4">
        <div className="bg-white rounded-xl shadow-lg p-5 flex flex-col gap-2">
          <h1 className="text-2xl font-bold text-vsu-orange">VSU Research</h1>
          <p className="text-gray-700">
            {school ? <>Welcome, {school} partner. </> : null}
            Here is the research happening at Virginia State University. This view is read only.
          </p>
          <input type="search" value={query} onChange={(event) => setQuery(event.target.value)}
            placeholder="Search by topic, department, professor, major, or skill"
            aria-label="Search research"
            className="border border-gray-400 rounded-lg p-3 mt-2" />
        </div>

        {loading && <p className="text-white">Loading...</p>}
        {error && <p role="alert" className="bg-red-100 text-red-800 rounded-lg p-4">{error}</p>}
        {!loading && !error && visible.length === 0 && (
          <p className="bg-white rounded-lg p-4">{items.length ? "No research matches that search." : "No research posted yet."}</p>
        )}

        {visible.map((r) => (
          <article key={r.id} className="bg-white rounded-xl shadow p-5 flex flex-col gap-2">
            <h2 className="text-xl font-bold">{r.title}</h2>
            <p className="text-sm text-gray-600">
              {[r.professorName, r.department ?? r.professorDepartment].filter(Boolean).join(" | ")}
            </p>
            {r.description && <p className="whitespace-pre-wrap">{r.description}</p>}
            <div className="flex flex-wrap gap-x-6 gap-y-1 text-sm text-gray-700">
              {r.startDate && <span>Starts {day(r.startDate)}</span>}
              {r.endDate && <span>Ends {day(r.endDate)}</span>}
              {r.applicationDeadline && <span>VSU application deadline {day(r.applicationDeadline)}</span>}
            </div>
            {(r.requiredMajors.length > 0 || r.requiredSkills.length > 0) && (
              <div className="flex flex-wrap gap-2">
                {[...r.requiredMajors, ...r.requiredSkills].map((tag, index) => (
                  <span key={`${tag}-${index}`} className="text-xs bg-gray-100 rounded-full px-2 py-1">{tag}</span>
                ))}
              </div>
            )}
          </article>
        ))}
      </main>
    </div>
  );
}
