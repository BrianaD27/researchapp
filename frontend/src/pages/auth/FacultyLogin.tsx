import { useState, type FormEvent } from "react";
import { isAxiosError } from "axios";
import { Link, useNavigate } from "react-router-dom";
import AuthNavBar from "../../components/common/AuthNavBar";
import IdleSignOutNotice from "../../components/common/IdleSignOutNotice";
import apiClient from "../../api/client";
import { currentRole, homeFor } from "../../api/session";
import { authErrorMessage } from "../../api/authErrors";
import background from "../../assets/studentsBg.png";
import logo from "../../assets/vsuCircleLogo.png";

interface LoginResponse {
  status?: string;
  token?: string;
  refreshToken?: string;
}

const FacultyLogin = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [code, setCode] = useState("");
  const [needsCode, setNeedsCode] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setError("");
    setBusy(true);

    try {
      sessionStorage.removeItem("researchapp.accessToken");
      sessionStorage.removeItem("researchapp.refreshToken");

      const endpoint = needsCode ? "/v1/auth/verify-2fa" : "/v1/auth/login";
      const payload = needsCode
        ? { username: username.trim(), code: code.trim() }
        : { username: username.trim(), password };

      const response = await apiClient.post<LoginResponse>(endpoint, payload);
      const data = response.data;

      if (data.status === "2FA_REQUIRED") {
        setPassword("");
        setNeedsCode(true);
        return;
      }

      if (
        typeof data.token !== "string" ||
        data.token.split(".").length !== 3
      ) {
        throw new Error("The server did not return a valid login token.");
      }

      sessionStorage.setItem("researchapp.accessToken", data.token);
      if (typeof data.refreshToken === "string" && data.refreshToken) {
        sessionStorage.setItem("researchapp.refreshToken", data.refreshToken);
      }

      setPassword("");
      setCode("");
      navigate(homeFor(currentRole()), { replace: true, state: { fromLogin: true } });
    } catch (caught) {
      if (isAxiosError(caught)) {
        setError(authErrorMessage(caught, needsCode ? "verify" : "login"));
      } else {
        setError(
          caught instanceof Error ? caught.message : "Login failed. Please try again."
        );
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <div className="relative z-50"><AuthNavBar /></div>
      <img src={background} alt="" className="fixed inset-0 w-full h-full object-cover" />
      <main className="relative flex-1 flex items-center justify-center p-6">
        <section className="bg-white w-full max-w-md rounded-lg shadow-lg p-8">
          <img src={logo} alt="Virginia State University" className="h-20 mx-auto mb-4" />
          <h1 className="text-center text-4xl font-bold text-vsu-orange">Faculty Login</h1>
          <p className="text-center mt-2 mb-6">Bridging VSU talent with research opportunities</p>

          <form onSubmit={submit} className="flex flex-col gap-3">
            <IdleSignOutNotice />
            <label htmlFor="faculty-username">Username</label>
            <input
              id="faculty-username"
              autoComplete="username"
              required
              value={username}
              disabled={busy || needsCode}
              onChange={(event) => setUsername(event.target.value)}
              className="border-2 border-black bg-gray-100 p-2"
            />

            {needsCode ? (
              <>
                <p>Enter the verification code sent to your account email.</p>
                <label htmlFor="faculty-code">Verification code</label>
                <input
                  id="faculty-code"
                  autoComplete="one-time-code"
                  inputMode="numeric"
                  pattern="[0-9]{6}"
                  maxLength={6}
                  required
                  value={code}
                  disabled={busy}
                  onChange={(event) => setCode(event.target.value)}
                  className="border-2 border-black bg-gray-100 p-2"
                />
                <button
                  type="button"
                  disabled={busy}
                  onClick={() => { setNeedsCode(false); setCode(""); setError(""); }}
                  className="underline"
                >
                  Back to login
                </button>
              </>
            ) : (
              <>
                <label htmlFor="faculty-password">Password</label>
                <input
                  id="faculty-password"
                  type="password"
                  autoComplete="current-password"
                  required
                  value={password}
                  disabled={busy}
                  onChange={(event) => setPassword(event.target.value)}
                  className="border-2 border-black bg-gray-100 p-2"
                />
              </>
            )}

            {error && <p role="alert" className="text-red-700">{error}</p>}

            <button
              type="submit"
              disabled={busy}
              className="bg-vsu-orange text-white px-8 py-2 rounded hover:bg-orange-600 disabled:opacity-50"
            >
              {busy ? "Checking..." : needsCode ? "Verify code" : "Log In"}
            </button>
            {!needsCode && (
              <Link to="/forgot-password" className="text-blue-700 underline text-center">
                Forgot password?
              </Link>
            )}
            {error.includes("isn't verified") && (
              <Link to="/verify-email" className="text-blue-700 underline text-center">
                Send a new activation email
              </Link>
            )}
            {!needsCode && (
              <Link to="/FacultySignUp" className="text-blue-700 underline text-center">
                Create a new account
              </Link>
            )}
            {!needsCode && (
              <Link to="/PartnerLogin" className="text-sm text-gray-600 underline text-center">
                Partner school? Log in here
              </Link>
            )}
          </form>
        </section>
      </main>
    </div>
  );
};

export default FacultyLogin;
