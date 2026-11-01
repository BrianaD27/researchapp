// "Recommended for you": a simple, explainable fit score between a student and a
// project, based on the project's own requirements. No hidden data is used.

export interface FitStudent {
  major: string | null;
  classification: string | null;
  gpa: number | null;
  skills: string[] | null;
}

export interface FitProject {
  requiredMajors: string[] | null;
  requiredClassifications: string[] | null;
  requiredSkills: string[] | null;
  minimumGpa: number | null;
}

export interface Fit {
  score: number;
  eligible: boolean;
  reasons: string[];
}

const norm = (value: string | null | undefined) => (value ?? "").trim().toLowerCase();

export function fit(student: FitStudent, project: FitProject): Fit {
  const reasons: string[] = [];
  let score = 0;
  let eligible = true;

  if (project.minimumGpa != null && student.gpa != null) {
    if (student.gpa < project.minimumGpa) eligible = false;
    else { score += 1; reasons.push(`Meets the ${project.minimumGpa} GPA minimum`); }
  }

  const majors = (project.requiredMajors ?? []).map(norm).filter(Boolean);
  if (majors.length && student.major && majors.includes(norm(student.major))) {
    score += 3;
    reasons.push(`Major: ${student.major}`);
  }

  const levels = (project.requiredClassifications ?? []).map(norm).filter(Boolean);
  if (levels.length && student.classification && levels.includes(norm(student.classification))) {
    score += 2;
    reasons.push(`Open to ${student.classification}s`);
  }

  const wanted = new Set((project.requiredSkills ?? []).map(norm).filter(Boolean));
  const shared = (student.skills ?? []).filter((skill) => wanted.has(norm(skill)));
  if (shared.length) {
    score += Math.min(shared.length, 4);
    reasons.push(`Skills: ${shared.slice(0, 4).join(", ")}`);
  }

  return { score, eligible, reasons };
}

// Good enough to recommend: eligible and a real match on something beyond GPA.
export function isRecommended(result: Fit) {
  return result.eligible && result.score >= 3;
}
