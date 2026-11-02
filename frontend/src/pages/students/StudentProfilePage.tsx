import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import { Link } from "react-router-dom";
import StudentNavBar from "../../components/common/StudentNavBar";
import apiClient from "../../api/client";
import { usePresence } from "../../api/presence";
import ActiveToggle from "../../components/common/ActiveToggle";
import { Chip, ProfileCard, ProfileHero, ProfileLayout } from "../../components/profile/ProfileParts";
import { actionClass, formatDate, hasText } from "../../components/profile/profileUtils";

interface Student {
  id: number;
  name: string;
  email: string;
  major: string | null;
  classification: string | null;
  graduationYear: number | null;
  description: string | null;
  previousExperience: string | null;
  outsideInterests: string | null;
  gpa: number | null;
  availableHoursPerWeek: number | null;
  profilePictureUrl: string | null;
  skills: string[] | null;
}

interface Application {
  id: number;
  researchOpportunityId: number;
  researchOpportunityTitle: string;
  opportunityStatus: string;
  applicationStatus: string;
  mentorNote: string | null;
}

interface Opportunity {
  id: number;
  title: string;
  department: string | null;
  professorName: string | null;
  startDate: string | null;
  endDate: string | null;
}

interface Completed {
  id: number;
  ongoing: boolean;
  title: string;
  mentor: string | null;
  department: string | null;
  startDate: string | null;
  endDate: string | null;
  mentorNote: string | null;
}

