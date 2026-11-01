import FacultySession from "./components/common/FacultySession";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";

import IndexPage from "./pages/IndexPage.tsx";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import FacultyLogin from "./pages/auth/FacultyLogin.tsx";
import FacultySignUp from "./pages/auth/FacultySignUp.tsx";
import StudentLogin from "./pages/auth/StudentLogin.tsx";
import StudentSignUp from "./pages/auth/StudentSignUp.tsx";
import ProfessorApplicantsPage from "./pages/professors/ProfessorApplicantsPage.tsx";
import ProfessorDiscoverPage from "./pages/professors/ProfessorDiscoverPage.tsx";
import ProfessorPostPage from "./pages/professors/ProfessorPostPage.tsx";
import StudentDiscoverPage from "./pages/students/StudentDiscoverPage.tsx";
import StudentBookmarksPage from "./pages/students/StudentBookmarksPage.tsx";
import StudentAppliedPage from "./pages/students/StudentAppliedPage.tsx";
import StudentCompletedPage from "./pages/students/StudentCompletedPage.tsx";
import StudentOngoingPage from "./pages/students/StudentOngoingPage.tsx";
import ProfessorViewAllPage from "./pages/professors/ProfessorViewAllPage.tsx";
import ProfessorProfilePage from "./pages/professors/ProfessorProfilePage.tsx";
import ProfessorAssociatesPage from "./pages/professors/ProfessorAssociatesPage.tsx";
import AdminUsersPage from "./pages/admin/AdminUsersPage.tsx";
import FacultyInfoPage from "./pages/auth/FacultyInfoPage.tsx";
import StudentInfoPage from "./pages/auth/StudentInfoPage.tsx";
import ForgotPasswordPage from "./pages/auth/ForgotPasswordPage.tsx";
import ResetPasswordPage from "./pages/auth/ResetPasswordPage.tsx";
import VerifyEmailPage from "./pages/auth/VerifyEmailPage.tsx";
import ReportProblemPage from "./pages/common/ReportProblemPage.tsx";
import AdminReportsPage from "./pages/admin/AdminReportsPage.tsx";
import StudentProfilePage from "./pages/students/StudentProfilePage.tsx";
import FacultyProfileViewPage from "./pages/professors/FacultyProfileViewPage.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        {/* Auth Routes */}
        <Route path="/" element={<IndexPage />} />
        <Route path="/FacultyLogin" element={<FacultyLogin />} />
        <Route path="/FacultySignUp" element={<FacultySignUp />} />
        <Route path="/StudentLogin" element={<StudentLogin />} />
        <Route path="/StudentSignUp" element={<StudentSignUp />} />
        <Route path="/faculty-info" element={<FacultyInfoPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />


        {/* Student Routes */}
        <Route element={<FacultySession loginPath="/StudentLogin" />}>
        <Route path="/student-info" element={<StudentInfoPage />} />
        <Route path="/discover-opportunities" element={<StudentDiscoverPage/>} />
        <Route path="/saved-opportunities" element={<StudentBookmarksPage />} />
        <Route path="/applied-opportunities" element={<StudentAppliedPage/>} />
        <Route path="/ongoing-opportunities" element={<StudentOngoingPage />} />
        <Route path="/completed-opportunities" element={<StudentCompletedPage/>} />
        <Route path="/student-settings" element={<StudentInfoPage />} />
        <Route path="/my-profile" element={<StudentProfilePage />} />
        
        
        </Route>
        {/* Professor Routes */}
        <Route element={<FacultySession />}>
        <Route
          path="discover-students"
          element={<ProfessorDiscoverPage />}
        />
        <Route path="post-new" element={<ProfessorPostPage/>} />
        <Route
          path="my-opportunities"
          element={<div>My Opportunities Page</div>}
        />
        <Route
          path="/faculty-view-all"
          element={<ProfessorViewAllPage />}
        />
        <Route
          path="/faculty-applicants"
          element={<ProfessorApplicantsPage />}
        />
        <Route path="/faculty-settings" element={<ProfessorProfilePage />} />
        <Route path="/faculty-associates" element={<ProfessorAssociatesPage />} />
        <Route path="/admin-users" element={<AdminUsersPage />} />
        <Route path="/admin-reports" element={<AdminReportsPage />} />
        <Route path="/report-problem" element={<ReportProblemPage />} />
        <Route path="/faculty-profile" element={<FacultyProfileViewPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </StrictMode>,
);
