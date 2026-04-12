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
import ProfessorViewAllPage from "./pages/professors/ProfessorViewAllPage.tsx";
import ProfessorProfilePage from "./pages/professors/ProfessorProfilePage.tsx";

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

        {/* Student Routes */}
        <Route path="/discover-opportunities" element={<StudentDiscoverPage/>} />
        <Route path="/saved-opportunities" element={<StudentBookmarksPage />} />
        <Route path="/applied-opportunities" element={<StudentAppliedPage/>} />
        <Route path="/completed-opportunities" element={<StudentCompletedPage/>} />
        
        
        {/* Professor Routes */}
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
        <Route path="/faculty-profile" element={<ProfessorProfilePage />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>,
);
