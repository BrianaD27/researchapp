import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import apiClient from "../../api/client";

interface TeamMember {
  userAccountId: number;
  name: string;
  title: string | null;
}

// "Research team" line on a project: the professor's Research Associates.
export default function ResearchTeam({ professorId }: { professorId: number | null | undefined }) {
  const [team, setTeam] = useState<TeamMember[]>([]);

  useEffect(() => {
    if (professorId == null) return;
    let active = true;
    apiClient.get<TeamMember[]>(`/associates/team-public/${professorId}`)
      .then(({ data }) => { if (active) setTeam(data); })
      .catch(() => { if (active) setTeam([]); });
    return () => { active = false; };
  }, [professorId]);

  if (professorId == null || team.length === 0) return null;
  return (
    <p className="text-sm text-gray-700">
      <span className="font-semibold">Research team: </span>
      {team.map((member, index) => (
        <span key={member.userAccountId}>
          {index > 0 && ", "}
          <Link to={`/associate-profile/${member.userAccountId}`} className="text-blue-700 underline">{member.name}</Link>
          {member.title && ` (${member.title})`}
        </span>
      ))}
    </p>
  );
}
