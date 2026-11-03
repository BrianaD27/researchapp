import { useEffect, useState } from "react";
import apiClient from "../../api/client";
import { announcePresenceChange } from "../../api/presence";

// "Show when I'm active" switch for your own profile banner. Same setting as on
// the Messages page: when it's off, nobody sees your green dot.
export default function ActiveToggle() {
  const [show, setShow] = useState<boolean | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let active = true;
    apiClient.get<{ showActive: boolean }>("/messages/settings")
      .then(({ data }) => { if (active) setShow(data.showActive); })
      .catch(() => undefined);
    return () => { active = false; };
  }, []);

  async function change(next: boolean) {
    if (busy) return;
    setBusy(true);
    try {
      const { data } = await apiClient.put<{ showActive: boolean }>("/messages/settings", { show: next });
      setShow(data.showActive);
      // Ping first so turning it back on shows the dot right away.
      if (data.showActive) await apiClient.post("/messages/heartbeat").catch(() => undefined);
      announcePresenceChange();
    } catch {
      // Leave the switch where it was if the save failed.
    } finally {
      setBusy(false);
    }
  }

  if (show === null) return null;
  return (
    <label className="flex items-center gap-2 bg-white/15 text-white rounded-lg px-4 py-3 min-h-11 cursor-pointer select-none">
      <input type="checkbox" className="w-4 h-4 cursor-pointer" checked={show} disabled={busy}
        onChange={(event) => void change(event.target.checked)} />
      Show when I'm active
    </label>
  );
}
