import { useCallback, useEffect, useMemo, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import { useSearchParams } from "react-router-dom";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import {
  AWAY_STYLE, COLOR_STYLES, calendarApi, colorStyle, dayLabel, isoDay, parseDay, timeRange,
  type CalendarColor, type CalendarEntry, type EntryInput, type MyCalendar, type Occurrence,
} from "../../api/calendar";

const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const MONTHS = ["January", "February", "March", "April", "May", "June", "July", "August",
  "September", "October", "November", "December"];

// Two years back through 2080 (or 10 years ahead, whichever is later).
function yearOptions(shown: number): number[] {
  const now = new Date().getFullYear();
  const last = Math.max(2080, now + 10);
  const years: number[] = [];
  for (let y = now - 2; y <= last; y++) years.push(y);
  return years.includes(shown) ? years : [...years, shown].sort((a, b) => a - b);
}

interface FormState {
  title: string;
  startDate: string;
  endDate: string;
  allDay: boolean;
  startTime: string;
  endTime: string;
  repeatWeekly: boolean;
  repeatUntil: string;
  location: string;
  notes: string;
  color: CalendarColor;
  visibility: "PUBLIC" | "PRIVATE";
  notifyStudents: boolean;
  away: boolean;
}

function blankForm(day: string, away = false): FormState {
  return {
    title: "", startDate: day, endDate: "", allDay: false, startTime: "09:00", endTime: "10:00",
    repeatWeekly: false, repeatUntil: "", location: "", notes: "", color: away ? "gray" : "blue",
    visibility: "PUBLIC", notifyStudents: false, away,
  };
}

function formFrom(entry: CalendarEntry): FormState {
  return {
    title: entry.title, startDate: entry.startDate, endDate: entry.endDate ?? "",
    allDay: !entry.startTime, startTime: entry.startTime?.slice(0, 5) ?? "09:00",
    endTime: entry.endTime?.slice(0, 5) ?? "", repeatWeekly: entry.repeatWeekly,
    repeatUntil: entry.repeatUntil ?? "", location: entry.location ?? "", notes: entry.notes ?? "",
    color: entry.color ?? "blue", visibility: entry.visibility, notifyStudents: false, away: entry.away,
  };
}

function problem(caught: unknown, fallback: string) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    const data = caught.response.data as { error?: string } | undefined;
    if (data?.error) return data.error;
  }
  return fallback;
}

function covers(item: Occurrence, day: string) {
  return item.date === day || (item.endDate !== null && item.date <= day && day <= item.endDate);
}

