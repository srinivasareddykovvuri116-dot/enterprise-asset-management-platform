import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";

import Dashboard from "./pages/dashboard/Dashboard";
import Projects from "./pages/projects/Projects";
import Tasks from "./pages/tasks/Tasks";

import Users from "./pages/users/Users";
import AuditLogs from "./pages/audit/AuditLogs";

import DashboardLayout from "./layouts/DashboardLayout";
import ProtectedRoute from "./routes/ProtectedRoute";
import RoleGuard from "./components/RoleGuard";

function App() {
  return (
    <BrowserRouter>

      <Routes>

        {/* ======================================================
            PUBLIC ROUTES
        ====================================================== */}

        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />


        {/* ======================================================
            PROTECTED ROUTES
        ====================================================== */}

        <Route element={<ProtectedRoute />}>

          <Route element={<DashboardLayout />}>

            {/* Dashboard */}

            <Route
              path="/dashboard"
              element={<Dashboard />}
            />


            {/* Projects
                All authenticated users */}

            <Route
              path="/projects"
              element={<Projects />}
            />


            {/* Tasks
                All authenticated users */}

            <Route
              path="/tasks"
              element={<Tasks />}
            />


            {/* ==================================================
                ORGANIZATION ADMIN ONLY
            ================================================== */}

            <Route
              path="/users"
              element={
                <RoleGuard
                  allowedRoles={[
                    "ORGANIZATION_ADMIN",
                  ]}
                >
                  <Users />
                </RoleGuard>
              }
            />


            <Route
              path="/audit-logs"
              element={
                <RoleGuard
                  allowedRoles={[
                    "ORGANIZATION_ADMIN",
                  ]}
                >
                  <AuditLogs />
                </RoleGuard>
              }
            />

          </Route>

        </Route>


        {/* ======================================================
            DEFAULT ROUTE
        ====================================================== */}

        <Route
          path="/"
          element={
            <Navigate
              to="/login"
              replace
            />
          }
        />


        {/* ======================================================
            UNKNOWN ROUTES
        ====================================================== */}

        <Route
          path="*"
          element={
            <Navigate
              to="/login"
              replace
            />
          }
        />

      </Routes>

    </BrowserRouter>
  );
}

export default App;