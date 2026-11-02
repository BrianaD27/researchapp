import { isAxiosError } from "axios";

// Plain-language messages for login, sign-up, and verification failures.
// Login never says whether the username or the password was wrong, so the
// page cannot be used to discover which usernames exist.
export function authErrorMessage(caught: unknown, action: "login" | "register" | "verify"): string {
  if (!isAxiosError(caught)) return "Something went wrong. Please try again.";
  if (!caught.response) return "Cannot reach the server. Check that the backend is running.";
  if (caught.response.status === 429) return "Too many attempts. Please wait a minute and try again.";

  const data = caught.response.data as { error?: unknown } | undefined;
  const reason = typeof data?.error === "string" ? data.error : "";

  if (action === "login") {
    if (/locked/i.test(reason)) {
      return "This account is temporarily locked after too many failed attempts. Try again in 15 minutes, or reset your password.";
    }
    if (/not verified/i.test(reason)) {
      return "Your email isn't verified yet. Open the activation link we emailed you.";
    }
    if (/inactive/i.test(reason)) {
      return "This account is disabled. Contact an administrator.";
    }
    return "Incorrect username or password.";
  }
  if (action === "verify") {
    return "That verification code is incorrect or expired. Go back to login to request a new one.";
  }
  return reason || "Sign-up failed. Check your VSU email and password, then try again.";
}
