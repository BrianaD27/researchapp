import { useEffect, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import { Link } from "react-router-dom";
import FacultyNavBar from "../../components/common/FacultyNavBar";
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
  applicationOpenDate: string | null;
  startDate: string | null;
  endDate: string | null;
  professorId: number | null;
  professorName: string | null;
}

function fieldsFrom(item: Opportunity) {
  return {
    title: item.title ?? "",
    description: item.description ?? "",
    department: item.department ?? "",
    requiredMajors: (item.requiredMajors ?? []).join(", "),
    requiredClassifications: (item.requiredClassifications ?? []).join(", "),
    requiredSkills: (item.requiredSkills ?? []).join(", "),
    availability: item.availability ?? "",
    minimumGpa: item.minimumGpa == null ? "" : String(item.minimumGpa),
    applicationDeadline: item.applicationDeadline ?? "",
    applicationOpenDate: item.applicationOpenDate ?? "",
    startDate: item.startDate ?? "",
    endDate: item.endDate ?? "",
  };
}

function errorText(caught: unknown) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    if (caught.response.status === 403) return "Only the owning professor or an administrator can edit this project.";
    if (caught.response.status === 401) return "Your login expired. Log in again.";
    return `Request failed (HTTP ${caught.response.status}). Changes have not been confirmed.`;
  }
  return "The request failed. Changes have not been confirmed.";
}

