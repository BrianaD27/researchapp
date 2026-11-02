import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { calendarApi, colorStyle, dayLabel, parseDay, timeRange, type Upcoming } from "../../api/calendar";

// "Office hours and upcoming": the next few PUBLIC calendar entries for a
// professor (by professor id) or a Research Associate (by account id).
export default function UpcomingCard({ professorId, accountId, mine = false, compact = false }: {
  professorId?: number | null;
  accountId?: number | null;
  mine?: boolean;
  compact?: boolean;
}) {
  const [data, setData] = useState<Upcoming | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let active = true;
    const request = professorId != null ? calendarApi.forProfessor(professorId)
      : accountId != null ? calendarApi.forAccount(accountId) : null;
    if (!request) return;
    request
      .then((result) => { if (active) { setData(result); setFailed(false); } })
      .catch(() => { if (active) setFailed(true); });
    return () => { active = false; };
  }, [professorId, accountId]);

  if (failed || !data) return null;
  if (compact && data.items.length === 0) return null;

  const items = (
    <ul className="flex flex-col gap-3">
      {data.items.map((item, index) => {
        const day = parseDay(item.date);
        return (
          <li key={`${item.kind}-${item.entryId ?? item.opportunityId}-${item.date}-${index}`} className="flex gap-3">
            <span className={`${colorStyle(item.color).pill} rounded-lg min-w-12 text-center leading-tight py-1 shrink-0`}>
              <span className="block text-xs font-semibold uppercase">{day.toLocaleDateString("en-US", { weekday: "short" })}</span>
              <span className="block text-lg font-bold">{day.getDate()}</span>
            </span>
            <span className="flex flex-col min-w-0">
              <span className="font-semibold break-words">{item.title}</span>
              <span className="text-sm text-gray-600 break-words">
                {[
                  item.endDate ? `${dayLabel(item.date)} to ${dayLabel(item.endDate)}` : timeRange(item),
                  item.location,
                  item.repeatWeekly ? "Weekly" : null,
                ].filter(Boolean).join(" \u00B7 ")}
              </span>
              {item.notes && !compact && <span className="text-sm text-gray-700 whitespace-pre-wrap break-words">{item.notes}</span>}
            </span>
          </li>
        );
      })}
    </ul>
  );

  if (compact) {
    return (
      <div className="flex flex-col gap-2">
        <p className="text-sm font-semibold">Office hours and upcoming</p>
        {items}
      </div>
    );
  }

  return (
    <section className="bg-white rounded-xl p-6 flex flex-col gap-3 border-l-4 border-[#F29D38]">
      <div className="flex flex-wrap justify-between items-baseline gap-2">
        <h2 className="text-xl font-semibold">Office hours and upcoming</h2>
        {mine && <Link to="/calendar" className="text-sm text-[#1B51A4] underline">Open calendar</Link>}
      </div>
      {data.items.length === 0
        ? <p className="text-gray-600">{mine ? "Nothing public in the next three weeks." : "Nothing posted for the next three weeks."}</p>
        : items}
      {mine && (
        <Link to="/calendar?add=1"
          className="self-start border border-[#1B51A4] text-[#1B51A4] font-semibold rounded-lg px-4 py-2 hover:bg-blue-50">
          + Add to calendar
        </Link>
      )}
    </section>
  );
}
