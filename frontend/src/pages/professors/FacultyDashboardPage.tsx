import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import { Link } from "react-router-dom";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import ActiveDot from "../../components/common/ActiveDot";
import { timeOfDay } from "../../api/greeting";
import apiClient from "../../api/client";
import { AWAY_STYLE, colorStyle, dayLabel, timeRange, type Occurrence } from "../../api/calendar";

interface Person {
  accountId: number;
  name: string;
  role: string;
  detail: string | null;
  onlineNow: boolean;
}

interface Dashboard {
  greetingName: string | null;
  professorName: string | null;
  associate: boolean;
  canSeeStudents: boolean;
  tiles: {
    pendingApplications: number;
    unreadMessages: number;
    openOpportunities: number;
    closingThisWeek: number;
    studentsOnProjects: number;
  };
  onlineNow: Person[];
  activeThisWeek: number;
  connectedPeople: number;
  thisWeek: Occurrence[];
  recent: { at: string; text: string }[];
  projects: {
    id: number;
    title: string;
    availability: string | null;
    deadline: string | null;
    applicants: number;
    pending: number;
    accepted: number;
  }[];
  waiting: { studentName: string; projectTitle: string; appliedAt: string | null }[];
  team: TeamMember[];
}

interface TeamMember {
  accountId: number;
  name: string;
  role: string;
  onlineNow: boolean;
  projectIds: number[];
}

// Colored project icons, in the spirit of a project-management home page.
const PROJECT_COLORS = ["bg-blue-600", "bg-teal-600", "bg-purple-600", "bg-orange-500", "bg-pink-600", "bg-amber-500"];



function problem(caught: unknown, fallback: string) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    const data = caught.response.data as { error?: string } | undefined;
    if (data?.error) return data.error;
  }
  return fallback;
}

