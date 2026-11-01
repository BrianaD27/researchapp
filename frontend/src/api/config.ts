// Where the Java backend lives. Defaults to the local demo backend; the
// security-test copy sets VITE_API_ORIGIN=http://localhost:8081.
const configured = import.meta.env.VITE_API_ORIGIN;

export const API_ORIGIN: string =
  typeof configured === "string" && configured.trim() !== ""
    ? configured.trim().replace(/\/+$/, "")
    : "http://localhost:8080";
