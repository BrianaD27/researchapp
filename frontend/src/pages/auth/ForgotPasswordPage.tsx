import { useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import { Link } from "react-router-dom";
import AuthNavBar from "../../components/common/AuthNavBar";
import apiClient from "../../api/client";
import studentsBg from "../../assets/studentsBg.png";

export default function ForgotPasswordPage() {
  const [identifier, setIdentifier] = useState("");
  const [busy, setBusy] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy || sent) return;
    setError("");
    setBusy(true);
    try {
      await apiClient.post("/v1/auth/forgot-password", { username: identifier.trim() });
      setSent(true);
    } catch (caught) {
      if (isAxiosError(caught) && !caught.response) {
        setError("Cannot reach the backend. Check that Java is running.");
      } else if (isAxiosError(caught) && caught.response?.status === 429) {
        setError("Too many requests. Please wait a minute and try again.");
      } else {
        setError("The request could not be completed. Please try again.");
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
        <section className="bg-white rounded-xl shadow-lg p-8 w-full max-w-lg">
          <h1 className="text-3xl font-bold text-vsu-orange mb-6">Forgot Password</h1>
          {sent ? (
            <div role="status" className="flex flex-col gap-4">
              <p>
                If an account matches what you entered, a reset link has been sent to its email.
                The link expires in 15 minutes.
              </p>
              <Link className="text-blue-700 underline" to="/StudentLogin">Back to Student Login</Link>
              <Link className="text-blue-700 underline" to="/FacultyLogin">Back to Faculty Login</Link>
            </div>
          ) : (
            <form onSubmit={submit} className="flex flex-col gap-4">
              {error && <p role="alert" className="text-red-700">{error}</p>}
              <label className="flex flex-col gap-1">
                Username or VSU email
                <input
                  required
                  autoComplete="username"
                  value={identifier}
                  disabled={busy}
                  onChange={(event) => setIdentifier(event.target.value)}
                  className="w-full border border-gray-400 rounded-lg p-3"
                />
              </label>
              <button
                type="submit"
                disabled={busy || !identifier.trim()}
                className="bg-blue-700 text-white font-semibold rounded-lg p-3 cursor-pointer disabled:opacity-50"
              >
                {busy ? "Please wait..." : "Send Reset Link"}
              </button>
              <Link className="text-blue-700 underline" to="/StudentLogin">Back to login</Link>
            </form>
          )}
        </section>
      </main>
    </div>
  );
}
