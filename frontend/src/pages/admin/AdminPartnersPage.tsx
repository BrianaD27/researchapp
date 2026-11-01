import { useEffect, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";

interface School {
  id: number;
  name: string;
  emailDomain: string;
  active: boolean;
}

function problem(caught: unknown, fallback: string) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    if (caught.response.status === 403) return "Only admins can manage partner schools.";
    const data = caught.response.data as { error?: string } | undefined;
    if (data?.error) return data.error;
  }
  return fallback;
}

// Admins choose which schools can sign up (with their own .edu email) and view VSU research.
export default function AdminPartnersPage() {
  const [schools, setSchools] = useState<School[]>([]);
  const [name, setName] = useState("");
  const [domain, setDomain] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let active = true;
    apiClient.get<School[]>("/admin/partner-schools")
      .then(({ data }) => { if (active) setSchools(data); })
      .catch((caught) => { if (active) setError(problem(caught, "Could not load partner schools.")); });
    return () => { active = false; };
  }, []);

  async function add(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    setMessage("");
    try {
      const { data } = await apiClient.post<School>("/admin/partner-schools", { name, emailDomain: domain });
      setSchools((current) => [...current, data].sort((a, b) => a.name.localeCompare(b.name)));
      setName("");
      setDomain("");
      setMessage(`${data.name} added. People with @${data.emailDomain} emails can now sign up as partners.`);
    } catch (caught) {
      setError(problem(caught, "The school was not added."));
    } finally {
      setBusy(false);
    }
  }

  async function toggle(school: School) {
    if (busy) return;
    setBusy(true);
    setError("");
    setMessage("");
    try {
      const { data } = await apiClient.put<School>(`/admin/partner-schools/${school.id}/active`, { active: !school.active });
      setSchools((current) => current.map((s) => (s.id === data.id ? data : s)));
      setMessage(data.active ? `${data.name} turned on.` : `${data.name} turned off. Its partner accounts can no longer view research.`);
    } catch (caught) {
      setError(problem(caught, "That change was not saved."));
    } finally {
      setBusy(false);
    }
  }

  const input = "border border-gray-400 rounded-lg p-2";
  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-4xl mx-auto p-5 flex flex-col gap-4">
        <h1 className="text-2xl text-white">Partner Schools</h1>
        <p className="text-white text-sm">
          People from a partner school sign up on the Partner School Sign Up page with their school email.
          They can view VSU research only. They cannot apply, message, or see student information.
        </p>
        {error && <p role="alert" className="bg-red-100 text-red-800 rounded-lg p-3">{error}</p>}
        {message && <p role="status" className="bg-green-100 text-green-800 rounded-lg p-3">{message}</p>}

        <form onSubmit={add} className="bg-white rounded-xl shadow p-5 flex flex-col md:flex-row gap-3 md:items-end">
          <label className="flex flex-col gap-1 flex-1">
            School name
            <input required value={name} disabled={busy} onChange={(event) => setName(event.target.value)}
              className={input} />
          </label>
          <label className="flex flex-col gap-1 flex-1">
            Email domain
            <input required value={domain} disabled={busy} onChange={(event) => setDomain(event.target.value)}
              className={input} />
          </label>
          <button type="submit" disabled={busy}
            className="bg-blue-700 text-white font-semibold rounded-lg px-4 py-2 cursor-pointer disabled:opacity-50">
            Add school
          </button>
        </form>

        <section className="bg-white rounded-xl shadow p-5 flex flex-col gap-2">
          {schools.length === 0 && <p className="text-gray-600">No partner schools yet.</p>}
          {schools.map((school) => (
            <div key={school.id} className="flex flex-wrap items-center justify-between gap-3 border-b last:border-b-0 py-2">
              <div>
                <p className="font-semibold">{school.name}</p>
                <p className="text-sm text-gray-600">@{school.emailDomain}</p>
              </div>
              <div className="flex items-center gap-3">
                <span className={`text-xs font-semibold rounded-full px-2 py-1 ${
                  school.active ? "bg-green-100 text-green-900" : "bg-gray-200 text-gray-700"}`}>
                  {school.active ? "On" : "Off"}
                </span>
                <button type="button" disabled={busy} onClick={() => void toggle(school)}
                  className="border rounded-lg px-3 py-1.5 text-sm font-semibold cursor-pointer disabled:opacity-50">
                  {school.active ? "Turn off" : "Turn on"}
                </button>
              </div>
            </div>
          ))}
        </section>
      </main>
    </div>
  );
}
