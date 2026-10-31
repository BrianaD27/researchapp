import { useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";

interface Professor { id: number; name: string; email: string }
interface Opportunity { id: number; title: string; availability: string }
const empty = {
  title: "", department: "", description: "", availability: "Open",
  minimumGpa: "", applicationDeadline: "", applicationOpenDate: "", startDate: "", endDate: "",
  majors: "", skills: "",
};
function split(value: string) {
  return [...new Set(value.split(",").map((item) => item.trim()).filter(Boolean))];
}
function localToday() {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}
function errorText(caught: unknown) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    if (caught.response.status === 403) return "You do not have permission to post for this faculty profile.";
    if (caught.response.status === 401) return "Your login expired. Log in again.";
    return `Request failed (HTTP ${caught.response.status}).`;
  }
  return "The request failed.";
}

export default function ProfessorPostPage() {
  const [fields, setFields] = useState(empty);
  const [classifications, setClassifications] = useState<string[]>([]);
  const [professorId, setProfessorId] = useState("");
  const [professors, setProfessors] = useState<Professor[]>([]);
  const [admin, setAdmin] = useState(false);
  const [ready, setReady] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [createdId, setCreatedId] = useState<number | null>(null);
  const [uncertain, setUncertain] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const { data } = await apiClient.get<{ admin: boolean; professorId: number | null }>(
          "/faculty-review/context"
        );
        if (!active) return;
        setAdmin(data.admin);
        if (data.admin) {
          const response = await apiClient.get<Professor[]>("/professors");
          if (!active) return;
          setProfessors(response.data);
          setProfessorId(data.professorId === null ? "" : String(data.professorId));
        } else if (data.professorId !== null) {
          setProfessorId(String(data.professorId));
        } else {
          setError("Your account needs a linked faculty profile before you can post. Ask a faculty administrator to link it.");
          return;
        }
        if (active) setReady(true);
      } catch (caught) {
        if (active) setError(errorText(caught));
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, []);

  function change(key: keyof typeof empty, value: string) {
    setFields((current) => ({ ...current, [key]: value }));
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!ready || saving || createdId !== null || uncertain || !professorId) return;
    setError("");
    setMessage("");
    const majors = split(fields.majors);
    if (!fields.title.trim() || !fields.department.trim() || !fields.description.trim() || !majors.length) {
      setError("Enter a title, department, description, and at least one target major.");
      return;
    }
    if (fields.startDate < localToday()) {
      setError("The start date must be today or later.");
      return;
    }
    if (fields.endDate < fields.startDate) {
      setError("The end date must be on or after the start date.");
      return;
    }
    if (fields.applicationDeadline < localToday() || fields.applicationDeadline > fields.startDate) {
      setError("Choose an application deadline between today and the start date.");
      return;
    }
    if (!fields.applicationOpenDate || fields.applicationOpenDate > fields.applicationDeadline) {
      setError("Applications must open on or before they close.");
      return;
    }
    setSaving(true);
    try {
      const { data } = await apiClient.post<Opportunity>(
        "/research-opportunities",
        {
          title: fields.title.trim(),
          department: fields.department.trim(),
          description: fields.description.trim(),
          requiredMajors: majors,
          requiredSkills: split(fields.skills),
          requiredClassifications: classifications,
          availability: fields.availability,
          minimumGpa: fields.minimumGpa === "" ? null : Number(fields.minimumGpa),
          applicationDeadline: fields.applicationDeadline,
          applicationOpenDate: fields.applicationOpenDate,
          startDate: fields.startDate,
          endDate: fields.endDate,
          researchMediaUrls: [],
        },
        { params: { professorId: Number(professorId) } }
      );
      setCreatedId(data.id);
      setMessage(`Opportunity created: ${data.title}.`);
      try {
        await apiClient.get<Opportunity>(`/research-opportunities/${data.id}`);
        setMessage(`Opportunity created and loaded back successfully: ${data.title}.`);
      } catch {
        setError("The project was created, but the follow-up check failed. Check View All before posting again.");
      }
    } catch (caught) {
      const status = isAxiosError(caught) ? caught.response?.status : undefined;
      const ambiguous = !status || status >= 500;
      setUncertain(ambiguous);
      setError(errorText(caught) + (ambiguous
        ? " Check View All before trying again; the project may have been created."
        : " Check your fields and account permissions before trying again."));
    } finally {
      setSaving(false);
    }
  }

  const locked = saving || createdId !== null || uncertain;
  const inputClass = "w-full bg-gray-100 border border-gray-400 rounded-lg p-3";
  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-7xl mx-auto p-5">
        <h1 className="text-white text-2xl mb-4">Post New Opportunity</h1>
        {loading && <p role="status" className="text-white">Loading faculty account...</p>}
        {error && <p role="alert" className="bg-white text-red-700 rounded-lg p-4 mb-4">{error}</p>}
        {message && <p role="status" className="bg-white text-green-800 rounded-lg p-4 mb-4">{message}</p>}
        {(createdId !== null || uncertain) && (
          <Link to="/faculty-view-all" className="inline-block bg-white text-blue-700 underline rounded-lg p-3 mb-4">
            View saved opportunities
          </Link>
        )}
        {!loading && ready && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
            <form onSubmit={submit} className="bg-white rounded-xl p-6">
              <fieldset disabled={locked} className="flex flex-col gap-4">
                {admin && <label className="flex flex-col gap-1">
                  Faculty owner
                  <select required value={professorId}
                    onChange={(event) => setProfessorId(event.target.value)}
                    className={inputClass}>
                    <option value="">Choose faculty</option>
                    {professors.map((item) => (
                      <option key={item.id} value={String(item.id)}>{item.name} - {item.email}</option>
                    ))}
                  </select>
                </label>}
                {([
                  ["title", "Project title"],
                  ["department", "Department"],
                  ["majors", "Target majors (separate with commas)"],
                  ["skills", "Required skills (separate with commas, optional)"],
                ] as const).map(([key, label]) => (
                  <label key={key} className="flex flex-col gap-1">
                    {label}
                    <input required={key !== "skills"} maxLength={255}
                      value={fields[key]} onChange={(event) => change(key, event.target.value)}
                      className={inputClass} />
                  </label>
                ))}
                <div>
                  <p className="mb-2">Classifications (leave empty for any)</p>
                  <div className="flex flex-wrap gap-3">
                    {["Freshman", "Sophomore", "Junior", "Senior"].map((value) => (
                      <label key={value} className="flex items-center gap-2">
                        <input type="checkbox" checked={classifications.includes(value)}
                          onChange={(event) => setClassifications((current) =>
                            event.target.checked ? [...current, value] : current.filter((item) => item !== value)
                          )} />
                        {value}
                      </label>
                    ))}
                  </div>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-1">
                    Status
                    <select value={fields.availability}
                      onChange={(event) => change("availability", event.target.value)}
                      className={inputClass}>
                      <option value="Open">Open</option>
                      <option value="Closed">Closed</option>
                    </select>
                  </label>
                  <label className="flex flex-col gap-1">
                    Minimum GPA (optional)
                    <input type="number" min={0} max={4} step="0.01" value={fields.minimumGpa}
                      onChange={(event) => change("minimumGpa", event.target.value)}
                      className={inputClass} />
                  </label>
                  {([
                    ["applicationOpenDate", "Applications Open"],
                    ["applicationDeadline", "Applications Close"],
                    ["startDate", "Project Starts"],
                    ["endDate", "Project Ends"],
                  ] as const).map(([key, label]) => (
                    <label key={key} className="flex flex-col gap-1">
                      {label}
                      <input type="date" required min={localToday()} value={fields[key]}
                        onChange={(event) => change(key, event.target.value)} className={inputClass} />
                    </label>
                  ))}
                </div>
                <label className="flex flex-col gap-1">
                  Description
                  <textarea required rows={6} value={fields.description}
                    onChange={(event) => change("description", event.target.value)}
                    className={inputClass} />
                </label>
                <div className="flex flex-wrap gap-3">
                  <button type="submit" disabled={!professorId || locked}
                    className="bg-green-700 text-white rounded-lg px-4 py-2 font-semibold cursor-pointer disabled:opacity-50">
                    {saving ? "Posting..." : createdId !== null ? "Posted" : "Post Opportunity"}
                  </button>
                  <button type="button" onClick={() => {
                    setFields(empty); setClassifications([]); setError(""); setMessage("");
                  }} className="border rounded-lg px-4 py-2 cursor-pointer">Clear Form</button>
                </div>
              </fieldset>
            </form>
            <section className="bg-white rounded-xl p-6">
              <p className="text-gray-600 mb-2">Preview</p>
              <h2 className="text-2xl font-semibold">{fields.title || "Project title"}</h2>
              <p className="mt-2">{fields.department || "Department"}</p>
              <p className="font-semibold mt-3">{fields.availability}</p>
              <dl className="grid grid-cols-2 gap-4 mt-4">
                {[
                  ["Minimum GPA", fields.minimumGpa || "Any"],
                  ["Classifications", classifications.join(", ") || "Any"],
                  ["Applications Open", fields.applicationOpenDate || "Not selected"],
                  ["Applications Close", fields.applicationDeadline || "Not selected"],
                  ["Project Starts", fields.startDate || "Not selected"],
                  ["Project Ends", fields.endDate || "Not selected"],
                ].map(([label, value]) => (
                  <div key={label} className="bg-gray-100 rounded-lg p-3">
                    <dt className="text-gray-600">{label}</dt>
                    <dd className="font-semibold">{value}</dd>
                  </div>
                ))}
              </dl>
              <h3 className="font-semibold mt-4">Target majors</h3>
              <p>{split(fields.majors).join(", ") || "Not entered"}</p>
              <h3 className="font-semibold mt-4">Required skills</h3>
              <p>{split(fields.skills).join(", ") || "None specified"}</p>
              <h3 className="font-semibold mt-4">Description</h3>
              <p className="whitespace-pre-wrap">{fields.description || "Your project description will appear here."}</p>
            </section>
          </div>
        )}
      </main>
    </div>
  );
}
