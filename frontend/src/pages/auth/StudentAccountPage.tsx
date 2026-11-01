import { useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import { Link, useNavigate } from "react-router-dom";
import AuthNavBar from "../../components/common/AuthNavBar";
import IdleSignOutNotice from "../../components/common/IdleSignOutNotice";
import apiClient from "../../api/client";
import { authErrorMessage } from "../../api/authErrors";
import studentsBg from "../../assets/studentsBg.png";

export default function StudentAccountPage({ register = false }: { register?: boolean }) {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [code, setCode] = useState("");
  const [needsCode, setNeedsCode] = useState(false);
  const [busy, setBusy] = useState(false);
  const [created, setCreated] = useState(false);
  const [error, setError] = useState("");

  function clearTokens() {
    sessionStorage.removeItem("researchapp.accessToken");
    sessionStorage.removeItem("researchapp.refreshToken");
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy || created) return;
    setError("");

    if (register && password !== confirm) {
      setError("The passwords do not match.");
      return;
    }

    setBusy(true);
    try {
      if (register) {
        await apiClient.post("/v1/auth/register", {
          username: username.trim(),
          email: email.trim(),
          password,
          role: "STUDENT",
        });
        setPassword("");
        setConfirm("");
        setCreated(true);
        return;
      }

      clearTokens();
      const { data } = needsCode
        ? await apiClient.post("/v1/auth/verify-2fa", {
            username: username.trim(), code,
          })
        : await apiClient.post("/v1/auth/login", {
            username: username.trim(), password,
          });

      if (data.status === "2FA_REQUIRED") {
        setPassword("");
        setNeedsCode(true);
        return;
      }

      if (typeof data.token !== "string" || data.token.split(".").length !== 3) {
        setError("Login did not return a valid session. Please try again.");
        return;
      }

      sessionStorage.setItem("researchapp.accessToken", data.token);
      if (typeof data.refreshToken === "string") {
        sessionStorage.setItem("researchapp.refreshToken", data.refreshToken);
      }
      setPassword("");
      setCode("");
      navigate("/my-profile", { replace: true });
    } catch (caught) {
      if (isAxiosError(caught)) {
        setError(authErrorMessage(caught, register ? "register" : needsCode ? "verify" : "login"));
      } else {
        setError("The request failed. Please try again.");
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
          <h1 className="text-3xl font-bold text-vsu-orange mb-6">
            {register ? "Student Sign Up" : "Student Login"}
          </h1>

          {created ? (
            <div role="status" className="flex flex-col gap-4">
              <p>Account created for {username}. Log in with your username and password.</p>
              <Link className="bg-blue-700 text-white rounded-lg p-3 text-center" to="/StudentLogin">
                Go to Student Login
              </Link>
            </div>
          ) : (
            <form onSubmit={submit} className="flex flex-col gap-4">
              <IdleSignOutNotice />
              {error && <p role="alert" className="text-red-700">{error}</p>}

              <label className="flex flex-col gap-1">
                Username
                <input
                  required
                  autoComplete="username"
                  value={username}
                  disabled={busy || needsCode}
                  onChange={(event) => setUsername(event.target.value)}
                  className={inputClass}
                />
              </label>

              {register && (
                <label className="flex flex-col gap-1">
                  VSU email
                  <input
                    required
                    type="email"
                    autoComplete="email"
                    pattern="[A-Za-z0-9._%+-]+@([sS][tT][uU][dD][eE][nN][tT][sS]\.)?[vV][sS][uU]\.[eE][dD][uU]"
                    title="Use an @vsu.edu or @students.vsu.edu email address."
                    value={email}
                    disabled={busy}
                    onChange={(event) => setEmail(event.target.value)}
                    className={inputClass}
                  />
                </label>
              )}

              {needsCode ? (
                <label className="flex flex-col gap-1">
                  Six-digit code sent to your email
                  <input
                    required
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    pattern="[0-9]{6}"
                    maxLength={6}
                    value={code}
                    disabled={busy}
                    onChange={(event) => setCode(event.target.value.replace(/\D/g, ""))}
                    className={inputClass}
                  />
                </label>
              ) : (
                <label className="flex flex-col gap-1">
                  Password
                  <input
                    required
                    type="password"
                    autoComplete={register ? "new-password" : "current-password"}
                    value={password}
                    disabled={busy}
                    onChange={(event) => setPassword(event.target.value)}
                    className={inputClass}
                  />
                </label>
              )}

              {register && (
                <>
                  <p className="text-sm text-gray-600">
                    Use 8 to 72 characters with an uppercase letter, a lowercase letter, a number, and a symbol or space.
                  </p>
                  <label className="flex flex-col gap-1">
                    Confirm password
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
                </>
              )}

              <button
                type="submit"
                disabled={busy || !username.trim()}
                className="bg-blue-700 text-white font-semibold rounded-lg p-3 cursor-pointer disabled:opacity-50"
              >
                {busy ? "Please wait..." : register ? "Create Account" : needsCode ? "Verify Code" : "Log In"}
              </button>

              {needsCode && (
                <button
                  type="button"
                  disabled={busy}
                  onClick={() => {
                    setNeedsCode(false);
                    setCode("");
                    setError("");
                  }}
                  className="underline cursor-pointer"
                >
                  Back to login
                </button>
              )}

              {!register && !needsCode && (
                <Link className="text-blue-700 underline" to="/forgot-password">
                  Forgot password?
                </Link>
              )}

              <Link
                className="text-blue-700 underline"
                to={register ? "/StudentLogin" : "/StudentSignUp"}
              >
                {register ? "Already have an account? Log in" : "Need an account? Sign up"}
              </Link>
            </form>
          )}
        </section>
      </main>
    </div>
  );
}