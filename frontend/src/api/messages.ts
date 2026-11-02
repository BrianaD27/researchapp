import { isAxiosError } from "axios";

// Shared by the Messages page and the top bar Messages link.
export interface Conversation {
  id: number;
  otherAccountId: number;
  otherName: string;
  otherDetail: string | null;
  otherActive: boolean;
  lastMessagePreview: string | null;
  lastMessageAt: string | null;
  unread: number;
  blockedByMe: boolean;
}

export interface ChatMessage {
  id: number;
  senderName: string;
  mine: boolean;
  body: string;
  createdAt: string;
}

// Someone found with the "New message" search.
export interface Person {
  accountId: number;
  name: string;
  detail: string | null;
  active: boolean;
  conversationId: number | null;
}

// What a Message button points at: a login account, a student profile, or a professor profile.
export type MessageTargetKind = "account" | "student" | "professor";

export const MESSAGE_MAX = 2000;

export function messageProblem(caught: unknown, fallback: string) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    if (caught.response.status === 401) return "Your login expired. Log in again.";
    if (caught.response.status === 429) return "Too many requests. Wait a minute and try again.";
    const data = caught.response.data as { error?: string } | undefined;
    if (data?.error) return String(data.error);
  }
  return fallback;
}

export function shortTime(value: string | null): string {
  if (!value) return "";
  const date = new Date(value);
  const sameDay = date.toDateString() === new Date().toDateString();
  return sameDay
    ? date.toLocaleTimeString([], { hour: "numeric", minute: "2-digit" })
    : date.toLocaleDateString([], { month: "short", day: "numeric" });
}
