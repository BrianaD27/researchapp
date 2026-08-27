import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";

import IndexPage from "./pages/IndexPage.tsx";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import FacultyLogin from "./pages/auth/FacultyLogin.tsx";
import FacultySignUp from "./pages/auth/FacultySignUp.tsx";
import StudentLogin from "./pages/auth/StudentLogin.tsx";
import StudentSignUp from "./pages/auth/StudentSignUp.tsx";
import Verify2FA from "./pages/auth/Verify2FA.tsx";
import ProfessorApplicantsPage from "./pages/professors/ProfessorApplicantsPage.tsx";
import ProfessorDiscoverPage from "./pages/professors/ProfessorDiscoverPage.tsx";
import ProfessorPostPage from "./pages/professors/ProfessorPostPage.tsx";
import StudentDiscoverPage from "./pages/students/StudentDiscoverPage.tsx";
import StudentBookmarksPage from "./pages/students/StudentBookmarksPage.tsx";
import StudentAppliedPage from "./pages/students/StudentAppliedPage.tsx";
import StudentCompletedPage from "./pages/students/StudentCompletedPage.tsx";
import ProfessorViewAllPage from "./pages/professors/ProfessorViewAllPage.tsx";
import ProfessorProfilePage from "./pages/professors/ProfessorProfilePage.tsx";
import StudentSettingsPage from "./pages/students/StudentSettingsPage.tsx";
import FacultyInfoPage from "./pages/auth/FacultyInfoPage.tsx";
import StudentInfoPage from "./pages/auth/StudentInfoPage.tsx";
import { AuthProvider } from "./auth/AuthContext.tsx";
import ProtectedRoute from "./auth/ProtectedRoute.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    {/* BrowserRouter must be OUTSIDE AuthProvider because AuthProvider uses
        useNavigate() (e.g. to redirect home on logout). */}
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* ---- Public routes: anyone can reach these ---- */}
          <Route path="/" element={<IndexPage />} />
          <Route path="/FacultyLogin" element={<FacultyLogin />} />
          <Route path="/FacultySignUp" element={<FacultySignUp />} />
          <Route path="/StudentLogin" element={<StudentLogin />} />
          <Route path="/StudentSignUp" element={<StudentSignUp />} />
          <Route path="/verify-2fa" element={<Verify2FA />} />

          {/* Profile forms shown right after signup - kept public so a brand new
              user (not logged in yet) can fill them out. */}
          <Route path="/faculty-info" element={<FacultyInfoPage />} />
          <Route path="/student-info" element={<StudentInfoPage />} />

          {/* ---- Student area: must be logged in as STUDENT (or ADMIN) ---- */}
          <Route element={<ProtectedRoute allow={["STUDENT", "ADMIN"]} />}>
            <Route path="/discover-opportunities" element={<StudentDiscoverPage />} />
            <Route path="/saved-opportunities" element={<StudentBookmarksPage />} />
            <Route path="/applied-opportunities" element={<StudentAppliedPage />} />
            <Route path="/completed-opportunities" element={<StudentCompletedPage />} />
            <Route path="/student-settings" element={<StudentSettingsPage />} />
          </Route>

          {/* ---- Professor area: must be logged in as PROFESSOR (or ADMIN) ---- */}
          <Route element={<ProtectedRoute allow={["PROFESSOR", "ADMIN"]} />}>
            <Route path="/discover-students" element={<ProfessorDiscoverPage />} />
            <Route path="/post-new" element={<ProfessorPostPage />} />
            <Route path="/my-opportunities" element={<div>My Opportunities Page</div>} />
            <Route path="/faculty-view-all" element={<ProfessorViewAllPage />} />
            <Route path="/faculty-applicants" element={<ProfessorApplicantsPage />} />
            <Route path="/faculty-settings" element={<ProfessorProfilePage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
);
