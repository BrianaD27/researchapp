import { useEffect, useState } from "react";
import apiClient from "./client";

// Green "active now" dots for a list of people. Everyone signed in can see who
// is online (unless that person turned off "Show when I'm active"). Checks once a minute.
export function usePresence(kind: "students" | "professors" | "accounts", ids: number[]): Record<number, boolean> {
  const [online, setOnline] = useState<Record<number, boolean>>({});
  const key = [...new Set(ids)].sort((a, b) => a - b).join(",");

  useEffect(() => {
    if (!key) return;
    let active = true;
    function load() {
      apiClient.get<Record<string, boolean>>(`/messages/presence/${kind}`, { params: { ids: key } })
        .then(({ data }) => {
          if (!active) return;
          const next: Record<number, boolean> = {};
          for (const [id, value] of Object.entries(data)) next[Number(id)] = value;
          setOnline(next);
        })
        .catch(() => undefined);
    }
    const first = window.setTimeout(load, 0);
    const timer = window.setInterval(load, 60000);
    return () => { active = false; window.clearTimeout(first); window.clearInterval(timer); };
  }, [kind, key]);

  return online;
}
