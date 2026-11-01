// Reads the role from the signed-in token, only to choose which screens to show.
// The server still checks every request, so this is never a security decision.
export function currentRole(): string {
  try {
    const token = sessionStorage.getItem("researchapp.accessToken");
    const part = token?.split(".")[1];
    if (!part) return "";
    const base64 = part.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
    const bytes = Uint8Array.from(atob(padded), (character) => character.charCodeAt(0));
    const claims = JSON.parse(new TextDecoder().decode(bytes)) as { role?: unknown };
    return typeof claims.role === "string" ? claims.role.toUpperCase().replace(/^ROLE_/, "") : "";
  } catch {
    return "";
  }
}

// Where each kind of account lands after logging in.
export function homeFor(role: string): string {
  if (role === "PARTNER") return "/partner-research";
  if (role === "STUDENT") return "/my-profile";
  return "/faculty-profile";
}
