// ---------------------------------------------------------------------------
// Verify2FA.tsx  —  shown only when the backend asks for a 6-digit email code
// ---------------------------------------------------------------------------
//
// The flow: user submits username + password on the login page. If their account
// has two-factor auth turned on, the backend emails them a code and replies
// "2FA_REQUIRED". The login page then sends them here to type that code.

import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

import AuthNavBar from "../../components/common/AuthNavBar";
import AuthForm from "../../components/common/AuthForm";
import { useAuth } from "../../auth/AuthContext";

const Verify2FA = () => {
  const auth = useAuth();
  const navigate = useNavigate();

  // If someone lands here directly (no login in progress), send them to login.
  useEffect(() => {
    if (!auth.pending2fa) {
      navigate("/StudentLogin", { replace: true });
    }
  }, [auth.pending2fa, navigate]);

  const handleSubmit = async (values: Record<string, string>) => {
    const { role } = await auth.verify2fa(values.code);
    if (role === "PROFESSOR") navigate("/discover-students");
    else navigate("/discover-opportunities");
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
        title="Enter Code"
        submitText="Verify"
        onSubmit={handleSubmit}
        fields={[{ label: "6-digit code from your email", type: "text", id: "code" }]}
      />
    </div>
  );
};

export default Verify2FA;
