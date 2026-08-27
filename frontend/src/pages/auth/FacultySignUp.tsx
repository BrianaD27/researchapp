import { Link, useNavigate } from "react-router-dom";

import AuthNavBar from "../../components/common/AuthNavBar";
import AuthForm from "../../components/common/AuthForm";
import { useAuth } from "../../auth/AuthContext";

const FacultySignUp = () => {
  const auth = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (values: Record<string, string>) => {
    // "Faculty" in the UI maps to the backend role "PROFESSOR".
    await auth.register({
      username: values.username,
      email: values.email,
      password: values.password,
      role: "PROFESSOR",
    });

    navigate("/faculty-info");
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
        title="Faculty SignUp"
        submitText="Sign Up"
        onSubmit={handleSubmit}
        fields={[
          { label: "Username", type: "text", id: "username" },
          { label: "VSU Email", type: "email", id: "email" },
          { label: "Password", type: "password", id: "password" },
        ]}
        footer={
          <p className="text-center">
            Already have an account?{" "}
            <Link to="/FacultyLogin" className="text-vsu-orange font-semibold">
              Log in
            </Link>
          </p>
        }
      />
    </div>
  );
};

export default FacultySignUp;
