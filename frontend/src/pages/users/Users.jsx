import { useEffect, useState } from "react";
import {
  Users as UsersIcon,
  Plus,
  X,
  UserPlus,
  Mail,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  Loader2,
  UserRound,
} from "lucide-react";

import {
  createUser,
  getUsers,
} from "../../api/userApi";

function Users() {
  const [users, setUsers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [showCreateForm, setShowCreateForm] =
    useState(false);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fullName, setFullName] = useState("");
  const [role, setRole] =
    useState("TEAM_MEMBER");

  // --------------------------------------------------
  // Load Users
  // --------------------------------------------------

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await getUsers();

      setUsers(
        Array.isArray(data)
          ? data
          : []
      );
    } catch (err) {
      console.error(
        "Failed to load users:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load users."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let active = true;

    const fetchUsers = async () => {
      try {
        setError("");

        const data = await getUsers();

        if (!active) {
          return;
        }

        setUsers(Array.isArray(data) ? data : []);
      } catch (err) {
        if (!active) {
          return;
        }

        console.error("Failed to load users:", err);

        setError(
          err.response?.data?.message ||
            "Failed to load users."
        );
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void fetchUsers();

    return () => {
      active = false;
    };
  }, []);

  // --------------------------------------------------
  // Create User
  // --------------------------------------------------

  const handleCreateUser = async (event) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (!fullName.trim()) {
      setError("Full name is required.");
      return;
    }

    if (!email.trim()) {
      setError("Email is required.");
      return;
    }

    if (!password) {
      setError("Password is required.");
      return;
    }

    if (password.length < 8) {
      setError(
        "Password must contain at least 8 characters."
      );
      return;
    }

    if (!role) {
      setError("Please select a role.");
      return;
    }

    try {
      setCreating(true);

      await createUser({
        email: email.trim(),
        password,
        fullName: fullName.trim(),
        role,
      });

      setEmail("");
      setPassword("");
      setFullName("");
      setRole("TEAM_MEMBER");

      setSuccess(
        "User created successfully."
      );

      setShowCreateForm(false);

      await loadUsers();
    } catch (err) {
      console.error(
        "Failed to create user:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to create user."
      );
    } finally {
      setCreating(false);
    }
  };

  // --------------------------------------------------
  // Helpers
  // --------------------------------------------------

  const getRoleClass = (role) => {
    switch (role) {
      case "ORGANIZATION_ADMIN":
        return "bg-violet-100 text-violet-700";

      case "PROJECT_MANAGER":
        return "bg-blue-100 text-blue-700";

      case "TEAM_MEMBER":
      default:
        return "bg-slate-100 text-slate-600";
    }
  };

  const getRoleLabel = (role) => {
    switch (role) {
      case "ORGANIZATION_ADMIN":
        return "Organization Admin";

      case "PROJECT_MANAGER":
        return "Project Manager";

      case "TEAM_MEMBER":
        return "Team Member";

      default:
        return role;
    }
  };

  // --------------------------------------------------
  // Loading
  // --------------------------------------------------

  if (loading) {
    return (
      <div className="flex min-h-[400px] items-center justify-center">
        <div className="flex items-center gap-3 text-slate-500">
          <Loader2
            size={20}
            className="animate-spin"
          />

          <span>Loading users...</span>
        </div>
      </div>
    );
  }

  // --------------------------------------------------
  // UI
  // --------------------------------------------------

  return (
    <div className="space-y-6">

      {/* ==========================================
          HEADER
      =========================================== */}

      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

        <div className="flex items-center gap-3">

          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-slate-900 text-white">
            <UsersIcon size={21} />
          </div>

          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Users
            </h1>

            <p className="mt-1 text-sm text-slate-500">
              Manage users and roles in your organization.
            </p>
          </div>

        </div>

        <button
          type="button"
          onClick={() => {
            setShowCreateForm(
              (value) => !value
            );

            setError("");
            setSuccess("");
          }}
          className="inline-flex items-center justify-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800"
        >
          {showCreateForm ? (
            <>
              <X size={17} />
              Close
            </>
          ) : (
            <>
              <Plus size={17} />
              Create User
            </>
          )}
        </button>

      </div>

      {/* ==========================================
          ALERTS
      =========================================== */}

      {error && (
        <div className="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          <AlertCircle
            size={18}
            className="mt-0.5 shrink-0"
          />

          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="flex items-start gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          <CheckCircle2
            size={18}
            className="mt-0.5 shrink-0"
          />

          <span>{success}</span>
        </div>
      )}

      {/* ==========================================
          CREATE USER
      =========================================== */}

      {showCreateForm && (
        <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

          <div className="border-b border-slate-200 px-6 py-5">

            <div className="flex items-center gap-3">

              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
                <UserPlus size={18} />
              </div>

              <div>
                <h2 className="text-lg font-semibold text-slate-900">
                  Create User
                </h2>

                <p className="mt-1 text-sm text-slate-500">
                  Add a new member to your organization.
                </p>
              </div>

            </div>

          </div>

          <form
            onSubmit={handleCreateUser}
            className="space-y-5 p-6"
          >

            {/* Full Name */}

            <div>
              <label
                htmlFor="user-full-name"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Full Name
              </label>

              <div className="relative">

                <UserRound
                  size={17}
                  className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />

                <input
                  id="user-full-name"
                  type="text"
                  value={fullName}
                  onChange={(event) =>
                    setFullName(
                      event.target.value
                    )
                  }
                  placeholder="Enter full name"
                  className="w-full rounded-lg border border-slate-300 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                />

              </div>
            </div>

            {/* Email */}

            <div>
              <label
                htmlFor="user-email"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Email
              </label>

              <div className="relative">

                <Mail
                  size={17}
                  className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />

                <input
                  id="user-email"
                  type="email"
                  value={email}
                  onChange={(event) =>
                    setEmail(
                      event.target.value
                    )
                  }
                  placeholder="Enter email address"
                  className="w-full rounded-lg border border-slate-300 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                />

              </div>
            </div>

            {/* Password */}

            <div>
              <label
                htmlFor="user-password"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Password
              </label>

              <input
                id="user-password"
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(
                    event.target.value
                  )
                }
                placeholder="Minimum 8 characters"
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
              />

              <p className="mt-1.5 text-xs text-slate-400">
                Password must contain at least 8 characters.
              </p>
            </div>

            {/* Role */}

            <div>
              <label
                htmlFor="user-role"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Role
              </label>

              <div className="relative">

                <ShieldCheck
                  size={17}
                  className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />

                <select
                  id="user-role"
                  value={role}
                  onChange={(event) =>
                    setRole(
                      event.target.value
                    )
                  }
                  className="w-full appearance-none rounded-lg border border-slate-300 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                >
                  <option value="TEAM_MEMBER">
                    Team Member
                  </option>

                  <option value="PROJECT_MANAGER">
                    Project Manager
                  </option>

                  <option value="ORGANIZATION_ADMIN">
                    Organization Admin
                  </option>
                </select>

              </div>
            </div>

            {/* Actions */}

            <div className="flex justify-end gap-3 border-t border-slate-200 pt-5">

              <button
                type="button"
                onClick={() => {
                  setShowCreateForm(false);
                  setEmail("");
                  setPassword("");
                  setFullName("");
                  setRole("TEAM_MEMBER");
                  setError("");
                }}
                className="rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50"
              >
                Cancel
              </button>

              <button
                type="submit"
                disabled={creating}
                className="inline-flex items-center gap-2 rounded-lg bg-slate-900 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {creating && (
                  <Loader2
                    size={16}
                    className="animate-spin"
                  />
                )}

                {creating
                  ? "Creating..."
                  : "Create User"}
              </button>

            </div>

          </form>
        </section>
      )}

      {/* ==========================================
          USER LIST HEADER
      =========================================== */}

      <div className="flex items-end justify-between">

        <div>
          <h2 className="text-lg font-semibold text-slate-900">
            Organization Users
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            {users.length}{" "}
            {users.length === 1
              ? "user"
              : "users"}{" "}
            in your organization
          </p>
        </div>

      </div>

      {/* ==========================================
          EMPTY STATE
      =========================================== */}

      {users.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">

          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-slate-100 text-slate-500">
            <UsersIcon size={25} />
          </div>

          <h3 className="mt-4 text-base font-semibold text-slate-900">
            No users found
          </h3>

          <p className="mx-auto mt-2 max-w-md text-sm text-slate-500">
            Add users to start building your organization team.
          </p>

          <button
            type="button"
            onClick={() =>
              setShowCreateForm(true)
            }
            className="mt-5 inline-flex items-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800"
          >
            <Plus size={17} />
            Create User
          </button>

        </div>
      ) : (

        /* ========================================
           USER CARDS
        ========================================= */

        <div className="grid gap-5 lg:grid-cols-2">

          {users.map((user) => (

            <article
              key={user.id}
              className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md"
            >

              {/* User Header */}

              <div className="flex items-start justify-between gap-4 border-b border-slate-200 p-6">

                <div className="flex min-w-0 items-center gap-4">

                  <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-slate-900 text-sm font-semibold text-white">
                    {user.fullName
                      ?.charAt(0)
                      .toUpperCase() ||
                      "U"}
                  </div>

                  <div className="min-w-0">

                    <h3 className="truncate text-base font-semibold text-slate-900">
                      {user.fullName}
                    </h3>

                    <p className="mt-1 truncate text-sm text-slate-500">
                      {user.email}
                    </p>

                  </div>

                </div>

                <span
                  className={`shrink-0 rounded-full px-3 py-1 text-xs font-semibold ${
                    user.active
                      ? "bg-emerald-100 text-emerald-700"
                      : "bg-red-100 text-red-700"
                  }`}
                >
                  {user.active
                    ? "ACTIVE"
                    : "INACTIVE"}
                </span>

              </div>

              {/* User Details */}

              <div className="space-y-4 p-6">

                <div className="rounded-lg bg-slate-50 p-4">

                  <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                    <ShieldCheck
                      size={15}
                    />
                    Role
                  </div>

                  <div className="mt-2">
                    <span
                      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${getRoleClass(
                        user.role
                      )}`}
                    >
                      {getRoleLabel(
                        user.role
                      )}
                    </span>
                  </div>

                </div>

                <div className="grid gap-4 sm:grid-cols-2">

                  <div className="rounded-lg bg-slate-50 p-4">

                    <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                      <Mail size={14} />
                      Email
                    </div>

                    <p className="mt-2 truncate text-sm font-medium text-slate-800">
                      {user.email}
                    </p>

                  </div>

                  <div className="rounded-lg bg-slate-50 p-4">

                    <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                      <UserRound
                        size={14}
                      />
                      User ID
                    </div>

                    <p className="mt-2 text-sm font-semibold text-slate-800">
                      #{user.id}
                    </p>

                  </div>

                </div>

              </div>

            </article>

          ))}

        </div>
      )}

    </div>
  );
}

export default Users;