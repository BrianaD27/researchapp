// ---------------------------------------------------------------------------
// ProtectedRoute.tsx  —  a gate in front of pages that require a login
// ---------------------------------------------------------------------------
//
// HOW IT'S USED (see main.tsx): we wrap a group of routes with this component.
// react-router renders <Outlet /> in place of "whatever child route matched".
// So if the visitor is allowed, they see the page; otherwise they get redirected.
//
//   <Route element={<ProtectedRoute allow={["STUDENT", "ADMIN"]} />}>
//     <Route path="/discover-opportunities" element={<StudentDiscoverPage />} />
//   </Route>

import { Navigate, Outlet } from "react-router-dom";

import { useAuth } from "./AuthContext";

interface ProtectedRouteProps {
  // If given, only these roles may enter. If omitted, any logged-in user may.
  allow?: string[];
}

const ProtectedRoute = ({ allow }: ProtectedRouteProps) => {
  const { status, user } = useAuth();

  // On a fresh page load we haven't finished checking localStorage yet.
  // Render nothing for that split second so we don't flash the login page.
  if (status === "loading") {
    return null;
  }

  // Not logged in -> go to login. `replace` means this redirect doesn't add a
  // history entry, so the back button still works sensibly.
  if (status !== "authed" || !user) {
    return <Navigate to="/StudentLogin" replace />;
  }

  // Logged in but wrong role (e.g. a student trying to open a professor page).
  if (allow && !allow.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  // All good - show the matched child route.
  return <Outlet />;
};

export default ProtectedRoute;
