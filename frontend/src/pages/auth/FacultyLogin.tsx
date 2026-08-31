import { Link, useNavigate } from "react-router-dom";

import AuthNavBar from "../../components/common/AuthNavBar";
import AuthForm from "../../components/common/AuthForm";
import { useAuth } from "../../auth/AuthContext";

const FacultyLogin = () => {
  const auth = useAuth();
  const navigate = useNavigate();

  const goToDashboard = (role?: string) => {
    if (role === "STUDENT") navigate("/discover-opportunities");
    else navigate("/discover-students");
  };

  const handleSubmit = async (values: Record<string, string>) => {
    const result = await auth.login(values.username, values.password);

    if (result.twoFactor) {
      navigate("/verify-2fa");
      return;
    }

    // This is the FACULTY login page. If a student's credentials were used,
    // the login technically worked (there's one login endpoint for everyone),
    // so undo it and show an error instead of letting them in here.
    if (result.role !== "PROFESSOR" && result.role !== "ADMIN") {
      auth.abandonSession();
      throw new Error(
        "That account isn't a faculty account. Please use the Student login page.",
      );
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
        title="Faculty Login"
        submitText="Log In"
        onSubmit={handleSubmit}
        fields={[
          { label: "Username", type: "text", id: "username" },
          { label: "Password", type: "password", id: "password" },
        ]}
        footer={
          <p className="text-center">
            New here?{" "}
            <Link to="/FacultySignUp" className="text-vsu-orange font-semibold">
              Create an account
            </Link>
          </p>
        }
      />
    </div>
  );
};

export default FacultyLogin;
