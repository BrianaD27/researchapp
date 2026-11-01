import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";

interface AdminUser {
  id: number;
  username: string;
  email: string;
  role: string;
  active: boolean;
  accountLocked: boolean;
}

const roles = [
  { value: "STUDENT", label: "Student" },
  { value: "PROFESSOR", label: "Faculty" },
  { value: "ASSOCIATE", label: "Research Associate" },
  { value: "ADMIN", label: "Admin" },
  { value: "PARTNER", label: "Partner school (view only)" },
];

function problem(caught: unknown, fallback: string) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    if (caught.response.status === 403) return "Only admins can manage users.";
    if (caught.response.status === 401) return "Your login expired. Log in again.";
    const data = caught.response.data as { message?: string; error?: string } | undefined;
    if (data?.message || data?.error) return String(data.message ?? data.error);
  }
  return fallback;
}

export default function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [roleDraft, setRoleDraft] = useState<Record<string, string>>({});
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function refresh() {
    const { data } = await apiClient.get<AdminUser[]>("/v1/admin/users");
    setUsers([...data].sort((a, b) => a.username.localeCompare(b.username)));
  }

  useEffect(() => {
    let active = true;
    apiClient.get<AdminUser[]>("/v1/admin/users")
      .then(({ data }) => { if (active) setUsers([...data].sort((a, b) => a.username.localeCompare(b.username))); })
      .catch((caught) => { if (active) setError(problem(caught, "Could not load users.")); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  async function act(request: () => Promise<unknown>, done: string) {
    if (busy) return;
    setBusy(true);
    setError("");
    setMessage("");
    try {
      await request();
      await refresh();
      setMessage(done);
    } catch (caught) {
      setError(problem(caught, "That change was not saved."));
    } finally {
      setBusy(false);
    }
  }

  const visible = users.filter((user) =>
    `${user.username} ${user.email} ${user.role}`.toLowerCase().includes(query.trim().toLowerCase()));
  const button = "rounded-lg px-3 py-1.5 text-sm font-semibold cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed";

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-6xl mx-auto p-5 flex flex-col gap-4">
        <h1 className="text-2xl text-white">Users</h1>
        {loading && <p role="status" className="text-white">Loading...</p>}
        {error && <p role="alert" className="bg-white text-red-700 rounded-lg p-4">{error}</p>}
        {message && <p role="status" className="bg-white text-green-800 rounded-lg p-4">{message}</p>}
        {!loading && !error && users.length === 0 && <p className="bg-white rounded-lg p-4">No users found.</p>}
        {users.length > 0 && (
          <section className="bg-white rounded-xl p-5 overflow-x-auto">
            <input aria-label="Search users" placeholder="Search by username, email, or role..."
              value={query} onChange={(event) => setQuery(event.target.value)}
              className="border rounded-lg p-2 w-full mb-4" />
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b">
                  <th className="py-2 pr-3">Username</th>
                  <th className="py-2 pr-3">Email</th>
                  <th className="py-2 pr-3">Role</th>
                  <th className="py-2 pr-3">Status</th>
                  <th className="py-2">Actions</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((user) => {
                  const draft = roleDraft[user.username] ?? user.role;
                  return (
                    <tr key={user.id} className="border-b align-middle">
                      <td className="py-2 pr-3 font-semibold">{user.username}</td>
                      <td className="py-2 pr-3">{user.email}</td>
                      <td className="py-2 pr-3">
                        <div className="flex items-center gap-2">
                          <select aria-label={`Role for ${user.username}`} value={draft} disabled={busy}
                            onChange={(event) => setRoleDraft((current) => ({ ...current, [user.username]: event.target.value }))}
                            className="border rounded-lg p-1">
                            {roles.map((role) => <option key={role.value} value={role.value}>{role.label}</option>)}
                          </select>
                          {draft !== user.role && (
                            <button type="button" disabled={busy} className={`${button} bg-blue-700 text-white`}
                              onClick={() => void act(
                                () => apiClient.put(`/v1/admin/users/${encodeURIComponent(user.username)}/role`, null,
                                  { params: { role: draft } }),
                                `${user.username} is now ${roles.find((role) => role.value === draft)?.label ?? draft}.`)
                                .then(() => setRoleDraft((current) => { const next = { ...current }; delete next[user.username]; return next; }))}>
                              Save
                            </button>
                          )}
                        </div>
                      </td>
                      <td className="py-2 pr-3">
                        {user.active ? "Active" : "Deactivated"}{user.accountLocked ? ", locked" : ""}
                      </td>
                      <td className="py-2">
                        <div className="flex gap-2 flex-wrap">
                          {user.active ? (
                            <button type="button" disabled={busy} className={`${button} border border-red-700 text-red-700`}
                              onClick={() => void act(() => apiClient.put(`/v1/admin/users/${encodeURIComponent(user.username)}/deactivate`),
                                `${user.username} was deactivated and signed out.`)}>Deactivate</button>
                          ) : (
                            <button type="button" disabled={busy} className={`${button} border border-green-700 text-green-700`}
                              onClick={() => void act(() => apiClient.put(`/v1/admin/users/${encodeURIComponent(user.username)}/activate`),
                                `${user.username} was reactivated.`)}>Reactivate</button>
                          )}
                          {user.accountLocked && (
                            <button type="button" disabled={busy} className={`${button} border`}
                              onClick={() => void act(() => apiClient.put(`/v1/admin/users/${encodeURIComponent(user.username)}/unlock`),
                                `${user.username} was unlocked.`)}>Unlock</button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </section>
        )}
      </main>
    </div>
  );
}
