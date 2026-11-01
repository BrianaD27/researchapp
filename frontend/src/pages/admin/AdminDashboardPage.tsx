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
};

function Tile({ label, value, tone = "default" }: { label: string; value: number | undefined; tone?: "default" | "warn" | "good" }) {
  const colors = tone === "warn" ? "border-amber-400" : tone === "good" ? "border-green-500" : "border-transparent";
  return (
    <div className={`bg-white rounded-xl p-4 border-l-4 ${colors}`}>
      <p className="text-3xl font-bold">{value ?? 0}</p>
      <p className="text-sm text-gray-600">{label}</p>
    </div>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="flex flex-col gap-2">
      <h2 className="text-white text-lg font-semibold">{title}</h2>
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">{children}</div>
    </section>
  );
}

// Admin-only usage numbers. Counts only, no personal details or message text.
export default function AdminDashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

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
      <main className="max-w-6xl mx-auto p-5 flex flex-col gap-6">
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
          <Section title="People">
            <Tile label="Accounts" value={stats.users.total} />
            <Tile label="Logged in this week" value={stats.users.loggedInThisWeek} tone="good" />
            <Tile label="New this week" value={stats.users.newThisWeek} />
            <Tile label="Waiting to verify email" value={stats.users.waitingForEmailVerification}
              tone={stats.users.waitingForEmailVerification ? "warn" : "default"} />
            {Object.entries(stats.usersByRole).map(([role, count]) => (
              <Tile key={role} label={ROLE_LABELS[role] ?? role} value={count} />
            ))}
            <Tile label="Student profiles" value={stats.profiles.students} />
            <Tile label="Faculty profiles" value={stats.profiles.faculty} />
            <Tile label="Locked accounts" value={stats.users.locked} tone={stats.users.locked ? "warn" : "default"} />
            <Tile label="Disabled accounts" value={stats.users.disabled} />
          </Section>

          <Section title="Research">
            <Tile label="Projects posted" value={stats.research.total} />
            <Tile label="Accepting applications" value={stats.research.acceptingApplications} tone="good" />
            <Tile label="Posted this week" value={stats.research.postedThisWeek} />
          </Section>

          <Section title="Applications">
            <Tile label="Total applications" value={stats.applications.total} />
            <Tile label="This week" value={stats.applications.thisWeek} />
            <Tile label="Waiting for a decision" value={stats.applications.pending}
              tone={stats.applications.pending ? "warn" : "default"} />
            <Tile label="Accepted (ongoing)" value={stats.applications.accepted} tone="good" />
            <Tile label="Completed" value={stats.applications.completed} tone="good" />
            <Tile label="Not selected" value={stats.applications.notSelected} />
          </Section>

          <Section title="Messages">
            <Tile label="Conversations" value={stats.messages.conversations} />
            <Tile label="Messages sent" value={stats.messages.messages} />
            <Tile label="Messages this week" value={stats.messages.messagesThisWeek} />
          </Section>

          <Section title="Problem reports">
            <Tile label="New" value={stats.problemReports.new} tone={stats.problemReports.new ? "warn" : "default"} />
            <Tile label="In progress" value={stats.problemReports.inProgress} />
            <Tile label="Fixed" value={stats.problemReports.fixed} tone="good" />
            <Link to="/admin-reports" className="bg-white/10 text-white rounded-xl p-4 flex items-center justify-center underline">
              Open Problem Reports
            </Link>
          </Section>
        </>}
      </main>
    </div>
  );
}
