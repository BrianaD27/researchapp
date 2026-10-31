import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";

interface Opportunity {
  id: number;
  title: string;
  department: string | null;
  availability: string | null;
  professorId: number | null;
  professorName: string | null;
}

interface Application {
  id: number;
  studentId: number;
  studentName: string;
  researchOpportunityId: number;
  applicationStatus: "PENDING" | "ACCEPTED" | "REJECTED";
  appliedAt: string | null;
}

interface Student {
  id: number;
  name: string;
  email: string;
  major: string | null;
  classification: string | null;
  graduationYear: number | null;
  gpa: number | null;
  availableHoursPerWeek: number | null;
  description: string | null;
  previousExperience: string | null;
  skills: string[] | null;
  profilePictureUrl: string | null;
  resumeUrl: string | null;
}

const statusLabels = {
  PENDING: "Pending",
  ACCEPTED: "Accepted",
  REJECTED: "Not Selected",
};

function failure(caught: unknown) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    if (caught.response.status === 403) return "You do not have permission to review these applicants.";
    if (caught.response.status === 401) return "Your login expired. Log in again.";
    return `Request failed (HTTP ${caught.response.status}). Refresh the page to try again.`;
  }
  return "Could not load the records. Refresh the page to try again.";
}

export default function ProfessorApplicantsPage() {
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [opportunityId, setOpportunityId] = useState<number | null>(null);
  const [applications, setApplications] = useState<Application[]>([]);
  const [applicationId, setApplicationId] = useState<number | null>(null);
  const [student, setStudent] = useState<Student | null>(null);
  const [filter, setFilter] = useState("");
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingApplicants, setLoadingApplicants] = useState(false);
  const [loadingStudent, setLoadingStudent] = useState(false);
  const [busy, setBusy] = useState(false);
  const [uncertain, setUncertain] = useState(false);
  const [error, setError] = useState("");
  const [applicantError, setApplicantError] = useState("");
  const [studentError, setStudentError] = useState("");
  const [actionError, setActionError] = useState("");
  const [message, setMessage] = useState("");
  const [reload] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    setActionError("");
    setMessage("");
    setUncertain(false);
    setApplications([]);
    setStudent(null);

    async function load() {
      try {
        const { data: context } = await apiClient.get<{
          admin: boolean; professorId: number | null;
        }>("/faculty-review/context");
        if (!context.admin && context.professorId === null) {
          throw new Error("NO_FACULTY_PROFILE");
        }
        const { data } = await apiClient.get<Opportunity[]>("/research-opportunities");
        const own = context.admin
          ? data
          : data.filter((item) => item.professorId === context.professorId);
        if (!active) return;
        setOpportunities(own);
        setOpportunityId((current) =>
          own.some((item) => item.id === current) ? current : own[0]?.id ?? null
        );
      } catch (caught) {
        if (!active) return;
        setOpportunities([]);
        setOpportunityId(null);
        setError(caught instanceof Error && caught.message === "NO_FACULTY_PROFILE"
          ? "This account has no linked faculty profile. A faculty administrator must link it before you can review applicants."
          : failure(caught));
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, [reload]);

  useEffect(() => {
    let active = true;
    setApplications([]);
    setApplicationId(null);
    setStudent(null);
    setApplicantError("");
    setActionError("");
    setMessage("");
    setFilter("");
    if (loading || opportunityId === null) return;
    setLoadingApplicants(true);
    apiClient.get<Application[]>(`/applications/opportunity/${opportunityId}`)
      .then(({ data }) => {
        if (!active) return;
        setApplications(data);
        setApplicationId(data[0]?.id ?? null);
      })
      .catch((caught: unknown) => {
        if (active) setApplicantError(failure(caught));
      })
      .finally(() => { if (active) setLoadingApplicants(false); });
    return () => { active = false; };
  }, [opportunityId, loading, reload]);

  const visible = applications.filter((item) =>
    (!filter || item.applicationStatus === filter)
    && item.studentName.toLowerCase().includes(query.trim().toLowerCase())
  );
  const selected = visible.find((item) => item.id === applicationId) ?? visible[0];
  const selectedStudentId = selected?.studentId;
  const opportunity = opportunities.find((item) => item.id === opportunityId);

  useEffect(() => {
    let active = true;
    setStudent(null);
    setStudentError("");
    if (selectedStudentId === undefined) return;
    setLoadingStudent(true);
    apiClient.get<Student>(`/students/${selectedStudentId}`)
      .then(({ data }) => { if (active) setStudent(data); })
      .catch((caught: unknown) => { if (active) setStudentError(failure(caught)); })
      .finally(() => { if (active) setLoadingStudent(false); });
    return () => { active = false; };
  }, [selectedStudentId, reload]);

  async function decide(status: Application["applicationStatus"]) {
    if (!selected || busy || uncertain) return;
    const id = selected.id;
    setBusy(true);
    setActionError("");
    setMessage("");
    try {
      await apiClient.put(`/applications/${id}`, { applicationStatus: status });
      const { data } = await apiClient.get<Application>(`/applications/${id}`);
      if (data.applicationStatus !== status) throw new Error("Decision not confirmed");
      setApplications((current) => current.map((item) => item.id === id ? data : item));
      setMessage(`Decision saved: ${statusLabels[data.applicationStatus]}.`);
    } catch (caught) {
      setUncertain(true);
      setActionError(`${failure(caught)} The decision has not been confirmed. Refresh the page before making another decision.`);
    } finally {
      setBusy(false);
    }
  }

  async function downloadResume() {
    if (!selected || busy) return;
    const id = selected.id;
    setBusy(true);
    setActionError("");
    try {
      const { data } = await apiClient.get<Blob>(
        `/faculty-review/applications/${id}/resume`, { responseType: "blob" }
      );
      const url = URL.createObjectURL(data);
      const link = document.createElement("a");
      link.href = url;
      link.download = `applicant-${selected.studentId}-resume.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 10000);
    } catch (caught) {
      setActionError(isAxiosError(caught) && caught.response?.status === 404
        ? "This applicant has not uploaded a resume."
        : failure(caught));
    } finally {
      setBusy(false);
    }
  }

  const buttonClass = "rounded-lg px-4 py-2 text-sm font-semibold cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed";
  const profile = student?.id === selectedStudentId ? student : null;

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      <FacultyNavBar />
      <main className="max-w-7xl mx-auto p-5">
        <div className="flex justify-between items-center gap-3 mb-4">
          <h1 className="text-2xl text-white">Review Applicants</h1>

        </div>
        {error && <p role="alert" className="bg-white text-red-700 rounded-lg p-4 mb-4">{error}</p>}
        {loading ? <p role="status" className="text-white">Loading opportunities...</p> : !error && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
            <section>
              <h2 className="text-xl text-white mb-3">My Opportunities</h2>
              {!opportunities.length && <p className="bg-white rounded-lg p-4">No opportunities are available for this account.</p>}
              <div className="max-h-64 overflow-y-auto flex flex-col gap-2 mb-5">
                {opportunities.map((item) => (
                  <button key={item.id} type="button" disabled={busy}
                    onClick={() => setOpportunityId(item.id)}
                    aria-pressed={opportunityId === item.id}
                    className={`bg-white rounded-xl p-4 text-left border-4 cursor-pointer ${opportunityId === item.id ? "border-orange-500" : "border-transparent"}`}>
                    <h3 className="font-semibold text-lg">{item.title}</h3>
                    <p>{item.department || "Department not specified"} | {item.availability || "Status not specified"}</p>
                    <p className="text-sm text-gray-600">{item.professorName}</p>
                  </button>
                ))}
              </div>
              {opportunity && <>
                <h2 className="text-xl text-white mb-3">Applicants - {opportunity.title}</h2>
                <input aria-label="Search applicants" placeholder="Search applicants by name..."
                  value={query} onChange={(event) => setQuery(event.target.value)}
                  className="w-full bg-white border rounded-lg p-3 mb-3" />
                <div className="flex flex-wrap gap-2 mb-3">
                  {[
                    ["", "All"], ["PENDING", "Pending"],
                    ["ACCEPTED", "Accepted"], ["REJECTED", "Not Selected"],
                  ].map(([value, label]) => (
                    <button key={value} type="button" disabled={busy}
                      aria-pressed={filter === value} onClick={() => setFilter(value)}
                      className={`${buttonClass} ${filter === value ? "bg-orange-600 text-white" : "bg-white"}`}>
                      {label} ({applications.filter((item) => !value || item.applicationStatus === value).length})
                    </button>
                  ))}
                </div>
                {applicantError && <p role="alert" className="bg-white text-red-700 rounded-lg p-4">{applicantError}</p>}
                {loadingApplicants ? <p role="status" className="text-white">Loading applicants...</p> : !applicantError && <>
                  {!visible.length && <p className="bg-white rounded-lg p-4">No applicants match this view.</p>}
                  <div className="flex flex-col gap-2">
                    {visible.map((item) => (
                      <button key={item.id} type="button" disabled={busy}
                        onClick={() => { setApplicationId(item.id); setMessage(""); setActionError(""); }}
                        aria-pressed={selected?.id === item.id}
                        className={`bg-white rounded-xl p-4 text-left border-4 cursor-pointer ${selected?.id === item.id ? "border-orange-500" : "border-transparent"}`}>
                        <h3 className="font-semibold text-lg">{item.studentName}</h3>
                        <p>{statusLabels[item.applicationStatus]}</p>
                        <p className="text-sm text-gray-600">Applied: {item.appliedAt ? new Date(item.appliedAt).toLocaleDateString() : "Not recorded"}</p>
                      </button>
                    ))}
                  </div>
                </>}
              </>}
            </section>
            {selected && !loadingApplicants && (
              <section className="bg-white rounded-xl p-6">
                <h2 className="text-2xl font-bold mb-3">{selected.studentName}</h2>
                <p className="font-semibold mb-4">Status: {statusLabels[selected.applicationStatus]}</p>
                {loadingStudent && <p role="status">Loading student profile...</p>}
                {studentError && <p role="alert" className="text-red-700">{studentError}</p>}
                {profile && <>
                  {profile.profilePictureUrl && <img
                    src={new URL(profile.profilePictureUrl, "http://localhost:8080").href}
                    alt="Applicant profile" className="w-28 h-28 rounded-full object-cover mb-4" />}
                  <p className="mb-3">{profile.email}</p>
                  <dl className="grid grid-cols-2 gap-3">
                    {[
                      ["Major", profile.major],
                      ["Classification", profile.classification],
                      ["Graduation year", profile.graduationYear],
                      ["GPA", profile.gpa],
                      ["Hours available per week", profile.availableHoursPerWeek],
                    ].map(([label, value]) => (
                      <div key={String(label)} className="bg-gray-100 rounded-lg p-3">
                        <dt className="text-gray-600">{label}</dt>
                        <dd className="font-semibold">{value ?? "Not specified"}</dd>
                      </div>
                    ))}
                  </dl>
                  <h3 className="font-semibold mt-4">Skills</h3>
                  <p>{profile.skills?.join(", ") || "Not specified"}</p>
                  <h3 className="font-semibold mt-4">About the applicant</h3>
                  <p className="whitespace-pre-wrap">{profile.description || "Not provided"}</p>
                  <h3 className="font-semibold mt-4">Previous experience</h3>
                  <p className="whitespace-pre-wrap">{profile.previousExperience || "Not provided"}</p>
                </>}
                {actionError && <p role="alert" className="text-red-700 mt-4">{actionError}</p>}
                {message && <p role="status" className="text-green-800 mt-4">{message}</p>}
                <div className="flex flex-wrap gap-3 mt-5">
                  <button type="button" disabled={busy || uncertain || selected.applicationStatus === "ACCEPTED"}
                    onClick={() => void decide("ACCEPTED")}
                    className={`${buttonClass} bg-green-700 text-white`}>Accept</button>
                  <button type="button" disabled={busy || uncertain || selected.applicationStatus === "REJECTED"}
                    onClick={() => void decide("REJECTED")}
                    className={`${buttonClass} bg-red-700 text-white`}>Decline</button>
                  <button type="button" disabled={busy || uncertain || selected.applicationStatus === "PENDING"}
                    onClick={() => void decide("PENDING")}
                    className={`${buttonClass} bg-gray-700 text-white`}>Reset to Pending</button>
                  <button type="button" disabled={busy}
                    onClick={() => void downloadResume()}
                    className={`${buttonClass} bg-blue-700 text-white`}>Download Resume</button>
                </div>
                {busy && <p role="status" className="mt-3">Processing...</p>}
              </section>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
