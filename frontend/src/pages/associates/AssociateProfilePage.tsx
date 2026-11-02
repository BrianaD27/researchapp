import { useEffect, useRef, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import { Link, useParams } from "react-router-dom";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import StudentNavBar from "../../components/common/StudentNavBar";
import apiClient from "../../api/client";
import { currentRole } from "../../api/session";
import { usePresence } from "../../api/presence";
import UpcomingCard from "../../components/calendar/UpcomingCard";
import ActiveToggle from "../../components/common/ActiveToggle";
import MessageButton from "../../components/common/MessageButton";
import { Chip, ProfileCard, ProfileHero, ProfileLayout } from "../../components/profile/ProfileParts";
import { actionClass, hasText } from "../../components/profile/profileUtils";

interface WorksWith {
  professorId: number;
  professorName: string;
  department: string | null;
}

interface AssociateProfile {
  userAccountId: number;
  username: string;
  name: string;
  title: string | null;
  department: string | null;
  about: string | null;
  profilePictureUrl: string | null;
  worksWith: WorksWith[];
  onTeam: boolean;
  mine: boolean;
}

function problem(caught: unknown, fallback: string) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    const data = caught.response.data as { error?: string } | undefined;
    if (data?.error) return data.error;
  }
  return fallback;
}

// A Research Associate's profile. /associate-profile is your own (editable);
// /associate-profile/:id is someone else's (view only).
export default function AssociateProfilePage() {
  const { id } = useParams();
  const [profile, setProfile] = useState<AssociateProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ name: "", title: "", department: "", about: "" });
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const photoInput = useRef<HTMLInputElement>(null);
  const online = usePresence("accounts", profile ? [profile.userAccountId] : []);
  const role = currentRole();

  useEffect(() => {
    let active = true;
    apiClient.get<AssociateProfile>(id ? `/associates/profile/${id}` : "/associates/profile/me")
      .then(({ data }) => { if (active) setProfile(data); })
      .catch((caught) => { if (active) setError(problem(caught, "Could not load this profile.")); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id]);

  function startEdit() {
    if (!profile) return;
    setForm({
      name: profile.name ?? "", title: profile.title ?? "",
      department: profile.department ?? "", about: profile.about ?? "",
    });
    setMessage("");
    setError("");
    setEditing(true);
  }

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      const { data } = await apiClient.put<AssociateProfile>("/associates/profile/me", form);
      setProfile(data);
      setEditing(false);
      setMessage("Profile saved.");
    } catch (caught) {
      setError(problem(caught, "Your profile was not saved."));
    } finally {
      setBusy(false);
    }
  }

  async function uploadPhoto(file: File | undefined) {
    if (!file || busy) return;
    setBusy(true);
    setError("");
    try {
      const body = new FormData();
      body.append("file", file);
      const { data } = await apiClient.post<AssociateProfile>("/associates/profile/me/photo", body, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setProfile(data);
      setMessage("Photo updated.");
    } catch (caught) {
      setError(problem(caught, "The photo was not uploaded."));
    } finally {
      setBusy(false);
      if (photoInput.current) photoInput.current.value = "";
    }
  }

  async function removePhoto() {
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      const { data } = await apiClient.delete<AssociateProfile>("/associates/profile/me/photo");
      setProfile(data);
      setMessage("Photo removed.");
    } catch (caught) {
      setError(problem(caught, "The photo was not removed."));
    } finally {
      setBusy(false);
    }
  }

  const input = "w-full border border-gray-400 rounded-lg p-2";
  return (
    <div className="min-h-screen bg-[#F4F6FA]">
      {role === "STUDENT" ? <StudentNavBar /> : <FacultyNavBar />}
      {loading && <p role="status" className="max-w-6xl mx-auto p-6">Loading profile...</p>}
      {error && <p role="alert" className="max-w-6xl mx-auto p-6 text-red-700">{error}</p>}
      {profile && (
        <>
          <ProfileHero
            name={profile.name}
            photoUrl={profile.profilePictureUrl}
            active={Boolean(online[profile.userAccountId])}
            lines={[
              [profile.title || "Research Associate", profile.department].filter(Boolean).join(" \u00B7 "),
            ]}
            actions={profile.mine && !editing ? (
              <>
                <button type="button" onClick={startEdit} className={actionClass}>Edit Profile</button>
                <button type="button" disabled={busy} onClick={() => photoInput.current?.click()}
                  className="bg-white text-[#1B51A4] font-bold rounded-lg px-5 py-3 min-h-11 cursor-pointer disabled:opacity-50">
                  {profile.profilePictureUrl ? "Change Photo" : "Add Photo"}
                </button>
                {profile.profilePictureUrl && (
                  <button type="button" disabled={busy} onClick={() => void removePhoto()}
                    className="border border-white text-white font-bold rounded-lg px-5 py-3 min-h-11 cursor-pointer disabled:opacity-50">
                    Remove Photo
                  </button>
                )}
                <ActiveToggle />
                <input ref={photoInput} type="file" accept="image/png,image/jpeg" className="hidden"
                  onChange={(event) => void uploadPhoto(event.target.files?.[0])} />
              </>
            ) : !profile.mine ? (
              <MessageButton kind="account" targetId={profile.userAccountId} />
            ) : undefined}
          />
          <ProfileLayout
            main={<>
              {message && <p role="status" className="bg-green-100 text-green-800 rounded-lg p-3">{message}</p>}
              {profile.mine && !profile.onTeam && (
                <section className="bg-amber-50 border-l-4 border-amber-400 rounded-xl p-4">
                  <p className="font-semibold">You're not on a professor's team yet.</p>
                  <p className="text-sm text-gray-700">
                    Your account is view-only for now. When a professor adds you on their Research Associates page,
                    you'll see their projects and can help with them.
                  </p>
                </section>
              )}
              {editing ? (
                <ProfileCard title="Edit your profile">
                  <form onSubmit={save} className="flex flex-col gap-3">
                    <label className="flex flex-col gap-1">Full name
                      <input maxLength={150} value={form.name} disabled={busy}
                        onChange={(event) => setForm({ ...form, name: event.target.value })} className={input} />
                    </label>
                    <label className="flex flex-col gap-1">Title
                      <input maxLength={150} value={form.title} disabled={busy} placeholder="Doctoral Candidate"
                        onChange={(event) => setForm({ ...form, title: event.target.value })} className={input} />
                    </label>
                    <label className="flex flex-col gap-1">Department
                      <input maxLength={150} value={form.department} disabled={busy}
                        onChange={(event) => setForm({ ...form, department: event.target.value })} className={input} />
                    </label>
                    <label className="flex flex-col gap-1">About
                      <textarea rows={6} maxLength={2000} value={form.about} disabled={busy}
                        placeholder="Your research interests and what you help with"
                        onChange={(event) => setForm({ ...form, about: event.target.value })} className={input} />
                    </label>
                    <div className="flex gap-3">
                      <button type="submit" disabled={busy}
                        className="bg-blue-700 text-white font-semibold rounded-lg px-5 py-2 cursor-pointer disabled:opacity-50">
                        {busy ? "Saving..." : "Save"}
                      </button>
                      <button type="button" disabled={busy} onClick={() => setEditing(false)}
                        className="border rounded-lg px-5 py-2 cursor-pointer">Cancel</button>
                    </div>
                  </form>
                </ProfileCard>
              ) : (
                <ProfileCard title="About">
                  {hasText(profile.about)
                    ? <p className="leading-relaxed text-gray-700 whitespace-pre-wrap">{profile.about}</p>
                    : <p className="text-gray-600">{profile.mine ? "Add a few lines about your research with Edit Profile." : "No details yet."}</p>}
                </ProfileCard>
              )}
            </>}
            side={<>
              <UpcomingCard accountId={profile.userAccountId} mine={profile.mine} />
              <ProfileCard title="Works with">
                {profile.worksWith.length === 0 && <p className="text-gray-600">Not on a professor's team yet.</p>}
                <div className="flex flex-col gap-2">
                  {profile.worksWith.map((w) => (
                    <div key={w.professorId} className="flex flex-col">
                      <span className="font-semibold">{w.professorName}</span>
                      {w.department && <span className="text-sm text-gray-600">{w.department}</span>}
                    </div>
                  ))}
                </div>
              </ProfileCard>
              {profile.mine && profile.onTeam && (
                <ProfileCard title="Your work">
                  <div className="flex flex-wrap gap-2">
                    <Chip>Use "Working for" in the top bar to switch professors</Chip>
                  </div>
                  <Link to="/faculty-view-all" className="text-blue-700 underline">Open my professor's projects</Link>
                </ProfileCard>
              )}
            </>}
          />
        </>
      )}
    </div>
  );
}
