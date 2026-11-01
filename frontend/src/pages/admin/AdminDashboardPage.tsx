import { useCallback, useEffect, useState } from "react";
import { isAxiosError } from "axios";
import { Link } from "react-router-dom";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";

type Group = Record<string, number>;

interface Stats {
  generatedAt: string;
  users: Group;
  usersByRole: Group;
  profiles: Group;
  research: Group;
  applications: Group;
  messages: Group;
  problemReports: Group;
}

const ROLE_LABELS: Record<string, string> = {
  STUDENT: "Students",
  PROFESSOR: "Faculty",
  ASSOCIATE: "Research Associates",
  ADMIN: "Admins",
  PARTNER: "Partner school",
  PENDING_FACULTY: "Waiting for faculty approval",
};

function Tile({ label, value, tone = "default", to }: {
  label: string; value: number | undefined; tone?: "default" | "warn" | "good"; to?: string;
}) {
  const colors = tone === "warn" ? "border-amber-400" : tone === "good" ? "border-green-500" : "border-transparent";
  const body = (
    <>
      <p className="text-2xl font-bold">{value ?? 0}</p>
      <p className="text-sm text-gray-600">{label}</p>
    </>
  );
  const box = `bg-gray-50 rounded-lg p-3 border-l-4 ${colors}`;
  return to
    ? <Link to={to} className={`${box} hover:bg-blue-50`}>{body}</Link>
    : <div className={box}>{body}</div>;
}

// One collapsible row: the headline numbers show at a glance; click to see the details.
function Section({ title, summary, alert, open, onToggle, children }: {
  title: string; summary: string; alert?: string; open: boolean; onToggle: () => void; children: React.ReactNode;
}) {
  return (
    <section className="bg-white rounded-xl shadow">
      <button type="button" onClick={onToggle} aria-expanded={open}
        className="w-full flex flex-wrap items-center justify-between gap-3 p-4 text-left cursor-pointer">
        <span className="flex flex-col">
          <span className="text-lg font-semibold">{title}</span>
          <span className="text-sm text-gray-600">{summary}</span>
        </span>
        <span className="flex items-center gap-3">
          {alert && <span className="text-xs font-semibold rounded-full px-2 py-1 bg-amber-100 text-amber-900">{alert}</span>}
          <span aria-hidden="true" className={`text-xl transition-transform ${open ? "rotate-180" : ""}`}>&#9662;</span>
        </span>
      </button>
      {open && <div className="grid grid-cols-2 md:grid-cols-4 gap-3 px-4 pb-4">{children}</div>}
    </section>
  );
}

