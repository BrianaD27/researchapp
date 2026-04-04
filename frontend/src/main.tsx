import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";

import IndexPage from "./pages/IndexPage.tsx";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import FacultyLogin from "./pages/auth/FacultyLogin.tsx";
import FacultySignUp from "./pages/auth/FacultySignUp.tsx";
import StudentLogin from "./pages/auth/StudentLogin.tsx";
import StudentSignUp from "./pages/auth/StudentSignUp.tsx";
import StudentHomePage from "./pages/students/StudentHomePage.tsx";
import ProfessorHomePage from "./pages/professors/ProfessorHomePage.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<IndexPage />} />
        <Route path="/FacultyLogin" element={<FacultyLogin/>}/>
        <Route path="/FacultySignUp" element={<FacultySignUp/>}/>
        <Route path="/StudentLogin" element={<StudentLogin/>}/>
        <Route path="/StudentSignUp" element={<StudentSignUp/>}/>
        <Route path="/StudentHomePage" element={<StudentHomePage/>}/>
        <Route path="/ProfessorHomePage" element={<ProfessorHomePage/>}/>
        <Route path="discover-students" element={<div>Discover Students Page</div>} />
        <Route path="post-new" element={<div>Post New Opportunity Page</div>} />
        <Route path="my-opportunities" element={<div>My Opportunities Page</div>} />  
        <Route path="/faculty-view-all" element={<div>Faculty View All Opportunities Page</div>} />
        <Route path="/faculty-applicants" element={<ProfessorHomePage/>} />
        <Route path="/profile" element={<div>Profile Page</div>} />
      </Routes>
      
    </BrowserRouter>
  </StrictMode>,
);
