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

    // Registration does not log you in, and the next page (the profile form)
    // needs a logged-in user to save anything. So log in right now using the
    // same username/password the user just typed.
    const result = await auth.login(values.username, values.password);

    // Brand new accounts won't have 2FA turned on, but handle it just in case.
    if (result.twoFactor) {
      navigate("/verify-2fa");
      return;
    }

    // Now logged in - go fill out the faculty profile. Pass along the email so
    // the info page can pre-fill it (the backend profile needs a matching one).
    navigate("/faculty-info", { state: { email: values.email } });
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
