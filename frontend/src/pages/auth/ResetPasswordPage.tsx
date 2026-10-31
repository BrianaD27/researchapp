import { useEffect, useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import { Link, useSearchParams } from "react-router-dom";
import AuthNavBar from "../../components/common/AuthNavBar";
import apiClient from "../../api/client";
import studentsBg from "../../assets/studentsBg.png";

function passwordProblems(password: string) {
  const problems: string[] = [];
  if (password.length < 8) problems.push("at least 8 characters");
  if (new TextEncoder().encode(password).length > 72) problems.push("no more than 72 characters");
  if (!/[A-Z]/.test(password)) problems.push("an uppercase letter");
  if (!/[a-z]/.test(password)) problems.push("a lowercase letter");
  if (!/\d/.test(password)) problems.push("a number");
  if (!/[^\p{L}\p{N}]/u.test(password)) problems.push("a symbol or space");
  return problems;
}

export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const [token] = useState(() => params.get("token") ?? "");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  // Keep the one-time token out of the address bar and browser history.
  useEffect(() => {
    if (window.location.search) {
      window.history.replaceState(null, "", window.location.pathname);
    }
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy || done) return;
    setError("");
    const problems = passwordProblems(password);
    if (problems.length) {
      setError(`Password needs ${problems.join(", ")}.`);
      return;
    }
    if (password !== confirm) {
      setError("The passwords do not match.");
      return;
    }
    setBusy(true);
    try {
      await apiClient.post("/v1/auth/reset-password", { token, newPassword: password });
      sessionStorage.removeItem("researchapp.accessToken");
      sessionStorage.removeItem("researchapp.refreshToken");
      setPassword("");
      setConfirm("");
      setDone(true);
    } catch (caught) {
      if (isAxiosError(caught) && !caught.response) {
        setError("Cannot reach the backend. Check that Java is running.");
      } else if (isAxiosError(caught) && caught.response?.status === 429) {
        setError("Too many requests. Please wait a minute and try again.");
      } else {
        const reason = isAxiosError(caught) ? caught.response?.data?.error : undefined;
        setError(typeof reason === "string" && reason
          ? `${reason}. If the link expired or was already used, request a new one.`
          : "The password could not be reset. Request a new link and try again.");
      }
    } finally {
      setBusy(false);
    }
  }

  const inputClass = "w-full border border-gray-400 rounded-lg p-3";
  return (
    <div className="min-h-screen bg-gray-100">
      <AuthNavBar />
      <main
        className="min-h-[85vh] flex items-center justify-center p-6 bg-cover bg-center"
        style={{ backgroundImage: `url(${studentsBg})` }}
      >
        <section className="bg-white rounded-xl shadow-lg p-8 w-full max-w-lg">
          <h1 className="text-3xl font-bold text-vsu-orange mb-6">Reset Password</h1>
          {done ? (
            <div role="status" className="flex flex-col gap-4">
              <p>Your password was reset. You have been signed out everywhere; log in with the new password.</p>
              <Link className="text-blue-700 underline" to="/StudentLogin">Student Login</Link>
              <Link className="text-blue-700 underline" to="/FacultyLogin">Faculty Login</Link>
            </div>
          ) : !token ? (
            <div role="alert" className="flex flex-col gap-4">
              <p>This reset link is missing its token. Open the link from your email again, or request a new one.</p>
              <Link className="text-blue-700 underline" to="/forgot-password">Request a new link</Link>
            </div>
          ) : (
            <form onSubmit={submit} className="flex flex-col gap-4">
              {error && <p role="alert" className="text-red-700">{error}</p>}
              <p className="text-sm text-gray-600">
                Use 8 to 72 characters with an uppercase letter, a lowercase letter, a number, and a symbol or space.
              </p>
              <label className="flex flex-col gap-1">
                New password
                <input
                  required
                  type="password"
                  autoComplete="new-password"
                  value={password}
                  disabled={busy}
                  onChange={(event) => setPassword(event.target.value)}
                  className={inputClass}
                />
              </label>
              <label className="flex flex-col gap-1">
                Confirm new password
                <input
                  required
                  type="password"
                  autoComplete="new-password"
                  value={confirm}
                  disabled={busy}
                  onChange={(event) => setConfirm(event.target.value)}
                  className={inputClass}
                />
              </label>
              <button
                type="submit"
                disabled={busy}
                className="bg-blue-700 text-white font-semibold rounded-lg p-3 cursor-pointer disabled:opacity-50"
              >
                {busy ? "Please wait..." : "Reset Password"}
              </button>
            </form>
          )}
        </section>
      </main>
    </div>
  );
}
