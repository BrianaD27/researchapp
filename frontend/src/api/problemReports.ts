import { isAxiosError } from "axios";

// Shared by the Report a Problem page and the admin Problem Reports page.
export interface ProblemReport {
  id: number;
  category: string;
  description: string;
  page: string | null;
  browser: string | null;
  reporterUsername: string;
  status: "NEW" | "IN_PROGRESS" | "FIXED";
  adminReply: string | null;
  createdAt: string;
  updatedAt: string;
}

export const REPORT_CATEGORIES = [
  "Something is broken",
  "Can't find something",
  "Account or login",
  "Suggestion",
  "Other",
];

export const STATUS_LABEL: Record<ProblemReport["status"], string> = {
  NEW: "New",
  IN_PROGRESS: "In progress",
  FIXED: "Fixed",
};

export const STATUS_STYLE: Record<ProblemReport["status"], string> = {
  NEW: "bg-amber-100 text-amber-900",
  IN_PROGRESS: "bg-blue-100 text-blue-900",
  FIXED: "bg-green-100 text-green-900",
};

export function reportProblem(caught: unknown, fallback: string) {
  if (isAxiosError(caught)) {
    if (!caught.response) return "Cannot reach the backend. Check that Java is running.";
    if (caught.response.status === 401) return "Your login expired. Log in again.";
    const data = caught.response.data as { error?: string } | undefined;
    if (data?.error) return String(data.error);
  }
  return fallback;
}
