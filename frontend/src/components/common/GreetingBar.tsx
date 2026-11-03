import { useEffect, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import apiClient from "../../api/client";
import { timeOfDay } from "../../api/greeting";

interface Greeting {
  name: string;
  canChange: boolean;
  preferredName: string | null;
}

// "Good morning, Mr. Hughes" on your own page. Students and associates choose
// the name; faculty are always "Dr. <last name>".
export default function GreetingBar({ className = "" }: { className?: string }) {
  const [greeting, setGreeting] = useState<Greeting | null>(null);
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const now = new Date();

  useEffect(() => {
    let active = true;
    apiClient.get<Greeting>("/me/greeting")
      .then(({ data }) => { if (active) setGreeting(data); })
      .catch(() => undefined);
    return () => { active = false; };
  }, []);

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      const { data } = await apiClient.put<Greeting>("/me/greeting", { preferredName: draft });
      setGreeting(data);
      setEditing(false);
    } catch (caught) {
      const data = isAxiosError(caught) ? caught.response?.data as { error?: string } | undefined : undefined;
      setError(data?.error ?? "Your name was not saved.");
    } finally {
      setBusy(false);
    }
  }

  if (!greeting) return null;
  return (
    <div className={`flex flex-wrap items-end justify-between gap-2 ${className}`}>
      <div>
        <p className="text-sm font-semibold text-gray-600">
          {now.toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric" })}
        </p>
        <p className="text-2xl sm:text-3xl font-bold">{timeOfDay(now.getHours())}, {greeting.name}</p>
      </div>
      {greeting.canChange && !editing && (
        <button type="button" onClick={() => { setDraft(greeting.preferredName ?? ""); setEditing(true); setError(""); }}
          className="text-sm text-[#1B51A4] underline cursor-pointer">
          Change what we call you
        </button>
      )}
      {editing && (
        <form onSubmit={save} className="flex flex-wrap items-center gap-2">
          <label className="flex items-center gap-2 text-sm">
            Call me
            <input autoFocus value={draft} maxLength={60} disabled={busy} placeholder="Mr. Hughes"
              onChange={(event) => setDraft(event.target.value)}
              className="border border-gray-400 rounded-lg px-2 py-1" />
          </label>
          <button type="submit" disabled={busy}
            className="bg-[#1B51A4] text-white text-sm font-semibold rounded-lg px-3 py-1.5 cursor-pointer disabled:opacity-50">
            Save
          </button>
          <button type="button" onClick={() => setEditing(false)} className="text-sm border rounded-lg px-3 py-1.5 cursor-pointer">
            Cancel
          </button>
          <span className="w-full text-xs text-gray-600">Leave it empty to use your profile name.</span>
          {error && <span role="alert" className="w-full text-sm text-red-700">{error}</span>}
        </form>
      )}
    </div>
  );
}
