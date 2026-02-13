import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";

import HomePage from "./pages/index.tsx";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import FacultyLogin from "./pages/auth/FacultyLogin.tsx";
import FacultySignUp from "./pages/auth/FacultySignUp.tsx";
import StudentLogin from "./pages/auth/StudentLogin.tsx";
import StudentSignUp from "./pages/auth/StudentSignUp.tsx";
import StudentHomePage from "./pages/students/StudentHomePage.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/FacultyLogin" element={<FacultyLogin/>}/>
        <Route path="/FacultySignUp" element={<FacultySignUp/>}/>
        <Route path="/StudentLogin" element={<StudentLogin/>}/>
        <Route path="/StudentSignUp" element={<StudentSignUp/>}/>
        <Route path="/StudentHomePage" element={<StudentHomePage/>}/>
      </Routes>
    </BrowserRouter>
  </StrictMode>,
);