// Admin-only usage numbers. Counts only, no personal details or message text.
export default function AdminDashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState<Record<string, boolean>>({});
  const toggle = (key: string) => setOpen((current) => ({ ...current, [key]: !current[key] }));

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await apiClient.get<Stats>("/admin/stats");
      setStats(data);
    } catch (caught) {
      setError(isAxiosError(caught) && caught.response?.status === 403
        ? "Only admins can see the dashboard."
        : isAxiosError(caught) && !caught.response
          ? "Cannot reach the backend. Check that Java is running."
          : "Could not load the dashboard.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const first = window.setTimeout(() => void load(), 0);
    return () => window.clearTimeout(first);
  }, [load]);

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-5xl mx-auto p-5 flex flex-col gap-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-2xl text-white">Admin Dashboard</h1>
            {stats && <p className="text-white/80 text-sm">Updated {new Date(stats.generatedAt).toLocaleString()}</p>}
          </div>
          <button type="button" disabled={loading} onClick={() => void load()}
            className="bg-white rounded-lg px-4 py-2 font-semibold cursor-pointer disabled:opacity-50">
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </div>
        {error && <p role="alert" className="bg-red-100 text-red-800 rounded-lg p-3">{error}</p>}

        {stats && <>
          <p className="text-white/90 text-sm">Click a section to see the details. Some numbers open the page they come from.</p>

          <Section title="People" open={Boolean(open.people)} onToggle={() => toggle("people")}
            summary={`${stats.users.total ?? 0} accounts, ${stats.users.loggedInThisWeek ?? 0} logged in this week, ${stats.users.newThisWeek ?? 0} new this week`}
            alert={stats.users.locked ? `${stats.users.locked} locked` : undefined}>
            <Tile label="Accounts" value={stats.users.total} to="/admin-users" />
            <Tile label="Logged in this week" value={stats.users.loggedInThisWeek} tone="good" />
            <Tile label="New this week" value={stats.users.newThisWeek} />
            <Tile label="Waiting to verify email" value={stats.users.waitingForEmailVerification}
              tone={stats.users.waitingForEmailVerification ? "warn" : "default"} />
            {Object.entries(stats.usersByRole).map(([role, count]) => (
              <Tile key={role} label={ROLE_LABELS[role] ?? role} value={count} to="/admin-users" />
            ))}
            <Tile label="Student profiles" value={stats.profiles.students} />
            <Tile label="Faculty profiles" value={stats.profiles.faculty} />
            <Tile label="Locked accounts" value={stats.users.locked} tone={stats.users.locked ? "warn" : "default"} to="/admin-users" />
            <Tile label="Disabled accounts" value={stats.users.disabled} to="/admin-users" />
          </Section>

          <Section title="Research" open={Boolean(open.research)} onToggle={() => toggle("research")}
            summary={`${stats.research.total ?? 0} projects, ${stats.research.acceptingApplications ?? 0} accepting applications`}>
            <Tile label="Projects posted" value={stats.research.total} to="/faculty-view-all" />
            <Tile label="Accepting applications" value={stats.research.acceptingApplications} tone="good" />
            <Tile label="Posted this week" value={stats.research.postedThisWeek} />
          </Section>

          <Section title="Applications" open={Boolean(open.applications)} onToggle={() => toggle("applications")}
            summary={`${stats.applications.total ?? 0} total, ${stats.applications.pending ?? 0} waiting for a decision`}
            alert={stats.applications.pending ? `${stats.applications.pending} waiting` : undefined}>
            <Tile label="Total applications" value={stats.applications.total} to="/faculty-applicants" />
            <Tile label="This week" value={stats.applications.thisWeek} />
            <Tile label="Waiting for a decision" value={stats.applications.pending}
              tone={stats.applications.pending ? "warn" : "default"} to="/faculty-applicants" />
            <Tile label="Accepted (ongoing)" value={stats.applications.accepted} tone="good" />
            <Tile label="Completed" value={stats.applications.completed} tone="good" />
            <Tile label="Not selected" value={stats.applications.notSelected} />
          </Section>

          <Section title="Messages" open={Boolean(open.messages)} onToggle={() => toggle("messages")}
            summary={`${stats.messages.messages ?? 0} messages in ${stats.messages.conversations ?? 0} conversations (counts only, nobody can read them here)`}>
            <Tile label="Conversations" value={stats.messages.conversations} />
            <Tile label="Messages sent" value={stats.messages.messages} />
            <Tile label="Messages this week" value={stats.messages.messagesThisWeek} />
          </Section>

          <Section title="Problem reports" open={Boolean(open.reports)} onToggle={() => toggle("reports")}
            summary={`${stats.problemReports.new ?? 0} new, ${stats.problemReports.inProgress ?? 0} in progress, ${stats.problemReports.fixed ?? 0} fixed`}
            alert={stats.problemReports.new ? `${stats.problemReports.new} new` : undefined}>
            <Tile label="New" value={stats.problemReports.new} tone={stats.problemReports.new ? "warn" : "default"} to="/admin-reports" />
            <Tile label="In progress" value={stats.problemReports.inProgress} to="/admin-reports" />
            <Tile label="Fixed" value={stats.problemReports.fixed} tone="good" to="/admin-reports" />
          </Section>
        </>}
      </main>
    </div>
  );
}