// Your own calendar: post office hours, announcements, time away, anything.
export default function CalendarPage() {
  const [params, setParams] = useSearchParams();
  const today = isoDay(new Date());
  const [month, setMonth] = useState(() => { const d = new Date(); return new Date(d.getFullYear(), d.getMonth(), 1); });
  const [selected, setSelected] = useState(today);
  const [data, setData] = useState<MyCalendar | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [form, setForm] = useState<FormState | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [busy, setBusy] = useState(false);

  // Six weeks of days starting on the Sunday before the 1st.
  const days = useMemo(() => {
    const start = new Date(month);
    start.setDate(1 - start.getDay());
    return Array.from({ length: 42 }, (_, i) => { const d = new Date(start); d.setDate(start.getDate() + i); return d; });
  }, [month]);
  const from = isoDay(days[0]);
  const to = isoDay(days[41]);

  const load = useCallback(async () => {
    try {
      setData(await calendarApi.mine(from, to));
      setError("");
    } catch (caught) {
      setError(problem(caught, "Could not load your calendar."));
    } finally {
      setLoading(false);
    }
  }, [from, to]);

  useEffect(() => { void load(); }, [load]);

  useEffect(() => {
    if (params.get("add") === "1") {
      setForm(blankForm(today));
      setEditingId(null);
      setParams({}, { replace: true });
    }
  }, [params, setParams, today]);

  const occurrences = data?.occurrences ?? [];
  const dayItems = occurrences.filter((item) => covers(item, selected));
  const monthKey = isoDay(month).slice(0, 7);
  const monthItems = occurrences.filter((item) => item.date.slice(0, 7) === monthKey
    || (item.endDate !== null && item.endDate.slice(0, 7) === monthKey));

  function changeMonth(step: number) {
    setMonth((current) => new Date(current.getFullYear(), current.getMonth() + step, 1));
  }

  function startAdd(day: string, away = false) {
    setForm(blankForm(day, away));
    setEditingId(null);
    setMessage("");
    setError("");
  }

  function startEdit(item: Occurrence) {
    if (item.kind !== "ENTRY" || item.entryId == null) return;
    const entry = data?.entries.find((e) => e.id === item.entryId);
    if (!entry) return;
    setForm(formFrom(entry));
    setEditingId(entry.id);
    setMessage("");
    setError("");
  }

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form || busy) return;
    if (!form.title.trim()) { setError("Add a title."); return; }
    setBusy(true);
    setError("");
    const input: EntryInput = {
      title: form.title,
      startDate: form.startDate,
      endDate: form.endDate || null,
      startTime: form.allDay || form.away ? null : form.startTime || null,
      endTime: form.allDay || form.away ? null : form.endTime || null,
      repeatWeekly: !form.away && form.repeatWeekly,
      repeatUntil: !form.away && form.repeatWeekly && form.repeatUntil ? form.repeatUntil : null,
      location: form.location,
      notes: form.notes,
      color: form.away ? "gray" : form.color,
      visibility: form.visibility,
      away: form.away,
      notifyStudents: editingId === null && form.visibility === "PUBLIC" && form.notifyStudents,
    };
    try {
      const result = editingId === null ? await calendarApi.create(input) : await calendarApi.update(editingId, input);
      setForm(null);
      setEditingId(null);
      setSelected(input.startDate);
      const start = parseDay(input.startDate);
      setMonth(new Date(start.getFullYear(), start.getMonth(), 1));
      setData(result);
      setMessage(input.notifyStudents
        ? `Saved. ${result.notifiedStudents} student${result.notifiedStudents === 1 ? "" : "s"} notified.`
        : "Saved.");
    } catch (caught) {
      setError(problem(caught, "The entry was not saved."));
    } finally {
      setBusy(false);
    }
  }

  async function remove() {
    if (editingId === null || busy) return;
    if (!window.confirm("Delete this entry? If it repeats weekly, every week is deleted.")) return;
    setBusy(true);
    try {
      await calendarApi.remove(editingId);
      setForm(null);
      setEditingId(null);
      setMessage("Deleted.");
      await load();
    } catch (caught) {
      setError(problem(caught, "The entry was not deleted."));
    } finally {
      setBusy(false);
    }
  }

  const input = "w-full border border-gray-400 rounded-lg p-2";
  const monthName = month.toLocaleDateString("en-US", { month: "long", year: "numeric" });

  function itemRow(item: Occurrence, index: number, showDay: boolean) {
    const style = colorStyle(item.color);
    const editable = item.kind === "ENTRY";
    const body = (
      <>
        <span className={`w-3 h-3 rounded-full mt-1.5 shrink-0 ${style.dot}`} aria-hidden="true" />
        <span className="flex flex-col min-w-0 text-left">
          <span className="font-semibold break-words">
            {item.away && <span className="bg-gray-700 text-white text-xs font-bold rounded px-1.5 py-0.5 mr-2 align-middle">AWAY</span>}
            {item.title}
          </span>
          <span className="text-sm text-gray-600 break-words">
            {[
              showDay ? dayLabel(item.date) : null,
              item.endDate ? `through ${dayLabel(item.endDate)}` : item.away ? "All day" : timeRange(item),
              item.location,
              item.repeatWeekly ? "Weekly" : null,
              item.kind === "DEADLINE" ? "From your opportunities" : item.visibility === "PRIVATE" ? "Just me" : "Everyone",
            ].filter(Boolean).join(" \u00B7 ")}
          </span>
          {item.notes && <span className="text-sm text-gray-700 whitespace-pre-wrap break-words">{item.notes}</span>}
        </span>
      </>
    );
    const key = `${item.kind}-${item.entryId ?? item.opportunityId}-${item.date}-${index}`;
    return editable ? (
      <li key={key}>
        <button type="button" onClick={() => startEdit(item)}
          className="w-full flex gap-3 p-2 rounded-lg hover:bg-gray-100 cursor-pointer" title="Edit this entry">
          {body}
        </button>
      </li>
    ) : (
      <li key={key} className="flex gap-3 p-2">{body}</li>
    );
  }

  return (
    <div className="min-h-screen bg-[#F4F6FA]">
      <FacultyNavBar />
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-8 flex flex-col gap-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-3xl font-bold">Your calendar</h1>
            <p className="text-gray-600">Post office hours, announcements, time away, or anything else. "Everyone" entries show on your profile.</p>
          </div>
          <div className="flex flex-wrap gap-3">
            <button type="button" onClick={() => startAdd(selected, true)}
              className="border-2 border-gray-700 text-gray-800 font-semibold rounded-lg px-5 py-3 cursor-pointer bg-white">
              Block out days
            </button>
            <button type="button" onClick={() => startAdd(selected)}
              className="bg-[#1B51A4] text-white font-semibold rounded-lg px-5 py-3 cursor-pointer">
              + Add entry
            </button>
          </div>
        </div>

        {message && <p role="status" className="bg-green-100 text-green-800 rounded-lg p-3">{message}</p>}
        {error && <p role="alert" className="bg-red-50 text-red-700 rounded-lg p-3">{error}</p>}

        {form && (
          <section className="bg-white rounded-xl p-6 flex flex-col gap-4 border-l-4 border-[#1B51A4]">
            <h2 className="text-xl font-semibold">
              {form.away ? (editingId === null ? "Block out days" : "Edit blocked-out days")
                : editingId === null ? "Add an entry" : "Edit entry"}
            </h2>
            <form onSubmit={save} className="flex flex-col gap-4">
              <label className="flex items-start gap-2 bg-gray-100 rounded-lg p-3">
                <input type="checkbox" className="mt-1" checked={form.away} disabled={busy}
                  onChange={(e) => setForm({ ...form, away: e.target.checked, color: e.target.checked ? "gray" : "blue" })} />
                <span>
                  <span className="font-semibold">Block out these days (I'm away)</span>
                  <span className="block text-sm text-gray-600">
                    The whole day or days show as unavailable on your calendar and profile, with the reason below.
                  </span>
                </span>
              </label>
              <label className="flex flex-col gap-1">{form.away ? "Reason students will see" : "Title"}
                <input maxLength={150} value={form.title} disabled={busy} className={input}
                  placeholder={form.away ? "Out of town for field research" : "Office hours"}
                  onChange={(e) => setForm({ ...form, title: e.target.value })} />
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <label className="flex flex-col gap-1">{form.away ? "First day away" : "Date"}
                  <input type="date" required value={form.startDate} disabled={busy} className={input}
                    onChange={(e) => setForm({ ...form, startDate: e.target.value })} />
                </label>
                <label className="flex flex-col gap-1">{form.away ? "Last day away" : "Last day (only if it spans several days)"}
                  <input type="date" value={form.endDate} min={form.startDate} disabled={busy} className={input}
                    onChange={(e) => setForm({ ...form, endDate: e.target.value })} />
                </label>
              </div>
              {!form.away && (
                <label className="flex items-center gap-2">
                  <input type="checkbox" checked={form.allDay} disabled={busy}
                    onChange={(e) => setForm({ ...form, allDay: e.target.checked })} />
                  All day
                </label>
              )}
              {!form.allDay && !form.away && (
                <div className="grid grid-cols-2 gap-4">
                  <label className="flex flex-col gap-1">Starts
                    <input type="time" required value={form.startTime} disabled={busy} className={input}
                      onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
                  </label>
                  <label className="flex flex-col gap-1">Ends
                    <input type="time" value={form.endTime} disabled={busy} className={input}
                      onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
                  </label>
                </div>
              )}
              {!form.away && <div className="flex flex-wrap items-center gap-4">
                <label className="flex items-center gap-2">
                  <input type="checkbox" checked={form.repeatWeekly} disabled={busy}
                    onChange={(e) => setForm({ ...form, repeatWeekly: e.target.checked })} />
                  Repeat every week
                </label>
                {form.repeatWeekly && (
                  <label className="flex items-center gap-2">Until (optional)
                    <input type="date" value={form.repeatUntil} min={form.startDate} disabled={busy}
                      className="border border-gray-400 rounded-lg p-2"
                      onChange={(e) => setForm({ ...form, repeatUntil: e.target.value })} />
                  </label>
                )}
              </div>}
              <label className="flex flex-col gap-1">Place or link (optional)
                <input maxLength={300} value={form.location} disabled={busy} className={input}
                  placeholder="Room 210, Hunter McDaniel Hall"
                  onChange={(e) => setForm({ ...form, location: e.target.value })} />
              </label>
              <label className="flex flex-col gap-1">Notes (optional)
                <textarea rows={3} maxLength={2000} value={form.notes} disabled={busy} className={input}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })} />
              </label>
              {!form.away && <fieldset className="flex flex-col gap-2">
                <legend className="mb-1">Color</legend>
                <div className="flex flex-wrap gap-2">
                  {(Object.keys(COLOR_STYLES) as CalendarColor[]).map((color) => (
                    <label key={color}
                      className={`flex items-center gap-2 rounded-full px-3 py-1 cursor-pointer border-2 ${COLOR_STYLES[color].pill} ${form.color === color ? "border-gray-800" : "border-transparent"}`}>
                      <input type="radio" name="color" className="sr-only" checked={form.color === color} disabled={busy}
                        onChange={() => setForm({ ...form, color })} />
                      <span className={`w-3 h-3 rounded-full ${COLOR_STYLES[color].dot}`} aria-hidden="true" />
                      {COLOR_STYLES[color].label}
                    </label>
                  ))}
                </div>
              </fieldset>}
              <fieldset className="flex flex-col gap-2">
                <legend className="mb-1">Who can see this</legend>
                <div className="flex flex-wrap gap-4">
                  <label className="flex items-center gap-2">
                    <input type="radio" name="visibility" checked={form.visibility === "PUBLIC"} disabled={busy}
                      onChange={() => setForm({ ...form, visibility: "PUBLIC" })} />
                    Everyone (shows on your profile)
                  </label>
                  <label className="flex items-center gap-2">
                    <input type="radio" name="visibility" checked={form.visibility === "PRIVATE"} disabled={busy}
                      onChange={() => setForm({ ...form, visibility: "PRIVATE", notifyStudents: false })} />
                    Just me
                  </label>
                </div>
              </fieldset>
              {editingId === null && form.visibility === "PUBLIC" && (
                <label className="flex items-center gap-2">
                  <input type="checkbox" checked={form.notifyStudents} disabled={busy}
                    onChange={(e) => setForm({ ...form, notifyStudents: e.target.checked })} />
                  Notify my students (a bell alert to students who applied to or work on your projects)
                </label>
              )}
              {editingId !== null && form.repeatWeekly && (
                <p className="text-sm text-gray-600">Changes apply to every week of this entry.</p>
              )}
              <div className="flex flex-wrap gap-3">
                <button type="submit" disabled={busy}
                  className="bg-[#1B51A4] text-white font-semibold rounded-lg px-5 py-2 cursor-pointer disabled:opacity-50">
                  {busy ? "Saving..." : "Save"}
                </button>
                <button type="button" disabled={busy} onClick={() => { setForm(null); setEditingId(null); }}
                  className="border rounded-lg px-5 py-2 cursor-pointer">Cancel</button>
                {editingId !== null && (
                  <button type="button" disabled={busy} onClick={() => void remove()}
                    className="ml-auto text-red-700 border border-red-300 rounded-lg px-5 py-2 cursor-pointer">Delete</button>
                )}
              </div>
            </form>
          </section>
        )}

        <div className="flex flex-wrap gap-6 items-start">
          <section className="bg-white rounded-xl p-4 sm:p-6 flex-[999_1_560px] min-w-0">
            <div className="flex items-center justify-between mb-4">
              <button type="button" onClick={() => changeMonth(-1)} aria-label="Previous month"
                className="px-3 py-1 rounded-lg hover:bg-gray-100 cursor-pointer text-xl">&lsaquo;</button>
              <div className="flex items-center gap-3">
                <h2 className="sr-only">{monthName}</h2>
                <select aria-label="Month" value={month.getMonth()}
                  onChange={(e) => setMonth(new Date(month.getFullYear(), Number(e.target.value), 1))}
                  className="border border-gray-400 rounded-lg px-2 py-1 text-lg font-semibold cursor-pointer">
                  {MONTHS.map((name, index) => <option key={name} value={index}>{name}</option>)}
                </select>
                <select aria-label="Year" value={month.getFullYear()}
                  onChange={(e) => setMonth(new Date(Number(e.target.value), month.getMonth(), 1))}
                  className="border border-gray-400 rounded-lg px-2 py-1 text-lg font-semibold cursor-pointer">
                  {yearOptions(month.getFullYear()).map((y) => <option key={y} value={y}>{y}</option>)}
                </select>
                <button type="button" className="text-sm text-[#1B51A4] underline cursor-pointer"
                  onClick={() => { const d = new Date(); setMonth(new Date(d.getFullYear(), d.getMonth(), 1)); setSelected(today); }}>
                  Today
                </button>
              </div>
              <button type="button" onClick={() => changeMonth(1)} aria-label="Next month"
                className="px-3 py-1 rounded-lg hover:bg-gray-100 cursor-pointer text-xl">&rsaquo;</button>
            </div>
            {loading && <p role="status">Loading...</p>}
            <div className="grid grid-cols-7 gap-1 text-center">
              {WEEKDAYS.map((d) => <div key={d} className="text-xs font-semibold text-gray-500 pb-1">{d}</div>)}
              {days.map((d) => {
                const key = isoDay(d);
                const items = occurrences.filter((item) => covers(item, key));
                const inMonth = d.getMonth() === month.getMonth();
                const isSelected = key === selected;
                const blocked = items.find((item) => item.away);
                return (
                  <button key={key} type="button" onClick={() => setSelected(key)}
                    aria-label={`${d.toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric" })}, ${items.length} item${items.length === 1 ? "" : "s"}`}
                    className={`min-h-14 sm:min-h-24 rounded-lg p-1 flex flex-col items-stretch text-left cursor-pointer border
                      ${isSelected ? "border-[#1B51A4] bg-blue-50" : "border-transparent hover:bg-gray-50"}
                      ${inMonth ? "" : "opacity-40"}`}
                    style={blocked ? AWAY_STYLE : undefined}
                    title={blocked ? `Away: ${blocked.title}` : undefined}>
                    <span className={`text-sm self-center sm:self-start w-7 h-7 flex items-center justify-center rounded-full
                      ${key === today ? "bg-[#F29D38] text-white font-bold" : ""}`}>{d.getDate()}</span>
                    <span className="hidden sm:flex flex-col gap-0.5">
                      {items.slice(0, 3).map((item, i) => (
                        <span key={i} className={`${item.away ? "bg-gray-700 text-white" : colorStyle(item.color).pill} text-xs rounded px-1 truncate`}>
                          {item.away ? `Away: ${item.title}` : item.title}
                        </span>
                      ))}
                      {items.length > 3 && <span className="text-xs text-gray-600">+{items.length - 3} more</span>}
                    </span>
                    {blocked && <span className="sm:hidden text-[10px] font-bold text-gray-700 text-center">AWAY</span>}
                    <span className="flex sm:hidden justify-center gap-0.5 mt-0.5">
                      {items.slice(0, 3).map((item, i) => (
                        <span key={i} className={`w-1.5 h-1.5 rounded-full ${colorStyle(item.color).dot}`} />
                      ))}
                    </span>
                  </button>
                );
              })}
            </div>
          </section>

          <aside className="flex-[1_1_300px] min-w-0 flex flex-col gap-6">
            <section className="bg-white rounded-xl p-6 flex flex-col gap-3">
              <div className="flex justify-between items-baseline gap-2">
                <h2 className="text-xl font-semibold">{dayLabel(selected, { weekday: "long", month: "long", day: "numeric" })}</h2>
                <button type="button" onClick={() => startAdd(selected)}
                  className="text-sm text-[#1B51A4] underline cursor-pointer">Add</button>
              </div>
              {dayItems.length === 0
                ? <p className="text-gray-600">Nothing on this day.</p>
                : <ul className="flex flex-col gap-1">{dayItems.map((item, i) => itemRow(item, i, false))}</ul>}
            </section>
            <section className="bg-white rounded-xl p-6 flex flex-col gap-3">
              <h2 className="text-xl font-semibold">All of {month.toLocaleDateString("en-US", { month: "long" })}</h2>
              {monthItems.length === 0
                ? <p className="text-gray-600">Nothing this month yet.</p>
                : <ul className="flex flex-col gap-1">{monthItems.map((item, i) => itemRow(item, i, true))}</ul>}
            </section>
          </aside>
        </div>
      </main>
    </div>
  );
}
