import { useEffect, useRef, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import { Link, useSearchParams } from "react-router-dom";
import AuthNavBar from "../../components/common/AuthNavBar";
import apiClient from "../../api/client";
import studentsBg from "../../assets/studentsBg.png";

type Status = "checking" | "verified" | "failed" | "no-token";

// Opened from the activation email. Confirms the account's VSU email,
// or lets the person ask for a new link.
export default function VerifyEmailPage() {
  const [params] = useSearchParams();
  const [token] = useState(() => params.get("token") ?? "");
  const [status, setStatus] = useState<Status>(token ? "checking" : "no-token");
  const [failure, setFailure] = useState("");
  const [identifier, setIdentifier] = useState("");
  const [busy, setBusy] = useState(false);
  const [resent, setResent] = useState(false);
  const [resendError, setResendError] = useState("");
  const started = useRef(false);

  // Keep the one-time token out of the address bar and browser history.
  useEffect(() => {
    if (window.location.search) {
      window.history.replaceState(null, "", window.location.pathname);
    }
  }, []);

  useEffect(() => {
    if (!token || started.current) return;
    started.current = true;
    apiClient.post("/v1/auth/verify-email", { token })
      .then(() => setStatus("verified"))
      .catch((caught) => {
        const reason = isAxiosError(caught) ? caught.response?.data?.error : undefined;
        setFailure(typeof reason === "string" && reason ? reason : "This activation link did not work");
        setStatus("failed");
      });
  }, [token]);

  async function resend(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setResendError("");
    setBusy(true);
    try {
      await apiClient.post("/v1/auth/resend-verification", { usernameOrEmail: identifier.trim() });
      setResent(true);
    } catch (caught) {
      if (isAxiosError(caught) && caught.response?.status === 429) {
        setResendError("Too many requests. Please wait a minute and try again.");
      } else {
        setResendError("Could not send a new link. Please try again.");
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <AuthNavBar />
      <main
        className="min-h-[85vh] flex items-center justify-center p-6 bg-cover bg-center"
        style={{ backgroundImage: `url(${studentsBg})` }}
      >
        <section className="bg-white rounded-xl shadow-lg p-8 w-full max-w-lg flex flex-col gap-4">
          <h1 className="text-3xl font-bold text-vsu-orange">Activate Account</h1>

          {status === "checking" && <p role="status">Checking your activation link...</p>}

          {status === "verified" && (
            <div role="status" className="flex flex-col gap-4">
              <p>Your VSU email is confirmed. You can log in now.</p>
              <Link className="bg-blue-700 text-white rounded-lg p-3 text-center" to="/StudentLogin">
                Go to Student Login
              </Link>
              <Link className="text-blue-700 underline" to="/FacultyLogin">Faculty Login</Link>
            </div>
          )}

          {(status === "failed" || status === "no-token") && (
            <>
              {status === "failed" && (
                <p role="alert" className="text-red-700">{failure}. You can request a new link below.</p>
              )}
              {status === "no-token" && (
                <p>Enter your username or VSU email and we will send a new activation link.</p>
              )}
              {resent ? (
                <p role="status">
                  If that account is waiting for activation, a new link is on its way. Check your VSU inbox.
                </p>
              ) : (
                <form onSubmit={resend} className="flex flex-col gap-4">
                  {resendError && <p role="alert" className="text-red-700">{resendError}</p>}
                  <label className="flex flex-col gap-1">
                    Username or VSU email
                    <input
                      required
                      value={identifier}
                      disabled={busy}
                      onChange={(event) => setIdentifier(event.target.value)}
                      className="w-full border border-gray-400 rounded-lg p-3"
                    />
                  </label>
                  <button
                    type="submit"
                    disabled={busy}
                    className="bg-blue-700 text-white font-semibold rounded-lg p-3 cursor-pointer disabled:opacity-50"
                  >
                    {busy ? "Please wait..." : "Send a new link"}
                  </button>
                </form>
              )}
              <Link className="text-blue-700 underline" to="/StudentLogin">Back to login</Link>
            </>
          )}
        </section>
      </main>
    </div>
  );
}
