import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import ActiveDot from "../../components/common/ActiveDot";
import MessageButton from "../../components/common/MessageButton";
import ProfilePhotoPreview from "../../components/common/ProfilePhotoPreview";
import apiClient from "../../api/client";
import { fit, isRecommended, type Fit } from "../../api/match";
import { usePresence } from "../../api/presence";

interface Student {
  id: number;
  name: string;
  email: string;
  major: string | null;
  classification: string | null;
  graduationYear: number | null;
  gpa: number | null;
  availableHoursPerWeek: number | null;
  description: string | null;
  skills: string[] | null;
  profilePictureUrl: string | null;
}

interface Project {
  id: number;
  title: string;
  professorId: number | null;
  requiredMajors: string[] | null;
  requiredClassifications: string[] | null;
  requiredSkills: string[] | null;
  minimumGpa: number | null;
}

// Discover Students: browse student profiles, see who fits one of your projects
// best ("Recommended"), who is online now, and message them.
export default function ProfessorDiscoverPage() {
  const [students, setStudents] = useState<Student[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [projectId, setProjectId] = useState<number | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const [context, all, opportunities] = await Promise.all([
          apiClient.get<{ admin: boolean; professorId: number | null }>("/faculty-review/context"),
          apiClient.get<Student[]>("/students"),
          apiClient.get<Project[]>("/research-opportunities"),
        ]);
        if (!active) return;
        const own = context.data.admin
          ? opportunities.data
          : opportunities.data.filter((item) => item.professorId === context.data.professorId);
        setStudents(all.data);
        setProjects(own);
        setProjectId(own[0]?.id ?? null);
        setSelectedId(all.data[0]?.id ?? null);
      } catch (caught) {
        if (!active) return;
        setError(isAxiosError(caught) && !caught.response
          ? "Cannot reach the backend. Check that Java is running."
          : "Could not load students. Please try again.");
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, []);

  const project = projects.find((item) => item.id === projectId) ?? null;
  const q = query.trim().toLowerCase();
  const rows: { student: Student; result: Fit | null }[] = students
    .filter((s) => !q || [s.name, s.major, s.classification, ...(s.skills ?? [])].join(" ").toLowerCase().includes(q))
    .map((student) => ({ student, result: project ? fit(student, project) : null }))
    .sort((a, b) => (b.result?.score ?? 0) - (a.result?.score ?? 0) || a.student.name.localeCompare(b.student.name));
  const online = usePresence("students", rows.map((row) => row.student.id));
  const selected = rows.find((row) => row.student.id === selectedId) ?? rows[0] ?? null;

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-7xl mx-auto p-5 grid grid-cols-1 lg:grid-cols-2 gap-6">
        <section className="flex flex-col gap-3">
          <h1 className="text-white text-2xl">Discover Students</h1>
          <input aria-label="Search students" value={query} onChange={(event) => setQuery(event.target.value)}
            placeholder="Search by name, major, classification, or skill..."
            className="bg-white border rounded-lg p-3" />
          {projects.length > 0 && (
            <label className="text-white flex flex-col gap-1">
              Recommend students for
              <select value={projectId ?? ""} onChange={(event) => setProjectId(Number(event.target.value) || null)}
                className="bg-white text-black border rounded-lg p-3">
                {projects.map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}
                <option value="">No project (A to Z)</option>
              </select>
            </label>
          )}

          {loading && <p role="status" className="text-white">Loading students...</p>}
          {error && <p role="alert" className="bg-white text-red-700 rounded-lg p-4">{error}</p>}
          {!loading && !error && <p className="text-white">{rows.length} students</p>}
          {!loading && !error && rows.length === 0 && (
            <p className="bg-white rounded-lg p-4">{students.length ? "No students match that search." : "No student profiles yet."}</p>
          )}

          <div className="flex flex-col gap-2">
            {rows.map(({ student, result }) => {
              const recommended = result ? isRecommended(result) : false;
              return (
                <button key={student.id} type="button" onClick={() => setSelectedId(student.id)}
                  aria-pressed={selected?.student.id === student.id}
                  className={`bg-white rounded-xl p-4 text-left border-4 cursor-pointer ${
                    selected?.student.id === student.id ? "border-orange-500" : "border-transparent"}`}>
                  <div className="flex items-center justify-between gap-2">
                    <h2 className="font-semibold text-lg flex items-center gap-2">
                      {student.name} <ActiveDot active={Boolean(online[student.id])} label={false} />
                    </h2>
                    {recommended && (
                      <span className="text-xs font-semibold rounded-full px-2 py-1 bg-green-100 text-green-900">Recommended</span>
                    )}
                  </div>
                  <p className="text-gray-600">
                    {[student.major, student.classification, student.graduationYear && `Class of ${student.graduationYear}`]
                      .filter(Boolean).join(" | ") || "Profile details not added yet"}
                  </p>
                  {result && result.reasons.length > 0 && (
                    <p className="text-xs text-green-800 mt-1">{result.reasons.join(" | ")}</p>
                  )}
                  {result && !result.eligible && (
                    <p className="text-xs text-gray-500 mt-1">Below this project's minimum GPA</p>
                  )}
                </button>
              );
            })}
          </div>
        </section>

        {selected && (
          <section className="bg-white rounded-xl p-6 self-start flex flex-col gap-3">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <h2 className="text-2xl font-bold">{selected.student.name}</h2>
                {!selected.student.profilePictureUrl && <ActiveDot active={Boolean(online[selected.student.id])} />}
              </div>
              <MessageButton targetId={selected.student.id} />
            </div>
            {selected.student.profilePictureUrl && (
              <ProfilePhotoPreview key={selected.student.id} active={Boolean(online[selected.student.id])}
                src={selected.student.profilePictureUrl} name={selected.student.name} />
            )}
            <p>{selected.student.email}</p>
            <dl className="grid grid-cols-2 gap-3">
              {[
                ["Major", selected.student.major],
                ["Classification", selected.student.classification],
                ["Graduation year", selected.student.graduationYear],
                ["GPA", selected.student.gpa],
                ["Hours available per week", selected.student.availableHoursPerWeek],
              ].map(([label, value]) => (
                <div key={String(label)} className="bg-gray-100 rounded-lg p-3">
                  <dt className="text-gray-600">{label}</dt>
                  <dd className="font-semibold">{value ?? "Not specified"}</dd>
                </div>
              ))}
            </dl>
            {project && selected.result && (
              <div className="bg-green-50 rounded-lg p-3">
                <p className="font-semibold">Fit for {project.title}</p>
                <p className="text-sm">
                  {selected.result.reasons.length ? selected.result.reasons.join(" | ") : "No direct matches with this project's requirements."}
                </p>
              </div>
            )}
            <h3 className="font-semibold">Skills</h3>
            <p>{selected.student.skills?.join(", ") || "Not specified"}</p>
            {selected.student.description?.trim() && <>
              <h3 className="font-semibold">About</h3>
              <p className="whitespace-pre-wrap">{selected.student.description}</p>
            </>}
          </section>
        )}
      </main>
    </div>
  );
}
