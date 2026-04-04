import React from "react";
import AuthNavBar from "../../components/common/AuthNavBar";
import AuthForm from "../../components/common/AuthForm";

const FacultyLogin = () => {
  return (
    <div className="h-screen bg-gray-100 flex flex-col">
      <div className="relative z-50">
        <AuthNavBar />
      </div>

      <img
        src="/src/assets/studentsBg.png"
        alt=""
        className="flex-1 object-cover"
      />

      <AuthForm
        title="Faculty Login"
        buttonText="Log In"
        buttonUrl="/ProfessorHomePage"
        textFields={[
          { label: "Email", type: "email", id: "faculty-email" },
          { label: "Password", type: "password", id: "faculty-password" },
        ]}
      />
    </div>
  );
};

export default FacultyLogin;
