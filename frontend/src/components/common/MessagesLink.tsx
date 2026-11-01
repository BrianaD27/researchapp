import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import apiClient from "../../api/client";
import { currentRole } from "../../api/session";

// Top bar "Messages" link with an unread count. It also sends the once-a-minute
// "I'm here" ping that powers the green active-now dot.
export default function MessagesLink() {
  const [count, setCount] = useState(0);
  // Partner-school and not-yet-approved faculty accounts don't have messaging.
  const partner = ["PARTNER", "PENDING_FACULTY"].includes(currentRole());

  const refresh = useCallback(async () => {
    try {
      const { data } = await apiClient.get<{ count: number }>("/messages/unread-count");
      setCount(data.count);
    } catch {
      // Quietly skip; the next check will try again.
    }
  }, []);

  useEffect(() => {
    if (partner) return;
    function ping() {
      if (document.visibilityState === "visible") {
        apiClient.post("/messages/heartbeat").catch(() => undefined);
      }
    }
    const first = window.setTimeout(() => { ping(); void refresh(); }, 0);
    const timer = window.setInterval(() => { ping(); void refresh(); }, 60000);
    window.addEventListener("focus", refresh);
    return () => {
      window.clearTimeout(first);
      window.clearInterval(timer);
      window.removeEventListener("focus", refresh);
    };
  }, [refresh, partner]);

  // Partner-school accounts don't have messaging.
  if (partner) return null;
  return (
    <Link to="/messages" className="relative text-sm text-blue-700 font-semibold hover:underline">
      Messages
      {count > 0 && (
        <span className="ml-1 inline-flex items-center justify-center min-w-5 h-5 px-1 rounded-full bg-red-600 text-white text-xs">
          {count > 99 ? "99+" : count}
        </span>
      )}
    </Link>
  );
}
