import React from "react";
import AuthNavBar from "../../components/common/AuthNavBar";
import AuthForm from "../../components/common/AuthForm";

const StudentSignUp = () => {
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
        title="Student SignUp"
        buttonText="Log In"
        buttonUrl="/discover-opportunities"
        textFields={[
          { label: "Email", type: "email", id: "student-email" },
          { label: "Password", type: "password", id: "student-password" },
        ]}
      />
    </div>
  );
};

export default StudentSignUp;