export default function StudentProfilePage() {
  const [student, setStudent] = useState<Student | null>(null);
  const online = usePresence("students", student ? [student.id] : []);
  const [completed, setCompleted] = useState<Completed[]>([]);
  const [missing, setMissing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const { data } = await apiClient.get<Student>("/students/me");
        if (!active) return;
        setStudent(data);
        const { data: applications } = await apiClient.get<Application[]>(`/applications/student/${data.id}`);
        const done = applications.filter((item) => item.applicationStatus === "ACCEPTED");
        const details = await Promise.all(done.map(async (item) => {
          try {
            const { data: opportunity } = await apiClient.get<Opportunity>(
              `/research-opportunities/${item.researchOpportunityId}`);
            return {
              id: item.id, ongoing: item.opportunityStatus !== "COMPLETED",
              title: opportunity.title, mentor: opportunity.professorName,
              department: opportunity.department, startDate: opportunity.startDate, endDate: opportunity.endDate,
              mentorNote: item.mentorNote,
            };
          } catch {
            return {
              id: item.id, ongoing: item.opportunityStatus !== "COMPLETED",
              title: item.researchOpportunityTitle, mentor: null,
              department: null, startDate: null, endDate: null, mentorNote: item.mentorNote,
            };
          }
        }));
        if (active) setCompleted(details);
      } catch (caught) {
        if (!active) return;
        if (isAxiosError(caught) && caught.response?.status === 404) setMissing(true);
        else setError("Could not load your profile. Refresh the page to try again.");
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, []);

  const current = completed.filter((item) => item.ongoing);
  const finished = completed.filter((item) => !item.ongoing);

  const years = (item: Completed) => {
    const start = formatDate(item.startDate);
    const end = formatDate(item.endDate);
    if (start && end) return `${start} \u2013 ${end}`;
    return start ?? end;
  };

  return (
    <div className="min-h-screen bg-[#F4F6FA]">
      <StudentNavBar />
      {loading && <p role="status" className="max-w-6xl mx-auto p-6">Loading your profile...</p>}
      {error && <p role="alert" className="max-w-6xl mx-auto p-6 text-red-700">{error}</p>}
      {missing && (
        <section className="max-w-xl mx-auto mt-10 bg-white rounded-xl p-6 flex flex-col gap-3">
          <h1 className="text-2xl font-bold">Set up your profile</h1>
          <p>You have not created your student profile yet.</p>
          <Link to="/student-settings" className={`${actionClass} self-start`}>Create Profile</Link>
        </section>
      )}
      {student && (
        <>
          <ProfileHero
            name={student.name}
            photoUrl={student.profilePictureUrl}
            active={Boolean(online[student.id])}
            lines={[
              [student.major, student.classification, student.graduationYear ? `Class of ${student.graduationYear}` : null]
                .filter(Boolean).join(" \u00B7 "),
              student.email,
            ]}
            actions={<><Link to="/student-settings" className={actionClass}>Edit Profile</Link><ActiveToggle /></>}
          />
          <ProfileLayout
            main={<>
              {hasText(student.description) && (
                <ProfileCard title="About">
                  <p className="leading-relaxed text-gray-700 whitespace-pre-wrap">{student.description}</p>
                </ProfileCard>
              )}
              {hasText(student.outsideInterests) && (
                <ProfileCard title="Outside the Classroom">
                  <p className="leading-relaxed text-gray-700 whitespace-pre-wrap">{student.outsideInterests}</p>
                </ProfileCard>
              )}
              {hasText(student.previousExperience) && (
                <ProfileCard title="Experience">
                  <p className="leading-relaxed text-gray-700 whitespace-pre-wrap">{student.previousExperience}</p>
                </ProfileCard>
              )}
              {current.length > 0 && (
                <ProfileCard title="Current Research" note="Projects in progress">
                  {current.map((item) => (
                    <article key={item.id} className="border border-[#DDE3EC] rounded-lg p-4 flex flex-col gap-1.5">
                      <div className="flex flex-wrap justify-between items-center gap-2">
                        <h3 className="text-lg font-semibold">{item.title}</h3>
                        <Chip>Ongoing</Chip>
                      </div>
                      {(item.mentor || item.department) && (
                        <p className="text-gray-700">
                          {[item.mentor && `Mentor: ${item.mentor}`, item.department].filter(Boolean).join(" \u00B7 ")}
                        </p>
                      )}
                      {years(item) && <p className="text-gray-700">{years(item)}</p>}
                    </article>
                  ))}
                </ProfileCard>
              )}
              {finished.length > 0 && (
                <ProfileCard title="Research History" note="From completed VSU projects">
                  {finished.map((item) => (
                    <article key={item.id} className="border border-[#DDE3EC] rounded-lg p-4 flex flex-col gap-1.5">
                      <div className="flex flex-wrap justify-between items-center gap-2">
                        <h3 className="text-lg font-semibold">{item.title}</h3>
                        <Chip tone="green">Completed</Chip>
                      </div>
                      {(item.mentor || item.department) && (
                        <p className="text-gray-700">
                          {[item.mentor && `Mentor: ${item.mentor}`, item.department].filter(Boolean).join(" \u00B7 ")}
                        </p>
                      )}
                      {years(item) && <p className="text-gray-700">{years(item)}</p>}
                      {item.mentorNote && (
                        <blockquote className="mt-1 border-l-4 border-orange-400 pl-3 text-gray-800 italic whitespace-pre-wrap">
                          {item.mentorNote}
                          {item.mentor && <span className="block not-italic text-sm text-gray-600 mt-1">{`- ${item.mentor}`}</span>}
                        </blockquote>
                      )}
                    </article>
                  ))}
                </ProfileCard>
              )}
            </>}
            side={<>
              {(student.skills ?? []).length > 0 && (
                <ProfileCard title="Skills">
                  <div className="flex flex-wrap gap-2">
                    {(student.skills ?? []).map((skill) => <Chip key={skill}>{skill}</Chip>)}
                  </div>
                </ProfileCard>
              )}
              <ProfileCard title="Details">
                <dl className="grid grid-cols-2 gap-3">
                  {[
                    ["Major", student.major],
                    ["Classification", student.classification],
                    ["Graduation", student.graduationYear],
                    ["Hours per week", student.availableHoursPerWeek],
                  ].filter(([, value]) => value !== null && value !== undefined && value !== "").map(([label, value]) => (
                    <div key={String(label)}>
                      <dt className="text-sm text-gray-600">{label}</dt>
                      <dd className="font-semibold">{value}</dd>
                    </div>
                  ))}
                </dl>
                {student.gpa !== null && (
                  <div className="border-t border-[#DDE3EC] pt-3">
                    <p className="text-sm text-gray-600">GPA {"\u00B7"} visible to you and faculty only</p>
                    <p className="font-semibold">{student.gpa}</p>
                  </div>
                )}
              </ProfileCard>
            </>}
          />
        </>
      )}
    </div>
  );
}
