import apiClient from "./client";

// Personal calendars for faculty and Research Associates. See CalendarService.java.

export type Visibility = "PUBLIC" | "PRIVATE";
export type CalendarColor = "blue" | "teal" | "purple" | "coral" | "amber" | "gray";

export interface CalendarEntry {
  id: number;
  title: string;
  startDate: string;
  endDate: string | null;
  startTime: string | null;
  endTime: string | null;
  repeatWeekly: boolean;
  repeatUntil: string | null;
  location: string | null;
  notes: string | null;
  color: CalendarColor;
  visibility: Visibility;
  notified: boolean;
  away: boolean;
}

export interface Occurrence {
  entryId: number | null;
  kind: "ENTRY" | "DEADLINE";
  title: string;
  date: string;
  endDate: string | null;
  startTime: string | null;
  endTime: string | null;
  location: string | null;
  notes: string | null;
  color: CalendarColor;
  visibility: Visibility;
  repeatWeekly: boolean;
  opportunityId: number | null;
  away: boolean;
}

export interface MyCalendar {
  entries: CalendarEntry[];
  occurrences: Occurrence[];
  notifiedStudents: number;
}

export interface Upcoming {
  ownerName: string;
  items: Occurrence[];
}

export interface EntryInput {
  title: string;
  startDate: string;
  endDate: string | null;
  startTime: string | null;
  endTime: string | null;
  repeatWeekly: boolean;
  repeatUntil: string | null;
  location: string;
  notes: string;
  color: CalendarColor;
  visibility: Visibility;
  notifyStudents: boolean;
  away: boolean;
}

export const calendarApi = {
  mine: (from: string, to: string) =>
    apiClient.get<MyCalendar>("/calendar/me", { params: { from, to } }).then((r) => r.data),
  create: (input: EntryInput) =>
    apiClient.post<MyCalendar>("/calendar/me/entries", input).then((r) => r.data),
  update: (id: number, input: EntryInput) =>
    apiClient.put<MyCalendar>(`/calendar/me/entries/${id}`, input).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/calendar/me/entries/${id}`),
  forProfessor: (professorId: number) =>
    apiClient.get<Upcoming>(`/calendar/professor/${professorId}/upcoming`).then((r) => r.data),
  forAccount: (accountId: number) =>
    apiClient.get<Upcoming>(`/calendar/account/${accountId}/upcoming`).then((r) => r.data),
};

// Full class names so Tailwind keeps them.
export const COLOR_STYLES: Record<CalendarColor, { pill: string; dot: string; label: string }> = {
  blue: { pill: "bg-blue-100 text-blue-900", dot: "bg-blue-600", label: "Blue" },
  teal: { pill: "bg-teal-100 text-teal-900", dot: "bg-teal-600", label: "Teal" },
  purple: { pill: "bg-purple-100 text-purple-900", dot: "bg-purple-600", label: "Purple" },
  coral: { pill: "bg-orange-100 text-orange-900", dot: "bg-orange-600", label: "Orange" },
  amber: { pill: "bg-amber-100 text-amber-900", dot: "bg-amber-500", label: "Yellow" },
  gray: { pill: "bg-gray-200 text-gray-800", dot: "bg-gray-500", label: "Gray" },
};

export function colorStyle(color: string | null | undefined) {
  return COLOR_STYLES[(color ?? "blue") as CalendarColor] ?? COLOR_STYLES.blue;
}

// "2026-11-03" as a local date (no time-zone shift).
export function parseDay(value: string): Date {
  const [y, m, d] = value.split("-").map(Number);
  return new Date(y, m - 1, d);
}

export function isoDay(date: Date): string {
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${m}-${d}`;
}

// "14:00:00" -> "2:00 PM"
export function formatTime(value: string | null): string {
  if (!value) return "";
  const [h, m] = value.split(":").map(Number);
  const suffix = h >= 12 ? "PM" : "AM";
  return `${h % 12 === 0 ? 12 : h % 12}:${String(m).padStart(2, "0")} ${suffix}`;
}

export function timeRange(item: { startTime: string | null; endTime: string | null }): string {
  if (!item.startTime) return "All day";
  return item.endTime ? `${formatTime(item.startTime)} - ${formatTime(item.endTime)}` : formatTime(item.startTime);
}

// Striped gray background for days blocked out as Away.
export const AWAY_STYLE = {
  backgroundImage: "repeating-linear-gradient(135deg, #E5E7EB 0 6px, #F3F4F6 6px 12px)",
};

// "Nov 20" or "Nov 20 to Nov 27"
export function awayRange(item: { date: string; endDate: string | null }): string {
  const short = { month: "short", day: "numeric" } as const;
  return item.endDate ? `${dayLabel(item.date, short)} to ${dayLabel(item.endDate, short)}` : dayLabel(item.date, short);
}

// The Away block covering today, if any.
export function awayNow<T extends { away: boolean; date: string; endDate: string | null }>(items: T[], today: string): T | undefined {
  return items.find((item) => item.away && item.date <= today && today <= (item.endDate ?? item.date));
}

export function dayLabel(value: string, options: Intl.DateTimeFormatOptions = { weekday: "short", month: "short", day: "numeric" }) {
  return parseDay(value).toLocaleDateString("en-US", options);
}
