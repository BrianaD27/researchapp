import { useEffect, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import { Link, useNavigate } from "react-router-dom";
import AuthNavBar from "../../components/common/AuthNavBar";
import apiClient from "../../api/client";

interface Student {
  id: number;
  name: string;
  email: string;
  major: string;
  classification: string;
  graduationYear: number;
  description: string;
  previousExperience: string | null;
  gpa: number | null;
  availableHoursPerWeek: number | null;
  skills: string[];
}

const initial = {
  name: "", email: "", major: "", classification: "Freshman",
  graduationYear: String(new Date().getFullYear()),
  description: "", previousExperience: "", gpa: "",
  availableHoursPerWeek: "5", skills: "",
};

function fieldsFrom(student: Student) {
  return {
    name: student.name ?? "",
    email: student.email ?? "",
    major: student.major ?? "",
    classification: student.classification ?? "Freshman",
    graduationYear: String(student.graduationYear),
    description: student.description ?? "",
    previousExperience: student.previousExperience ?? "",
    gpa: student.gpa == null ? "" : String(student.gpa),
    availableHoursPerWeek: String(student.availableHoursPerWeek ?? 5),
    skills: (student.skills ?? []).join(", "),
  };
}

export default function StudentInfoPage() {
  const navigate = useNavigate();
  const [fields, setFields] = useState(initial);
  const [id, setId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [ready, setReady] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  function showError(caught: unknown) {
    if (isAxiosError(caught)) {
      const status = caught.response?.status;
      if (status === 401) {
        navigate("/StudentLogin", { replace: true });
        return;
      }
      setError(!caught.response
        ? "Cannot reach the backend. Check that Java is running."
        : `Request failed (HTTP ${status}). Your changes have not been confirmed.`);
    } else {
      setError("Request failed. Your changes have not been confirmed.");
    }
  }

  useEffect(() => {
    let active = true;
    if (!sessionStorage.getItem("researchapp.accessToken")) {
      navigate("/StudentLogin", { replace: true });
      return;
    }
    apiClient.get<Student>("/students/me")
      .then(({ data }) => {
        if (!active) return;
        setId(data.id);
        setFields(fieldsFrom(data));
        setReady(true);
      })
      .catch((caught: unknown) => {
        if (!active) return;
        if (isAxiosError(caught) && caught.response?.status === 404) {
          setReady(true);
        } else if (isAxiosError(caught) && caught.response?.status === 401) {
          navigate("/StudentLogin", { replace: true });
        } else {
          setError("Could not load your profile. Check Java is running, then refresh.");
        }
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [navigate]);

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!ready || saving) return;
    setError("");
    setMessage("");
    const skills = fields.skills.split(",").map((skill) => skill.trim()).filter(Boolean);
    if (!skills.length) {
      setError("Enter at least one skill.");
      return;
    }

    setSaving(true);
    try {
      const body = {
        name: fields.name.trim(),
        email: fields.email.trim().toLowerCase(),
        major: fields.major.trim(),
        classification: fields.classification,
        graduationYear: Number(fields.graduationYear),
        description: fields.description.trim(),
        previousExperience: fields.previousExperience.trim(),
        gpa: fields.gpa === "" ? null : Number(fields.gpa),
        availableHoursPerWeek: Number(fields.availableHoursPerWeek),
        skills,
      };
      const result = id === null
        ? await apiClient.post<Student>("/students", body)
        : await apiClient.put<Student>(`/students/${id}`, body);
      setId(result.data.id);

      const { data } = await apiClient.get<Student>("/students/me");
      setFields(fieldsFrom(data));
      setMessage("Student profile saved and loaded back successfully.");
    } catch (caught) {
      showError(caught);
    } finally {
      setSaving(false);
    }
  }

  const inputs: {
    key: keyof typeof initial; label: string; type?: string;
    min?: number; max?: number; step?: string;
    minLength?: number; maxLength?: number; pattern?: string; required?: boolean;
  }[] = [
    { key: "name", label: "Full name", required: true, minLength: 2, maxLength: 100, pattern: "[a-zA-Z\\s'-]+" },
    { key: "email", label: "Student email (@students.vsu.edu)", type: "email", required: true, pattern: "[A-Za-z0-9._%+-]+@students\\.vsu\\.edu", maxLength: 255 },
    { key: "major", label: "Major", required: true, minLength: 2, maxLength: 100, pattern: "[a-zA-Z\\s,&()\\-]+" },
    { key: "graduationYear", label: "Graduation year", type: "number", min: 2000, max: 2100, required: true },
    { key: "gpa", label: "GPA (optional)", type: "number", min: 0, max: 4, step: "0.01" },
    { key: "availableHoursPerWeek", label: "Available hours per week", type: "number", min: 0, max: 80, required: true },
    { key: "skills", label: "Skills, separated by commas", required: true },
  ];

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <AuthNavBar />
      <main className="max-w-2xl mx-auto p-6">
        <section className="bg-white rounded-xl p-6">
          <h1 className="text-2xl font-bold mb-4">My Student Profile</h1>
          {loading && <p>Loading your profile...</p>}
          {error && <p role="alert" className="text-red-700 mb-4">{error}</p>}
          {message && <p role="status" className="text-green-800 mb-4">{message}</p>}
          {ready && (
            <form onSubmit={save} className="flex flex-col gap-4">
              <fieldset disabled={saving} className="flex flex-col gap-4">
                {inputs.map(({ key, label, ...attributes }) => (
                  <label key={key} className="flex flex-col gap-1">
                    {label}
                    <input
                      {...attributes}
                      readOnly={key === "email" && id !== null}
                      value={fields[key]}
                      onChange={(event) => setFields((current) => ({
                        ...current, [key]: event.target.value,
                      }))}
                      className="border border-gray-400 rounded-lg p-3"
                    />
                  </label>
                ))}
                <label className="flex flex-col gap-1">
                  Classification
                  <select
                    value={fields.classification}
                    onChange={(event) => setFields((current) => ({
                      ...current, classification: event.target.value,
                    }))}
                    className="border rounded-lg p-3"
                  >
                    {["Freshman", "Sophomore", "Junior", "Senior"].map((item) => (
                      <option key={item}>{item}</option>
                    ))}
                  </select>
                </label>
                <label className="flex flex-col gap-1">
                  About you (10-1000 characters)
                  <textarea
                    required minLength={10} maxLength={1000} rows={4}
                    value={fields.description}
                    onChange={(event) => setFields((current) => ({
                      ...current, description: event.target.value,
                    }))}
                    className="border rounded-lg p-3"
                  />
                </label>
                <label className="flex flex-col gap-1">
                  Previous experience (optional)
                  <textarea
                    maxLength={1000} rows={3}
                    value={fields.previousExperience}
                    onChange={(event) => setFields((current) => ({
                      ...current, previousExperience: event.target.value,
                    }))}
                    className="border rounded-lg p-3"
                  />
                </label>
                <button
                  type="submit"
                  className="bg-blue-700 text-white rounded-lg p-3 cursor-pointer"
                >
                  {saving ? "Saving..." : "Save Student Profile"}
                </button>
              </fieldset>
              <Link to="/discover-opportunities" className="text-blue-700 underline">
                Back to opportunities
              </Link>
            </form>
          )}
        </section>
      </main>
    </div>
  );
}