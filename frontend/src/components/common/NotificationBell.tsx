import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../../api/client";

interface Notice {
  id: number;
  type: string;
  message: string;
  link: string | null;
  read: boolean;
  createdAt: string | null;
}

function timeAgo(value: string | null): string {
  if (!value) return "";
  const minutes = Math.floor((Date.now() - new Date(value).getTime()) / 60000);
  if (minutes < 1) return "Just now";
  if (minutes < 60) return `${minutes} min ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} hr ago`;
  return new Date(value).toLocaleDateString();
}

// Bell in the signed-in top bar. Checks for new alerts every 60 seconds.
export default function NotificationBell() {
  const navigate = useNavigate();
  const [count, setCount] = useState(0);
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<Notice[]>([]);
  const [error, setError] = useState("");
  const box = useRef<HTMLDivElement>(null);

  const refreshCount = useCallback(async () => {
    try {
      const { data } = await apiClient.get<{ count: number }>("/notifications/unread-count");
      setCount(data.count);
    } catch {
      // Quietly skip; the next check will try again.
    }
  }, []);

  useEffect(() => {
    const first = window.setTimeout(() => void refreshCount(), 0);
    const timer = window.setInterval(() => void refreshCount(), 60000);
    window.addEventListener("focus", refreshCount);
    return () => {
      window.clearTimeout(first);
      window.clearInterval(timer);
      window.removeEventListener("focus", refreshCount);
    };
  }, [refreshCount]);

  useEffect(() => {
    if (!open) return;
    function close(event: MouseEvent) {
      if (box.current && !box.current.contains(event.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);

  async function toggle() {
    if (open) {
      setOpen(false);
      return;
    }
    setOpen(true);
    setError("");
    try {
      const { data } = await apiClient.get<Notice[]>("/notifications");
      setItems(data);
    } catch {
      setError("Notifications could not be loaded.");
    }
  }

  async function openNotice(item: Notice) {
    if (!item.read) {
      try {
        await apiClient.post(`/notifications/${item.id}/read`);
      } catch {
        // Still navigate; the item will just stay unread.
      }
      setItems((list) => list.map((n) => (n.id === item.id ? { ...n, read: true } : n)));
      setCount((value) => Math.max(0, value - 1));
    }
    setOpen(false);
    if (item.link && item.link.startsWith("/") && !item.link.startsWith("//")) navigate(item.link);
  }

  async function markAll() {
    try {
      await apiClient.post("/notifications/read-all");
      setItems((list) => list.map((n) => ({ ...n, read: true })));
      setCount(0);
    } catch {
      setError("Could not mark notifications as read.");
    }
  }

  return (
    <div className="relative" ref={box}>
      <button
        type="button"
        onClick={() => void toggle()}
        aria-label={count > 0 ? `Notifications, ${count} unread` : "Notifications"}
        aria-expanded={open}
        className="relative rounded-full p-2 hover:bg-gray-100 cursor-pointer"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        {count > 0 && (
          <span className="absolute -top-0.5 -right-0.5 min-w-5 h-5 px-1 rounded-full bg-orange-500 text-white text-xs font-bold flex items-center justify-center">
            {count > 9 ? "9+" : count}
          </span>
        )}
      </button>
      {open && (
        <div className="absolute right-0 mt-2 w-80 max-w-[90vw] bg-white border rounded-xl shadow-lg z-50">
          <div className="flex items-center justify-between px-4 py-3 border-b">
            <p className="font-semibold">Notifications</p>
            {items.some((n) => !n.read) && (
              <button type="button" onClick={() => void markAll()}
                className="text-sm text-blue-700 hover:underline cursor-pointer">
                Mark all read
              </button>
            )}
          </div>
          {error && <p role="alert" className="px-4 py-3 text-red-700 text-sm">{error}</p>}
          {!error && items.length === 0 && (
            <p className="px-4 py-6 text-sm text-gray-600 text-center">You have no notifications yet.</p>
          )}
          <ul className="max-h-96 overflow-y-auto">
            {items.map((item) => (
              <li key={item.id}>
                <button type="button" onClick={() => void openNotice(item)}
                  className={`w-full text-left px-4 py-3 border-b last:border-b-0 hover:bg-gray-50 cursor-pointer flex gap-3 ${item.read ? "" : "bg-orange-50"}`}>
                  <span className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${item.read ? "bg-transparent" : "bg-orange-500"}`} />
                  <span>
                    <span className="block text-sm">{item.message}</span>
                    <span className="block text-xs text-gray-500 mt-1">{timeAgo(item.createdAt)}</span>
                  </span>
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
