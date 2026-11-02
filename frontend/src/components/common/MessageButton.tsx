import { useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../../api/client";
import { messageProblem, type MessageTargetKind } from "../../api/messages";

// Opens (or starts) the conversation with this person on the Messages page.
// kind says what targetId is: a login account, a student profile, or a professor profile.
export default function MessageButton({ kind, targetId, label = "Message" }: {
  kind: MessageTargetKind;
  targetId: number;
  label?: string;
}) {
  const navigate = useNavigate();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function open() {
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      const { data } = await apiClient.post<{ id: number }>("/messages/start", { kind, id: targetId });
      navigate(`/messages?c=${data.id}`);
    } catch (caught) {
      setError(messageProblem(caught, "Could not open messages."));
      setBusy(false);
    }
  }

  return (
    <span className="inline-flex flex-col gap-1">
      <button type="button" disabled={busy} onClick={() => void open()}
        className="bg-blue-700 text-white rounded-lg px-3 py-1.5 text-sm font-semibold cursor-pointer disabled:opacity-50">
        {busy ? "Opening..." : label}
      </button>
      {error && <span role="alert" className="text-xs text-red-700">{error}</span>}
    </span>
  );
}