// Research Associates can't delete projects; they ask the professor, who approves
// or declines on the Research Associates page.
function RemovalRequestBox({ item }: { item: Opportunity }) {
  const [reason, setReason] = useState("");
  const [busy, setBusy] = useState(false);
  const [sent, setSent] = useState(false);
  const [problem, setProblem] = useState("");

  async function send(event: FormEvent) {
    event.preventDefault();
    if (busy || sent) return;
    setBusy(true);
    setProblem("");
    try {
      await apiClient.post("/associates/removal-requests", { opportunityId: item.id, reason: reason.trim() });
      setSent(true);
    } catch (caught) {
      const data = isAxiosError(caught) ? caught.response?.data as { error?: string } | undefined : undefined;
      setProblem(isAxiosError(caught) && caught.response?.status === 403
        ? "You need permission to post and edit projects for this professor to request a removal."
        : data?.error ?? "The request was not sent. Try again.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={send} className="bg-white rounded-xl p-5 mt-4 flex flex-col gap-2">
      <h3 className="font-semibold">Ask the professor to remove this project</h3>
      {sent ? (
        <p role="status" className="text-green-800">Request sent. The professor will approve or decline it.</p>
      ) : <>
        <label className="flex flex-col gap-1 text-sm">
          Reason (optional)
          <textarea value={reason} maxLength={500} rows={2} onChange={(event) => setReason(event.target.value)}
            className="border rounded-lg p-2" />
        </label>
        {problem && <p role="alert" className="text-red-700 text-sm">{problem}</p>}
        <button type="submit" disabled={busy}
          className="self-start rounded-lg px-4 py-2 text-sm font-semibold border border-red-700 text-red-700 cursor-pointer disabled:opacity-50">
          {busy ? "Sending..." : "Request removal"}
        </button>
      </>}
    </form>
  );
}

function splitList(value: string) {
  return [...new Set(value.split(",").map((item) => item.trim()).filter(Boolean))];
}

function Editor({ item, onSaved, onDirty }: {
  item: Opportunity;
  onSaved: (item: Opportunity) => void;
  onDirty: (dirty: boolean) => void;
}) {
  const [fields, setFields] = useState(() => fieldsFrom(item));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const dirty = JSON.stringify(fields) !== JSON.stringify(fieldsFrom(item));

  useEffect(() => { onDirty(dirty || saving); }, [dirty, saving, onDirty]);

  useEffect(() => {
    function warn(event: BeforeUnloadEvent) {
      if (dirty || saving) {
        event.preventDefault();
        event.returnValue = "";
      }
    }
    window.addEventListener("beforeunload", warn);
    return () => window.removeEventListener("beforeunload", warn);
  }, [dirty, saving]);

  function change(key: keyof typeof fields, value: string) {
    setFields((current) => ({ ...current, [key]: value }));
    setMessage("");
  }

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (saving) return;
    setError("");
    setMessage("");
    if (fields.startDate && fields.endDate && fields.startDate > fields.endDate) {
      setError("The end date must be on or after the start date.");
      return;
    }
    if (fields.applicationOpenDate && fields.applicationDeadline
        && fields.applicationOpenDate > fields.applicationDeadline) {
      setError("Applications must open on or before they close.");
      return;
    }
    if (fields.applicationDeadline && fields.startDate
        && fields.applicationDeadline > fields.startDate) {
      setError("Applications must close on or before the project starts.");
      return;
    }
    setSaving(true);
    try {
      await apiClient.put(`/research-opportunities/${item.id}`, {
        title: fields.title.trim(),
        department: fields.department.trim(),
        description: fields.description.trim(),
        availability: fields.availability,
        requiredMajors: splitList(fields.requiredMajors),
        requiredClassifications: splitList(fields.requiredClassifications),
        requiredSkills: splitList(fields.requiredSkills),
        ...(fields.minimumGpa !== "" ? { minimumGpa: Number(fields.minimumGpa) } : {}),
        ...(fields.applicationDeadline ? { applicationDeadline: fields.applicationDeadline } : {}),
        ...(fields.applicationOpenDate ? { applicationOpenDate: fields.applicationOpenDate } : {}),
        ...(fields.startDate ? { startDate: fields.startDate } : {}),
        ...(fields.endDate ? { endDate: fields.endDate } : {}),
      });
      const { data } = await apiClient.get<Opportunity>(`/research-opportunities/${item.id}`);
      setFields(fieldsFrom(data));
      onSaved(data);
      setMessage("Changes saved and loaded back successfully.");
    } catch (caught) {
      setError(errorText(caught));
    } finally {
      setSaving(false);
    }
  }

  const inputClass = "border border-gray-400 rounded-lg p-3 w-full";
  const textInputs: { key: keyof typeof fields; label: string; required?: boolean }[] = [
    { key: "title", label: "Project title", required: true },
    { key: "department", label: "Department", required: true },
    { key: "requiredMajors", label: "Target majors (separate with commas)" },
    { key: "requiredClassifications", label: "Classifications (separate with commas)" },
    { key: "requiredSkills", label: "Required skills (separate with commas)" },
  ];

  return (
    <section className="bg-white rounded-xl p-6">
      <h2 className="text-2xl font-semibold mb-2">Edit Opportunity</h2>
      <p className="text-gray-600 mb-4">Faculty: {item.professorName || "Not specified"}</p>
      {error && <p role="alert" className="text-red-700 mb-4">{error}</p>}
      {message && <p role="status" className="text-green-800 mb-4">{message}</p>}
      <form onSubmit={save}>
        <fieldset disabled={saving} className="flex flex-col gap-4">
          {textInputs.map(({ key, label, required }) => (
            <label key={key} className="flex flex-col gap-1">
              {label}
              <input required={required} maxLength={255}
                value={fields[key]} onChange={(event) => change(key, event.target.value)}
                className={inputClass} />
            </label>
          ))}
          <label className="flex flex-col gap-1">
            Status
            <select required value={fields.availability}
              onChange={(event) => change("availability", event.target.value)}
              className={inputClass}>
              <option value="">Choose status</option>
              {!["", "Open", "Closed"].includes(fields.availability) && (
                <option value={fields.availability}>{fields.availability}</option>
              )}
              <option value="Open">Open</option>
              <option value="Closed">Closed</option>
            </select>
          </label>
          <label className="flex flex-col gap-1">
            Minimum GPA
            <input type="number" min={0} max={4} step="0.01"
              required={item.minimumGpa !== null}
              value={fields.minimumGpa}
              onChange={(event) => change("minimumGpa", event.target.value)}
              className={inputClass} />
          </label>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {([
              ["applicationOpenDate", "Applications Open"],
              ["applicationDeadline", "Applications Close"],
              ["startDate", "Project Starts"],
              ["endDate", "Project Ends"],
            ] as const).map(([key, label]) => (
              <label key={key} className="flex flex-col gap-1">
                {label}
                <input type="date" required={Boolean(item[key])}
                  value={fields[key]} onChange={(event) => change(key, event.target.value)}
                  className={inputClass} />
              </label>
            ))}
          </div>
          <label className="flex flex-col gap-1">
            Description
            <textarea required rows={6} value={fields.description}
              onChange={(event) => change("description", event.target.value)}
              className={inputClass} />
          </label>
          <div className="flex flex-wrap gap-3 items-center">
            <button type="submit" disabled={!dirty || saving || !fields.title.trim() || !fields.department.trim()}
              className="bg-green-700 text-white font-semibold rounded-lg px-4 py-2 cursor-pointer disabled:opacity-50">
              {saving ? "Saving..." : "Save Changes"}
            </button>
            <button type="button" disabled={saving || !dirty}
              onClick={() => { setFields(fieldsFrom(item)); setError(""); setMessage(""); }}
              className="border rounded-lg px-4 py-2 cursor-pointer disabled:opacity-50">
              Cancel Changes
            </button>
          </div>
        </fieldset>
      </form>
    </section>
  );
}

export default function ProfessorViewAllPage() {
  const [items, setItems] = useState<Opportunity[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [dirty, setDirty] = useState(false);
  const [associate, setAssociate] = useState(false);
  const [actingFor, setActingFor] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const { data: context } = await apiClient.get<{
          admin: boolean; professorId: number | null; associate?: boolean; actingForName?: string | null;
        }>("/faculty-review/context");
        if (active) {
          setAssociate(context.associate === true);
          setActingFor(context.actingForName ?? null);
        }
        if (!context.admin && context.professorId === null) {
          if (active) setError(context.associate
            ? "You are not on a professor's team yet. Ask a professor to add you on their Research Associates page."
            : "This account has no linked faculty profile. Ask a faculty administrator to link your account.");
          return;
        }
        const { data } = await apiClient.get<Opportunity[]>(
          context.admin ? "/research-opportunities"
            : `/research-opportunities/professor/${context.professorId}`
        );
        if (!active) return;
        setItems(data);
        setSelectedId(data[0]?.id ?? null);
      } catch (caught) {
        if (active) setError(errorText(caught));
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, []);

  const visible = items.filter((item) =>
    [item.title, item.department, item.professorName, item.description]
      .join(" ").toLowerCase().includes(query.trim().toLowerCase())
  );
  const selected = items.find((item) => item.id === selectedId);

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-7xl mx-auto p-5">
        <h1 className="text-2xl text-white mb-4">
          {associate && actingFor ? `Opportunities for ${actingFor}` : "My Opportunities"}
        </h1>
        {loading && <p role="status" className="text-white">Loading opportunities...</p>}
        {error && <p role="alert" className="bg-white text-red-700 rounded-lg p-4">{error}</p>}
        {!loading && !error && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
            <section>
              <input aria-label="Search your opportunities"
                placeholder="Search your opportunities..."
                value={query} onChange={(event) => setQuery(event.target.value)}
                className="bg-white border rounded-lg p-3 w-full mb-4" />
              <p className="text-white mb-3">{visible.length} opportunities</p>
              {!visible.length && <p className="bg-white rounded-lg p-4">
                {items.length ? "No opportunities match your search." : "No opportunities have been posted for this account."}
              </p>}
              <div className="flex flex-col gap-3">
                {visible.map((item) => (
                  <button key={item.id} type="button" disabled={dirty && item.id !== selectedId}
                    onClick={() => setSelectedId(item.id)}
                    aria-pressed={item.id === selectedId}
                    className={`bg-white rounded-xl border-4 p-4 text-left cursor-pointer disabled:opacity-50 ${
                      item.id === selectedId ? "border-orange-500" : "border-transparent"
                    }`}>
                    <h2 className="text-xl font-semibold">{item.title}</h2>
                    <p>{item.department || "Department not specified"}</p>
                    <p>{item.professorName}</p>
                    <p className="font-semibold mt-2">{item.availability || "Status not specified"}</p>
                  </button>
                ))}
              </div>
              {dirty && <p className="text-white mt-3">Save or cancel your changes before selecting another project.</p>}
              <Link to="/faculty-applicants" className="inline-block text-white underline mt-5">
                Review applicants
              </Link>
            </section>
            {selected && <div>
              <Editor key={selected.id} item={selected} onDirty={setDirty}
                onSaved={(updated) => setItems((current) =>
                  current.map((item) => item.id === updated.id ? updated : item)
                )} />
              {associate && <RemovalRequestBox key={`removal-${selected.id}`} item={selected} />}
            </div>}
          </div>
        )}
      </main>
    </div>
  );
}