function ago(value: string) {
  const minutes = Math.max(0, Math.round((Date.now() - new Date(value).getTime()) / 60000));
  if (minutes < 60) return `${minutes} min ago`;
  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours} hr ago`;
  const days = Math.round(hours / 24);
  return `${days} day${days === 1 ? "" : "s"} ago`;
}

// "My Dashboard" for faculty and Research Associates: only their own projects and people.
export default function FacultyDashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [error, setError] = useState("");
  const [teamProject, setTeamProject] = useState("all");

  useEffect(() => {
    let active = true;
    function load() {
      apiClient.get<Dashboard>("/faculty-dashboard")
        .then(({ data: result }) => { if (active) { setData(result); setError(""); } })
        .catch((caught) => { if (active) setError(problem(caught, "Could not load your dashboard.")); });
    }
    load();
    const timer = window.setInterval(load, 60000);
    return () => { active = false; window.clearInterval(timer); };
  }, []);

  const now = new Date();
  const team = data?.team ?? [];
  const teamOnProject = teamProject === "all"
    ? team
    : team.filter((p) => p.projectIds.includes(Number(teamProject)));
  const card = "bg-white rounded-xl p-5 flex flex-col gap-3 shadow-sm";
  const tile = "bg-white rounded-xl p-4 flex flex-col gap-1 hover:ring-2 hover:ring-[#F29D38]";

  return (
    <div className="min-h-screen bg-[#F4F6FA]">
      <FacultyNavBar />
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-8 flex flex-col gap-6">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div>
            <p className="text-sm font-semibold text-gray-600">
              {now.toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric" })}
            </p>
            <h1 className="text-3xl sm:text-4xl font-bold">
              {timeOfDay(now.getHours())}{data?.greetingName ? `, ${data.greetingName}` : ""}
            </h1>
            {data?.associate && data.professorName && (
              <p className="text-gray-600">Working for {data.professorName}</p>
            )}
          </div>
          {data && (
            <div className="flex flex-wrap gap-2 text-sm">
              <span className="bg-white rounded-full px-3 py-1 shadow-sm">
                {data.tiles.pendingApplications} waiting for review
              </span>
              <span className="bg-white rounded-full px-3 py-1 shadow-sm">
                {data.tiles.unreadMessages} unread message{data.tiles.unreadMessages === 1 ? "" : "s"}
              </span>
              <span className="bg-white rounded-full px-3 py-1 shadow-sm">
                {data.thisWeek.length} coming up
              </span>
            </div>
          )}
        </div>
        {error && <p role="alert" className="bg-red-50 text-red-700 rounded-lg p-3">{error}</p>}
        {!data && !error && <p role="status">Loading...</p>}

        {data && !data.professorName && (
          <p className="bg-amber-50 border-l-4 border-amber-400 rounded-xl p-4">
            {data.associate
              ? "You're not on a professor's team yet. Your calendar and messages are below."
              : "This account has no linked faculty profile yet. Your calendar and messages are below."}
          </p>
        )}

        {data && (
          <>
            <section aria-label="At a glance" className="grid grid-cols-2 md:grid-cols-5 gap-3">
              <Link to="/faculty-applicants" className={tile}>
                <span className="text-sm text-gray-600">Applications waiting</span>
                <span className="text-3xl font-bold text-[#1B51A4]">{data.tiles.pendingApplications}</span>
              </Link>
              <Link to="/messages" className={tile}>
                <span className="text-sm text-gray-600">Unread messages</span>
                <span className="text-3xl font-bold text-[#1B51A4]">{data.tiles.unreadMessages}</span>
              </Link>
              <Link to="/faculty-view-all" className={tile}>
                <span className="text-sm text-gray-600">Open opportunities</span>
                <span className="text-3xl font-bold text-[#1B51A4]">{data.tiles.openOpportunities}</span>
                {data.tiles.closingThisWeek > 0 && (
                  <span className="text-xs font-semibold text-orange-700">{data.tiles.closingThisWeek} closing this week</span>
                )}
              </Link>
              <Link to="/faculty-view-all" className={tile}>
                <span className="text-sm text-gray-600">Students on projects</span>
                <span className="text-3xl font-bold text-[#1B51A4]">{data.tiles.studentsOnProjects}</span>
              </Link>
              <Link to="/calendar" className={tile}>
                <span className="text-sm text-gray-600">Coming up (2 weeks)</span>
                <span className="text-3xl font-bold text-[#1B51A4]">{data.thisWeek.length}</span>
              </Link>
            </section>

            <div className="grid md:grid-cols-2 gap-6">
              <section className={card}>
                <div className="flex justify-between items-baseline gap-2">
                  <h2 className="text-xl font-semibold">Needs your attention</h2>
                  <Link to="/faculty-applicants" className="text-sm text-[#1B51A4] underline">All applicants</Link>
                </div>
                {!data.canSeeStudents && data.professorName && (
                  <p className="text-gray-600">You don't have review permission for this professor's applicants.</p>
                )}
                {data.canSeeStudents && data.waiting.length === 0 && data.tiles.unreadMessages === 0 && (
                  <p className="text-gray-600">You're all caught up.</p>
                )}
                <ul className="flex flex-col divide-y divide-gray-200">
                  {data.tiles.unreadMessages > 0 && (
                    <li className="py-2 flex justify-between gap-3">
                      <span>{data.tiles.unreadMessages} unread message{data.tiles.unreadMessages === 1 ? "" : "s"}</span>
                      <Link to="/messages" className="text-sm text-[#1B51A4] underline shrink-0">Open</Link>
                    </li>
                  )}
                  {data.waiting.map((w, i) => (
                    <li key={i} className="py-2 flex justify-between gap-3">
                      <span className="min-w-0">
                        <span className="font-semibold">{w.studentName}</span> applied to {w.projectTitle}
                        {w.appliedAt && <span className="block text-xs text-gray-500">{ago(w.appliedAt)}</span>}
                      </span>
                      <Link to="/faculty-applicants" className="text-sm text-[#1B51A4] underline shrink-0">Review</Link>
                    </li>
                  ))}
                </ul>
              </section>

              <section className={card}>
                <div className="flex justify-between items-baseline gap-2">
                  <h2 className="text-xl font-semibold">Projects</h2>
                  <Link to="/faculty-view-all" className="text-sm text-[#1B51A4] underline">View all</Link>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {!data.associate && (
                    <Link to="/post-new" className="flex items-center gap-3 rounded-lg p-2 hover:bg-gray-100">
                      <span className="w-10 h-10 rounded-lg border-2 border-dashed border-gray-400 flex items-center justify-center text-xl text-gray-600">+</span>
                      <span className="font-semibold">Post new opportunity</span>
                    </Link>
                  )}
                  {data.projects.slice(0, 7).map((p, i) => (
                    <Link key={p.id} to="/faculty-view-all" className="flex items-center gap-3 rounded-lg p-2 hover:bg-gray-100 min-w-0">
                      <span className={`w-10 h-10 rounded-lg ${PROJECT_COLORS[i % PROJECT_COLORS.length]} text-white font-bold flex items-center justify-center shrink-0`}>
                        {p.title.charAt(0).toUpperCase()}
                      </span>
                      <span className="flex flex-col min-w-0">
                        <span className="font-semibold truncate">{p.title}</span>
                        <span className="text-xs text-gray-600">
                          {p.pending > 0 ? `${p.pending} waiting` : p.availability || "Status not set"}
                        </span>
                      </span>
                    </Link>
                  ))}
                </div>
              </section>
            </div>

            <div className="flex flex-wrap gap-6 items-start">
              <div className="flex-[999_1_520px] min-w-0 flex flex-col gap-6">
                <section className={card}>
                  <div className="flex flex-wrap justify-between items-baseline gap-2">
                    <h2 className="text-xl font-semibold">Coming up</h2>
                    <Link to="/calendar" className="text-sm text-[#1B51A4] underline">Open calendar</Link>
                  </div>
                  {data.thisWeek.length === 0 && <p className="text-gray-600">Nothing on your calendar for the next two weeks.</p>}
                  <ul className="flex flex-col gap-2">
                    {data.thisWeek.slice(0, 8).map((item, index) => (
                      <li key={`${item.kind}-${item.entryId ?? item.opportunityId}-${item.date}-${index}`}
                        className="flex gap-3 items-start rounded-lg p-2" style={item.away ? AWAY_STYLE : undefined}>
                        <span className={`w-3 h-3 rounded-full mt-1.5 shrink-0 ${item.away ? "bg-gray-700" : colorStyle(item.color).dot}`} />
                        <span className="flex flex-col min-w-0">
                          <span className="font-semibold break-words">
                            {item.away && <span className="bg-gray-700 text-white text-xs font-bold rounded px-1.5 py-0.5 mr-2">AWAY</span>}
                            {item.title}
                          </span>
                          <span className="text-sm text-gray-600">
                            {[dayLabel(item.date), item.endDate ? `through ${dayLabel(item.endDate)}` : item.away ? null : timeRange(item),
                              item.location].filter(Boolean).join(" - ")}
                          </span>
                        </span>
                      </li>
                    ))}
                  </ul>
                </section>

                {data.projects.length > 0 && (
                  <section className={card}>
                    <h2 className="text-xl font-semibold">Project details</h2>
                    <div className="overflow-x-auto">
                      <table className="w-full text-sm">
                        <thead>
                          <tr className="text-left text-gray-600 border-b">
                            <th className="py-2 pr-3">Project</th>
                            <th className="py-2 pr-3">Status</th>
                            <th className="py-2 pr-3">Deadline</th>
                            <th className="py-2 pr-3 text-right">Applied</th>
                            <th className="py-2 pr-3 text-right">Waiting</th>
                            <th className="py-2 text-right">Accepted</th>
                          </tr>
                        </thead>
                        <tbody>
                          {data.projects.map((p) => (
                            <tr key={p.id} className="border-b last:border-b-0">
                              <td className="py-2 pr-3 font-semibold">{p.title}</td>
                              <td className="py-2 pr-3">{p.availability || "Not set"}</td>
                              <td className="py-2 pr-3">{p.deadline ? dayLabel(p.deadline, { month: "short", day: "numeric", year: "numeric" }) : "-"}</td>
                              <td className="py-2 pr-3 text-right">{p.applicants}</td>
                              <td className="py-2 pr-3 text-right">{p.pending > 0 ? <span className="font-bold text-orange-700">{p.pending}</span> : 0}</td>
                              <td className="py-2 text-right">{p.accepted}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </section>
                )}
              </div>

              <aside className="flex-[1_1_300px] min-w-0 flex flex-col gap-6">
                {data.team.length > 0 && (
                  <section className={card}>
                    <h2 className="text-xl font-semibold">Your team</h2>
                    {data.projects.length > 0 && (
                      <label className="flex flex-col gap-1 text-sm">
                        Project
                        <select value={teamProject} onChange={(event) => setTeamProject(event.target.value)}
                          className="border border-gray-400 rounded-lg px-2 py-1.5 cursor-pointer">
                          <option value="all">All projects</option>
                          {data.projects.map((p) => <option key={p.id} value={String(p.id)}>{p.title}</option>)}
                        </select>
                      </label>
                    )}
                    {teamOnProject.length === 0 && (
                      <p className="text-sm text-gray-600">
                        No one is on this project yet. Assign associates on the Research Associates page.
                      </p>
                    )}
                    <ul className="flex flex-col gap-2">
                      {teamOnProject.map((p) => (
                        <li key={p.accountId} className="flex items-center gap-3">
                          <span className="relative w-9 h-9 rounded-full bg-[#DCE6F5] text-[#1B51A4] font-bold flex items-center justify-center shrink-0">
                            {p.name.charAt(0).toUpperCase()}
                            {p.onlineNow && <span className="absolute -bottom-0.5 -right-0.5 w-3 h-3 rounded-full bg-green-500 border-2 border-white" />}
                          </span>
                          <span className="flex flex-col min-w-0">
                            <span className="font-semibold truncate">{p.name}</span>
                            <span className="text-xs text-gray-600">{p.role}</span>
                          </span>
                        </li>
                      ))}
                    </ul>
                  </section>
                )}
                {data.professorName && (
                  <section className={card}>
                    <h2 className="text-xl font-semibold">Who's active</h2>
                    <p className="text-sm text-gray-600">
                      {data.activeThisWeek} of {data.connectedPeople} people on your projects were active this week.
                    </p>
                    {data.onlineNow.length === 0
                      ? <p className="text-gray-600">No one on your projects is online right now.</p>
                      : (
                        <ul className="flex flex-col gap-2">
                          {data.onlineNow.map((p) => (
                            <li key={p.accountId} className="flex items-center gap-2">
                              <ActiveDot active label={false} />
                              <span className="flex flex-col min-w-0">
                                <span className="font-semibold truncate">{p.name}</span>
                                <span className="text-xs text-gray-600 truncate">{[p.role, p.detail].filter(Boolean).join(" - ")}</span>
                              </span>
                            </li>
                          ))}
                        </ul>
                      )}
                    <p className="text-xs text-gray-500">Only people connected to your projects. Anyone who turned off "Show when I'm active" is hidden.</p>
                  </section>
                )}
                {data.canSeeStudents && (
                  <section className={card}>
                    <h2 className="text-xl font-semibold">Recent activity</h2>
                    {data.recent.length === 0 && <p className="text-gray-600">Nothing new this week.</p>}
                    <ul className="flex flex-col gap-2">
                      {data.recent.map((r, i) => (
                        <li key={i} className="text-sm">
                          <span>{r.text}</span>
                          <span className="block text-xs text-gray-500">{ago(r.at)}</span>
                        </li>
                      ))}
                    </ul>
                  </section>
                )}
              </aside>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
