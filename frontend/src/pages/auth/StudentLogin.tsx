import React from "react";
import NavBar from "../../components/common/NavBar";
import AuthForm from "../../components/common/AuthForm";

const StudentLogin = () => {
  return (
    <div className="h-screen bg-gray-100 flex flex-col">
      <div className="relative z-50">
        <NavBar />
      </div>

      <img
        src="/src/assets/studentsBg.png"
        alt=""
        className="flex-1 object-cover"
      />

      <AuthForm
        title="Student Login"
        buttonText="Log In"
        buttonUrl="/StudentHomePage"
        textFields={[
          { label: "Email", type: "email", id: "student-email" },
          { label: "Password", type: "password", id: "student-password" },
        ]}
      />
    </div>
  );
};

export default StudentLogin;
