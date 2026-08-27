import { Link, useNavigate } from "react-router-dom";

import AuthNavBar from "../../components/common/AuthNavBar";
import AuthForm from "../../components/common/AuthForm";
import { useAuth } from "../../auth/AuthContext";

const StudentLogin = () => {
  const auth = useAuth();
  const navigate = useNavigate();

  // Where to send someone once they're logged in, based on their role.
  const goToDashboard = (role?: string) => {
    if (role === "PROFESSOR") navigate("/discover-students");
    else navigate("/discover-opportunities");
  };

  // Called by AuthForm with { username, password } when the form is submitted.
  // If this throws, AuthForm catches it and shows the message.
  const handleSubmit = async (values: Record<string, string>) => {
    const result = await auth.login(values.username, values.password);

    if (result.twoFactor) {
      // The server emailed a code - go collect it.
      navigate("/verify-2fa");
      return;
    }
    goToDashboard(result.role);
  };

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
        title="Student Login"
        submitText="Log In"
        onSubmit={handleSubmit}
        fields={[
          { label: "Username", type: "text", id: "username" },
          { label: "Password", type: "password", id: "password" },
        ]}
        footer={
          <p className="text-center">
            New here?{" "}
            <Link to="/StudentSignUp" className="text-vsu-orange font-semibold">
              Create an account
            </Link>
          </p>
        }
      />
    </div>
  );
};

export default StudentLogin;
