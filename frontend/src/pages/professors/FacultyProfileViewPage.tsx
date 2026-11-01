import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import apiClient from "../../api/client";
import { Chip, ProfileCard, ProfileHero, ProfileLayout } from "../../components/profile/ProfileParts";
import { actionClass, formatDate, hasText } from "../../components/profile/profileUtils";

interface Professor {
  id: number;
  name: string;
  email: string | null;
  department: string | null;
  officeLocation: string | null;
  description: string | null;
  profilePictureUrl: string | null;
  outsideInterests: string | null;
  education: { degree: string; school: string; graduationYear: string | null; inProgress?: boolean | null }[] | null;
}

interface Opportunity {
  id: number;
  title: string;
  department: string | null;
  availability: string | null;
  applicationDeadline: string | null;
}

interface Application {
  id: number;
  studentName: string;
  opportunityStatus: string;
}

interface PastProject {
  id: number;
  title: string;
  students: string[];
}

export default function FacultyProfileViewPage() {
  const navigate = useNavigate();
  const fromLogin = Boolean((useLocation().state as { fromLogin?: boolean } | null)?.fromLogin);
  const [professor, setProfessor] = useState<Professor | null>(null);
  const [projects, setProjects] = useState<Opportunity[]>([]);
  const [past, setPast] = useState<PastProject[]>([]);
  const [notice, setNotice] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const { data: context } = await apiClient.get<{ admin: boolean; professorId: number | null }>(
          "/faculty-review/context");
        if (context.professorId === null) {
          // Administrators without their own faculty profile start on Discover Students.
          if (context.admin && fromLogin) {
            navigate("/discover-students", { replace: true });
            return;
          }
          if (active) setNotice(context.admin
            ? "This administrator account has no faculty profile of its own. Use Profile Settings to edit faculty profiles."
            : "This account has no linked faculty profile. Ask a faculty administrator to link your account.");
          return;
        }
        const [{ data: profile }, { data: owned }] = await Promise.all([
          apiClient.get<Professor>(`/professors/${context.professorId}`),
          apiClient.get<Opportunity[]>(`/research-opportunities/professor/${context.professorId}`),
        ]);
        if (!active) return;
        setProfessor(profile);
        setProjects(owned);
        const finished = await Promise.all(owned.map(async (project) => {
          try {
            const { data } = await apiClient.get<Application[]>(`/applications/opportunity/${project.id}`);
            const students = data.filter((item) => item.opportunityStatus === "COMPLETED").map((item) => item.studentName);
            return { id: project.id, title: project.title, students };
          } catch {
            return { id: project.id, title: project.title, students: [] };
          }
        }));
        if (active) setPast(finished.filter((item) => item.students.length > 0));
      } catch {
        if (active) setError("Could not load your profile. Refresh the page to try again.");
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, [fromLogin, navigate]);

  const education = professor?.education ?? [];

  return (
    <div className="min-h-screen bg-[#F4F6FA]">
      <FacultyNavBar />
      {loading && <p role="status" className="max-w-6xl mx-auto p-6">Loading your profile...</p>}
      {error && <p role="alert" className="max-w-6xl mx-auto p-6 text-red-700">{error}</p>}
      {notice && (
        <section className="max-w-xl mx-auto mt-10 bg-white rounded-xl p-6 flex flex-col gap-3">
          <p>{notice}</p>
          <Link to="/faculty-settings" className={`${actionClass} self-start`}>Profile Settings</Link>
        </section>
      )}
      {professor && (
        <>
          <ProfileHero
            name={professor.name}
            photoUrl={professor.profilePictureUrl}
            lines={[
              professor.department,
              [professor.email, hasText(professor.officeLocation) ? `Office: ${professor.officeLocation}` : null]
                .filter(Boolean).join(" \u00B7 "),
            ]}
            actions={<Link to="/faculty-settings" className={actionClass}>Edit Profile</Link>}
          />
          <ProfileLayout
            main={<>
              {hasText(professor.description) && (
                <ProfileCard title="About">
                  <p className="leading-relaxed text-gray-700 whitespace-pre-wrap">{professor.description}</p>
                </ProfileCard>
              )}
              {hasText(professor.outsideInterests) && (
                <ProfileCard title="Outside the Classroom">
                  <p className="leading-relaxed text-gray-700 whitespace-pre-wrap">{professor.outsideInterests}</p>
                </ProfileCard>
              )}
              {education.length > 0 && (
                <ProfileCard title="Education">
                  <ul className="flex flex-col gap-3">
                    {education.map((item, index) => (
                      <li key={index} className="flex flex-wrap justify-between gap-x-4 gap-y-1 border-b border-[#DDE3EC] pb-3 last:border-b-0 last:pb-0">
                        <span className="flex flex-col">
                          <span className="font-semibold">
                            {item.degree}
                            {item.inProgress && <span className="ml-2 align-middle"><Chip>In progress</Chip></span>}
                          </span>
                          <span className="text-gray-700">{item.school}</span>
                        </span>
                        {hasText(item.graduationYear) && (
                          <span className="text-gray-600">
                            {item.inProgress ? `Expected ${item.graduationYear}` : item.graduationYear}
                          </span>
                        )}
                      </li>
                    ))}
                  </ul>
                </ProfileCard>
              )}
              {projects.length > 0 && (
                <ProfileCard title="Current Projects">
                  {projects.map((project) => {
                    const open = project.availability?.toLowerCase() === "open";
                    const closes = formatDate(project.applicationDeadline);
                    return (
                      <article key={project.id} className="border border-[#DDE3EC] rounded-lg p-4 flex flex-col gap-1.5">
                        <div className="flex flex-wrap justify-between items-center gap-2">
                          <h3 className="text-lg font-semibold">{project.title}</h3>
                          <Chip tone={open ? "green" : "gray"}>{project.availability || "Status not set"}</Chip>
                        </div>
                        <p className="text-gray-700">
                          {[project.department, closes && `Applications close ${closes}`].filter(Boolean).join(" \u00B7 ")}
                        </p>
                      </article>
                    );
                  })}
                </ProfileCard>
              )}
              {past.length > 0 && (
                <ProfileCard title="Past Research" note="Completed projects and students mentored">
                  {past.map((project) => (
                    <article key={project.id} className="border border-[#DDE3EC] rounded-lg p-4 flex flex-col gap-1.5">
                      <h3 className="text-lg font-semibold">{project.title}</h3>
                      <p className="text-gray-700">Students: {project.students.join(", ")}</p>
                    </article>
                  ))}
                </ProfileCard>
              )}
            </>}
            side={
              <ProfileCard title="Contact">
                <dl className="flex flex-col gap-3">
                  {[
                    ["Email", professor.email],
                    ["Office", professor.officeLocation],
                  ].filter(([, value]) => hasText(value)).map(([label, value]) => (
                    <div key={label}>
                      <dt className="text-sm text-gray-600">{label}</dt>
                      <dd className="font-semibold break-words">{value}</dd>
                    </div>
                  ))}
                </dl>
              </ProfileCard>
            }
          />
        </>
      )}
    </div>
  );
}
