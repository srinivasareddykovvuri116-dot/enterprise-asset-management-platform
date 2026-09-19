import { Navigate } from "react-router-dom";

import { useSelector } from "react-redux";

function RoleGuard({
  allowedRoles,
  children,
}) {

  const role = useSelector(
    (state) => state.auth.user?.role
  );

  if (!role) {
    return (
      <Navigate
        to="/login"
        replace
      />
    );
  }

  if (!allowedRoles.includes(role)) {
    return (
      <Navigate
        to="/dashboard"
        replace
      />
    );
  }

  return children;
}

export default RoleGuard;