import { useCallback, useEffect, useState } from "react";
import { isAxiosError } from "axios";
import apiClient from "../../api/client";

interface FacultyRequest {
  userAccountId: number;
  username: string;
  email: string;
  name: string;
  department: string | null;
  emailVerified: boolean;
  requestedAt: string | null;
}

// Faculty sign-ups waiting for an admin. Approving makes the account Faculty and
// creates (or links) their faculty profile; declining deactivates the account.
export default function FacultyRequestsPanel({ onChanged }: { onChanged: () => void }) {
  const [items, setItems] = useState<FacultyRequest[]>([]);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const load = useCallback(async () => {
    try {
      const { data } = await apiClient.get<FacultyRequest[]>("/admin/faculty-requests");
      setItems(data);
    } catch {
      setError("Could not load faculty requests.");
    }
  }, []);

  useEffect(() => {
    const first = window.setTimeout(() => void load(), 0);
    return () => window.clearTimeout(first);
  }, [load]);

  async function decide(item: FacultyRequest, action: "approve" | "decline") {
    if (busyId !== null) return;
    if (action === "decline" && !window.confirm(`Decline ${item.name}? Their account will be deactivated.`)) return;
    setBusyId(item.userAccountId);
    setError("");
    setMessage("");
    try {
      await apiClient.post(`/admin/faculty-requests/${item.userAccountId}/${action}`);
      setMessage(action === "approve"
        ? `${item.name} is now Faculty. They were notified and can log in again to start.`
        : `${item.name}'s request was declined and the account deactivated.`);
      await load();
      onChanged();
    } catch (caught) {
      const reason = isAxiosError(caught) ? (caught.response?.data as { error?: string } | undefined)?.error : undefined;
      setError(reason ?? "That change was not saved.");
    } finally {
      setBusyId(null);
    }
  }

  if (!items.length && !error && !message) return null;
  return (
    <section className="bg-white rounded-xl p-5 flex flex-col gap-3 border-l-4 border-amber-400">
      <h2 className="text-lg font-semibold">Faculty access requests ({items.length})</h2>
      {error && <p role="alert" className="text-red-700">{error}</p>}
      {message && <p role="status" className="text-green-800">{message}</p>}
      {items.map((item) => (
        <div key={item.userAccountId} className="flex flex-wrap items-center justify-between gap-3 border rounded-lg p-3">
          <div>
            <p className="font-semibold">{item.name}{item.department ? `, ${item.department}` : ""}</p>
            <p className="text-sm text-gray-600">{item.email} (username {item.username})</p>
            {!item.emailVerified && (
              <p className="text-xs text-amber-800">Waiting for them to click the activation link in their email.</p>
            )}
          </div>
          <div className="flex gap-2">
            <button type="button" disabled={busyId !== null || !item.emailVerified}
              onClick={() => void decide(item, "approve")}
              className="bg-green-700 text-white rounded-lg px-3 py-1.5 text-sm font-semibold cursor-pointer disabled:opacity-50">
              Approve
            </button>
            <button type="button" disabled={busyId !== null}
              onClick={() => void decide(item, "decline")}
              className="border border-red-700 text-red-700 rounded-lg px-3 py-1.5 text-sm font-semibold cursor-pointer disabled:opacity-50">
              Decline
            </button>
          </div>
        </div>
      ))}
    </section>
  );
}
