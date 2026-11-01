import { useEffect, useState } from "react";

const FLAG = "researchapp.idleSignedOut";

// Shown once on the login page after an automatic inactivity sign-out.
export default function IdleSignOutNotice() {
  const [show] = useState(() => sessionStorage.getItem(FLAG) === "1");

  useEffect(() => {
    sessionStorage.removeItem(FLAG);
  }, []);

  if (!show) return null;
  return (
    <p role="status" className="bg-amber-100 border border-amber-300 rounded-lg p-3 text-gray-900">
      You were signed out after 30 minutes of inactivity. Please log in again.
    </p>
  );
}
