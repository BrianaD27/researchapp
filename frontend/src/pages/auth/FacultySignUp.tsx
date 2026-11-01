import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import AuthNavBar from "../../components/common/AuthNavBar";
import apiClient from "../../api/client";
import { authErrorMessage } from "../../api/authErrors";
import studentsBg from "../../assets/studentsBg.png";

// Faculty sign-up: VSU faculty email, then email activation, then an admin approves.
export default function FacultySignUp() {
  const [name, setName] = useState("");
  const [department, setDepartment] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy || done) return;
    setError("");
    if (password !== confirm) {
      setError("The passwords do not match.");
      return;
    }
    setBusy(true);
    try {
      await apiClient.post("/v1/auth/register-faculty", {
        name: name.trim(), department: department.trim(), username: username.trim(), email: email.trim(), password,
      });
      setPassword("");
      setConfirm("");
      setDone(true);
    } catch (caught) {
      setError(authErrorMessage(caught, "register"));
    } finally {
      setBusy(false);
    }
  }

  const input = "w-full border border-gray-400 rounded-lg p-3";
  return (
    <div className="min-h-screen bg-gray-100">
      <AuthNavBar />
      <main className="min-h-[85vh] flex items-center justify-center p-6 bg-cover bg-center"
        style={{ backgroundImage: `url(${studentsBg})` }}>
        <section className="bg-white rounded-xl shadow-lg p-8 w-full max-w-lg">
          <h1 className="text-3xl font-bold text-vsu-orange mb-2">Faculty Sign Up</h1>
          {done ? (
            <div role="status" className="flex flex-col gap-4">
              <p>Almost done. We sent an activation link to {email}. Open it to confirm your VSU email.</p>
              <p>After that, an app admin will approve your faculty account. You will get an email when it is ready.</p>
              <Link className="bg-blue-700 text-white rounded-lg p-3 text-center" to="/FacultyLogin">Go to Faculty Login</Link>
            </div>
          ) : (
            <form onSubmit={submit} className="flex flex-col gap-4">
              <p className="text-sm text-gray-600">
                For VSU faculty and staff. Use your @vsu.edu email. An admin approves each faculty account.
              </p>
              {error && <p role="alert" className="text-red-700">{error}</p>}
              <label className="flex flex-col gap-1">
                Full name
                <input required maxLength={150} value={name} disabled={busy}
                  onChange={(event) => setName(event.target.value)} placeholder="Dr. Jane Smith" className={input} />
              </label>
              <label className="flex flex-col gap-1">
                Department
                <input maxLength={150} value={department} disabled={busy}
                  onChange={(event) => setDepartment(event.target.value)} className={input} />
              </label>
              <label className="flex flex-col gap-1">
                Username
                <input required autoComplete="username" value={username} disabled={busy}
                  onChange={(event) => setUsername(event.target.value)} className={input} />
              </label>
              <label className="flex flex-col gap-1">
                VSU faculty email
                <input required type="email" autoComplete="email" value={email} disabled={busy}
                  pattern="[A-Za-z0-9._%+-]+@[vV][sS][uU]\.[eE][dD][uU]"
                  title="Use your @vsu.edu faculty email."
                  onChange={(event) => setEmail(event.target.value)} className={input} />
              </label>
              <label className="flex flex-col gap-1">
                Password
                <input required type="password" autoComplete="new-password" value={password} disabled={busy}
                  onChange={(event) => setPassword(event.target.value)} className={input} />
              </label>
              <p className="text-sm text-gray-600">
                Use 8 to 72 characters with an uppercase letter, a lowercase letter, a number, and a symbol or space.
              </p>
              <label className="flex flex-col gap-1">
                Confirm password
                <input required type="password" autoComplete="new-password" value={confirm} disabled={busy}
                  onChange={(event) => setConfirm(event.target.value)} className={input} />
              </label>
              <button type="submit" disabled={busy}
                className="bg-blue-700 text-white font-semibold rounded-lg p-3 cursor-pointer disabled:opacity-50">
                {busy ? "Please wait..." : "Request Faculty Account"}
              </button>
              <Link className="text-blue-700 underline" to="/FacultyLogin">Already have an account? Log in</Link>
            </form>
          )}
        </section>
      </main>
    </div>
  );
}
