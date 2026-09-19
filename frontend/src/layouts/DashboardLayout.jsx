import {
  NavLink,
  Outlet,
  useNavigate,
} from "react-router-dom";

import {
  useDispatch,
  useSelector,
} from "react-redux";

import {
  LayoutDashboard,
  FolderKanban,
  ListTodo,
  Users,
  ScrollText,
  LogOut,
} from "lucide-react";

import { logout } from "../store/authSlice";

function DashboardLayout() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const user = useSelector(
    (state) => state.auth.user
  );

  const handleLogout = () => {
    dispatch(logout());

    navigate("/login", {
      replace: true,
    });
  };

  const navItemClass = ({ isActive }) =>
    `flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium transition ${
      isActive
        ? "bg-white/10 text-white"
        : "text-slate-400 hover:bg-white/5 hover:text-white"
    }`;

  const isAdmin =
    user?.role === "ORGANIZATION_ADMIN";

  return (
    <div className="min-h-screen bg-slate-100">

      <div className="flex min-h-screen">

        {/* ======================================================
            SIDEBAR
        ====================================================== */}

        <aside className="fixed inset-y-0 left-0 z-40 hidden w-64 flex-col bg-slate-950 text-white md:flex">

          {/* Logo */}

          <div className="flex h-16 items-center border-b border-white/10 px-6">

            <div>
              <h1 className="text-lg font-bold">
                Asset Management
              </h1>

              <p className="text-xs text-slate-400">
                Enterprise Platform
              </p>
            </div>

          </div>


          {/* Navigation */}

          <nav className="flex-1 space-y-2 px-4 py-6">

            {/* Dashboard */}

            <NavLink
              to="/dashboard"
              className={navItemClass}
            >
              <LayoutDashboard size={18} />

              Dashboard
            </NavLink>


            {/* Projects */}

            <NavLink
              to="/projects"
              className={navItemClass}
            >
              <FolderKanban size={18} />

              Projects
            </NavLink>


            {/* Tasks */}

            <NavLink
              to="/tasks"
              className={navItemClass}
            >
              <ListTodo size={18} />

              Tasks
            </NavLink>


            {/* ==================================================
                ORGANIZATION ADMIN ONLY
            ================================================== */}

            {isAdmin && (
              <>
                {/* Users */}

                <NavLink
                  to="/users"
                  className={navItemClass}
                >
                  <Users size={18} />

                  Users
                </NavLink>


                {/* Audit Logs */}

                <NavLink
                  to="/audit-logs"
                  className={navItemClass}
                >
                  <ScrollText size={18} />

                  Audit Logs
                </NavLink>
              </>
            )}

          </nav>


          {/* ======================================================
              SIDEBAR FOOTER
          ====================================================== */}

          <div className="border-t border-white/10 p-4">

            <div className="mb-3 rounded-lg bg-white/5 p-3">

              <p className="truncate text-sm font-medium text-white">
                {user?.email || "User"}
              </p>

              <p className="mt-1 text-xs text-slate-400">
                {user?.role || "USER"}
              </p>

            </div>


            {/* Logout */}

            <button
              type="button"
              onClick={handleLogout}
              className="flex w-full items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium text-slate-400 transition hover:bg-red-500/10 hover:text-red-400"
            >
              <LogOut size={18} />

              Logout
            </button>

          </div>

        </aside>


        {/* ======================================================
            MAIN AREA
        ====================================================== */}

        <div className="flex min-h-screen flex-1 flex-col md:ml-64">

          {/* ====================================================
              TOP HEADER
          ==================================================== */}

          <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6">

            <div>
              <p className="text-sm font-medium text-slate-500">
                Enterprise Workspace
              </p>
            </div>


            {/* Current User */}

            <div className="flex items-center gap-3">

              <div className="hidden text-right sm:block">

                <p className="text-sm font-semibold text-slate-800">
                  {user?.email || "User"}
                </p>

                <p className="text-xs text-slate-500">
                  {user?.role || "USER"}
                </p>

              </div>


              {/* Avatar */}

              <div className="flex h-9 w-9 items-center justify-center rounded-full bg-slate-900 text-sm font-semibold text-white">

                {user?.email
                  ?.charAt(0)
                  .toUpperCase() || "U"}

              </div>

            </div>

          </header>


          {/* ====================================================
              PAGE CONTENT
          ==================================================== */}

          <main className="flex-1 p-4 sm:p-6 lg:p-8">

            <Outlet />

          </main>

        </div>

      </div>

    </div>
  );
}

export default DashboardLayout;