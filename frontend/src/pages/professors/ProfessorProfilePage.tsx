import { useEffect, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";

interface Professor {
  id: number;
  name: string | null;
  email: string | null;
  department: string | null;
  officeLocation: string | null;
  description: string | null;
  profilePictureUrl: string | null;
}

interface ProfileFields {
  name: string;
  department: string;
  officeLocation: string;
  description: string;
}

const emptyFields: ProfileFields = {
  name: "",
  department: "",
  officeLocation: "",
  description: "",
};

function fieldsFrom(profile: Professor): ProfileFields {
  return {
    name: profile.name ?? "",
    department: profile.department ?? "",
    officeLocation: profile.officeLocation ?? "",
    description: profile.description ?? "",
  };
}

function errorMessage(error: unknown): string {
  if (isAxiosError(error)) {
    if (!error.response) return "Cannot reach the backend. Check that Java is running.";
    if (error.response.status === 401) return "Your login has expired. Log in again.";
    if (error.response.status === 403) return "You do not have permission to edit this profile.";
    return `The request failed (HTTP ${error.response.status}). Changes were not confirmed.`;
  }
  return "The request failed. Changes were not confirmed.";
}

const ProfessorProfilePage = () => {
  const [profiles, setProfiles] = useState<Professor[]>([]);
  const [selected, setSelected] = useState<Professor | null>(null);
  const [fields, setFields] = useState<ProfileFields>(emptyFields);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const [{ data: context }, { data }] = await Promise.all([
          apiClient.get<{ admin: boolean; professorId: number | null }>("/faculty-review/context"),
          apiClient.get<Professor[]>("/professors"),
        ]);
        if (!active) return;
        if (context.admin) {
          setIsAdmin(true);
          setProfiles(data);
          return;
        }
        const own = data.find((profile) => profile.id === context.professorId) ?? null;
        if (!own) {
          setError("This account has no linked faculty profile. Ask a faculty administrator to link your account.");
          return;
        }
        setProfiles([own]);
        setSelected(own);
        setFields(fieldsFrom(own));
      } catch (caught) {
        if (active) setError(errorMessage(caught));
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, []);

  function chooseProfile(id: string) {
    const profile = profiles.find((item) => String(item.id) === id) ?? null;
    setSelected(profile);
    setFields(profile ? fieldsFrom(profile) : emptyFields);
    setMessage("");
    setError("");
  }

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selected || saving) return;
    setSaving(true);
    setError("");
    setMessage("");

    try {
      await apiClient.put(`/professors/${selected.id}`, {
        name: fields.name.trim(),
        department: fields.department.trim(),
        officeLocation: fields.officeLocation.trim(),
        description: fields.description.trim(),
      });

      // Read the record back from the backend after saving.
      const { data } = await apiClient.get<Professor>(`/professors/${selected.id}`);
      setSelected(data);
      setFields(fieldsFrom(data));
      setProfiles((current) => current.map((item) => item.id === data.id ? data : item));
      setMessage("Profile saved and loaded back from the database.");
    } catch (caught) {
      setError(errorMessage(caught));
    } finally {
      setSaving(false);
    }
  }

  async function changePhoto(file: File | null) {
    if (!selected || saving) return;

    if (file && (!["image/jpeg", "image/png"].includes(file.type)
        || file.size > 5 * 1024 * 1024)) {
      setError("Choose a JPG or PNG picture smaller than 5 MB.");
      setMessage("");
      return;
    }

    const id = selected.id;
    setSaving(true);
    setError("");
    setMessage("");

    try {
      if (file) {
        const body = new FormData();
        body.append("file", file);
        await apiClient.post(`/professors/${id}/profile-picture`, body, {
          headers: { "Content-Type": "multipart/form-data" },
        });
      } else {
        await apiClient.delete(`/professors/${id}/profile-picture`);
      }

      const { data } = await apiClient.get<Professor>(`/professors/${id}`);
      setSelected(data);
      setProfiles((current) =>
        current.map((item) => item.id === data.id ? data : item)
      );
      setMessage(file ? "Profile photo saved." : "Profile photo removed.");
    } catch (caught) {
      setError(errorMessage(caught));
    } finally {
      setSaving(false);
    }
  }

  const inputs: { key: keyof ProfileFields; label: string; required?: boolean }[] = [
    { key: "name", label: "Name", required: true },
    { key: "department", label: "Department", required: true },
    { key: "officeLocation", label: "Office location" },
  ];

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-3xl mx-auto p-6">
        <h1 className="text-2xl text-white mb-4">Faculty Profile Settings</h1>
        <section className="bg-white rounded-xl p-6">
          {loading && <p role="status" className="mb-4">Loading profile...</p>}
          {isAdmin && <>
          <label htmlFor="profile-select" className="block font-semibold mb-2">
            Select faculty profile to edit
          </label>
          <select
            id="profile-select"
            value={selected ? String(selected.id) : ""}
            disabled={loading || saving}
            onChange={(event) => chooseProfile(event.target.value)}
            className="w-full border rounded p-2 mb-4"
          >
            <option value="">{loading ? "Loading profiles..." : "Choose a profile"}</option>
            {profiles.map((profile) => (
              <option key={profile.id} value={String(profile.id)}>
                {profile.name || "Unnamed faculty"} - {profile.email || `ID ${profile.id}`}
              </option>
            ))}
          </select>
          </>}

          {error && <p role="alert" className="text-red-700 mb-4">{error}</p>}
          {message && <p role="status" className="text-green-800 mb-4">{message}</p>}
          {isAdmin && !loading && !error && profiles.length === 0 && (
            <p>No faculty profiles have been created yet.</p>
          )}

          {selected && (
            <form onSubmit={save} className="flex flex-col gap-4">
              {isAdmin && (
                <p className="text-sm text-gray-600">
                  Editing: {selected.name || "Unnamed faculty"} (ID {selected.id}).
                </p>
              )}

              <label className="flex flex-col gap-1">
                Email
                <input
                  value={selected.email ?? ""}
                  readOnly
                  className="border rounded p-2 bg-gray-100"
                />
                <span className="text-sm text-gray-600">
                  Email changes are not available here yet.
                </span>
              </label>

              {inputs.map((input) => (
                <label key={input.key} className="flex flex-col gap-1">
                  {input.label}
                  <input
                    required={input.required}
                    maxLength={255}
                    value={fields[input.key]}
                    disabled={saving}
                    onChange={(event) => setFields((current) => ({
                      ...current, [input.key]: event.target.value,
                    }))}
                    className="border rounded p-2"
                  />
                </label>
              ))}

              <label className="flex flex-col gap-1">
                Description
                <textarea
                  maxLength={255}
                  rows={4}
                  value={fields.description}
                  disabled={saving}
                  onChange={(event) => setFields((current) => ({
                    ...current, description: event.target.value,
                  }))}
                  className="border rounded p-2"
                />
              </label>

              <section className="border rounded p-4 flex flex-col gap-3">
                <h2 className="font-semibold">Profile photo</h2>

                {selected.profilePictureUrl ? (
                  <img
                    src={new URL(
                      selected.profilePictureUrl,
                      "http://localhost:8080"
                    ).href}
                    alt="Faculty profile"
                    className="w-32 h-32 rounded-full object-cover"
                  />
                ) : (
                  <p>No profile photo saved.</p>
                )}

                <label className="inline-flex self-start items-center justify-center bg-blue-700 text-white font-semibold rounded-lg px-5 py-3 cursor-pointer hover:bg-blue-800">
                  {saving ? "Saving photo..." : "Choose Photo"}
                  <input
                    key={selected.id}
                    type="file"
                    accept="image/jpeg,image/png"
                    disabled={saving}
                    className="sr-only"
                    onChange={(event) => {
                      const file = event.target.files?.[0];
                      event.target.value = "";
                      if (file) void changePhoto(file);
                    }}
                  />
                </label>

                <p className="text-sm text-gray-600">
                  JPG or PNG, up to 5 MB. Selecting a file saves it immediately.
                </p>

                {selected.profilePictureUrl && (
                  <button
                    type="button"
                    disabled={saving}
                    onClick={() => void changePhoto(null)}
                    className="bg-red-700 text-white rounded px-4 py-2 disabled:opacity-50"
                  >
                    Remove photo
                  </button>
                )}

                {saving && <p role="status">Saving changes...</p>}
              </section>

              <button
                type="submit"
                disabled={saving || !fields.name.trim() || !fields.department.trim()}
                className="bg-green-700 text-white rounded px-4 py-2 disabled:opacity-50"
              >
                {saving ? "Saving..." : "Save profile"}
              </button>
            </form>
          )}
        </section>
      </main>
    </div>
  );
};

export default ProfessorProfilePage;
