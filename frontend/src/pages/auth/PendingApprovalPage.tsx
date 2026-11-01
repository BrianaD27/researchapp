import { useNavigate } from "react-router-dom";

// Shown to faculty who signed up and are waiting for an admin to approve them.
export default function PendingApprovalPage() {
  const navigate = useNavigate();

  function logInAgain() {
    sessionStorage.removeItem("researchapp.accessToken");
    sessionStorage.removeItem("researchapp.refreshToken");
    navigate("/FacultyLogin", { replace: true });
  }

  return (
    <div className="min-h-screen bg-[#1B51A4] flex items-start justify-center p-6">
      <section className="bg-white rounded-xl shadow-lg p-8 w-full max-w-lg flex flex-col gap-4 mt-10">
        <h1 className="text-2xl font-bold text-vsu-orange">Waiting for approval</h1>
        <p>
          Thanks for signing up. An app admin needs to approve your faculty account before you can
          post research and review students. You will get a notification and an email when it is approved.
        </p>
        <p className="text-sm text-gray-600">Already approved? Log in again to start.</p>
        <button type="button" onClick={logInAgain}
          className="bg-blue-700 text-white font-semibold rounded-lg p-3 cursor-pointer self-start">
          Log in again
        </button>
      </section>
    </div>
  );
}
