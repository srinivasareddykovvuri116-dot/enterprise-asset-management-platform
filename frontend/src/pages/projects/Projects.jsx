import { useCallback, useEffect, useState } from "react";
import { useSelector } from "react-redux";
import {
  FolderKanban,
  Plus,
  Pencil,
  Archive,
  RotateCcw,
  X,
  CheckCircle2,
  AlertCircle,
  Loader2,
  UserRound,
  CalendarDays,
} from "lucide-react";

import {
  createProject,
  getProjects,
  updateProject,
  archiveProject,
  restoreProject,
} from "../../api/projectApi";

import { getUsers } from "../../api/userApi";

function Projects() {
  const user = useSelector((state) => state.auth.user);

  const isAdmin = user?.role === "ORGANIZATION_ADMIN";
  const isProjectManager = user?.role === "PROJECT_MANAGER";
  const canManageProjects = isAdmin || isProjectManager;

  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [showCreateForm, setShowCreateForm] = useState(false);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [managerId, setManagerId] = useState("");

  const [editingProjectId, setEditingProjectId] = useState(null);

  const [editName, setEditName] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [editManagerId, setEditManagerId] = useState("");
  const [editStatus, setEditStatus] = useState("ACTIVE");

  // --------------------------------------------------
  // Load Projects
  // --------------------------------------------------

  const loadProjects = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const projectsData = await getProjects();

      setProjects(
        Array.isArray(projectsData)
          ? projectsData
          : []
      );
    } catch (err) {
      console.error("Failed to load projects:", err);

      setError(
        err.response?.data?.message ||
          "Failed to load projects."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  // --------------------------------------------------
  // Load Users
  // Only Organization Admin needs user list.
  // Project Manager uses their own userId.
  // Team Member does not need user list.
  // --------------------------------------------------

  const loadUsers = useCallback(async () => {
    if (!isAdmin) {
      return;
    }

    try {
      const usersData = await getUsers();

      setUsers(
        Array.isArray(usersData)
          ? usersData
          : []
      );
    } catch (err) {
      console.error("Failed to load users:", err);

      setError(
        err.response?.data?.message ||
          "Failed to load users."
      );
    }
  }, [isAdmin]);

  useEffect(() => {
    const timer = setTimeout(() => {
      void Promise.all([
        loadProjects(),
        loadUsers(),
      ]);
    }, 0);

    return () => {
      clearTimeout(timer);
    };
  }, [loadProjects, loadUsers]);

  // --------------------------------------------------
  // Create Project
  // --------------------------------------------------

  const handleCreateProject = async (event) => {
    event.preventDefault();

    if (!canManageProjects) {
      return;
    }

    setError("");
    setSuccess("");

    if (!name.trim()) {
      setError("Project name is required.");
      return;
    }

    // Admin selects manager.
    // PM automatically becomes manager.
    const selectedManagerId = isProjectManager
      ? user?.userId
      : managerId;

    if (!selectedManagerId) {
      setError("Please select a project manager.");
      return;
    }

    try {
      setCreating(true);

      await createProject({
        name: name.trim(),
        description: description.trim() || null,
        managerId: Number(selectedManagerId),
      });

      setName("");
      setDescription("");
      setManagerId("");

      setSuccess("Project created successfully.");
      setShowCreateForm(false);

      await loadProjects();
    } catch (err) {
      console.error(
        "Failed to create project:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to create project."
      );
    } finally {
      setCreating(false);
    }
  };

  // --------------------------------------------------
  // Start Editing
  // --------------------------------------------------

  const startEditing = (project) => {
    if (!canManageProjects) {
      return;
    }

    setError("");
    setSuccess("");

    setEditingProjectId(project.id);

    setEditName(project.name || "");
    setEditDescription(project.description || "");

    setEditManagerId(
      project.managerId
        ? String(project.managerId)
        : ""
    );

    setEditStatus(project.status || "ACTIVE");
  };

  // --------------------------------------------------
  // Cancel Editing
  // --------------------------------------------------

  const cancelEditing = () => {
    setEditingProjectId(null);
    setEditName("");
    setEditDescription("");
    setEditManagerId("");
    setEditStatus("ACTIVE");
  };

  // --------------------------------------------------
  // Update Project
  // --------------------------------------------------

  const handleUpdateProject = async (
    event,
    projectId
  ) => {
    event.preventDefault();

    if (!canManageProjects) {
      return;
    }

    setError("");
    setSuccess("");

    if (!editName.trim()) {
      setError("Project name is required.");
      return;
    }

    const selectedManagerId = isProjectManager
      ? user?.userId
      : editManagerId;

    if (!selectedManagerId) {
      setError("Please select a project manager.");
      return;
    }

    try {
      setSaving(true);

      await updateProject(projectId, {
        name: editName.trim(),
        description:
          editDescription.trim() || null,
        status: editStatus,
        managerId: Number(selectedManagerId),
      });

      setSuccess(
        "Project updated successfully."
      );

      cancelEditing();

      await loadProjects();
    } catch (err) {
      console.error(
        "Failed to update project:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to update project."
      );
    } finally {
      setSaving(false);
    }
  };

  // --------------------------------------------------
  // Archive
  // --------------------------------------------------

  const handleArchive = async (projectId) => {
    if (!canManageProjects) {
      return;
    }

    const confirmed = window.confirm(
      "Are you sure you want to archive this project?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setError("");
      setSuccess("");

      await archiveProject(projectId);

      setSuccess(
        "Project archived successfully."
      );

      await loadProjects();
    } catch (err) {
      console.error(
        "Failed to archive project:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to archive project."
      );
    }
  };

  // --------------------------------------------------
  // Restore
  // --------------------------------------------------

  const handleRestore = async (projectId) => {
    if (!canManageProjects) {
      return;
    }

    const confirmed = window.confirm(
      "Are you sure you want to restore this project?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setError("");
      setSuccess("");

      await restoreProject(projectId);

      setSuccess(
        "Project restored successfully."
      );

      await loadProjects();
    } catch (err) {
      console.error(
        "Failed to restore project:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to restore project."
      );
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
          <span>Loading projects...</span>
        </div>
      </div>
    );
  }

  // --------------------------------------------------
  // Manager List
  // --------------------------------------------------

  const managerUsers = users.filter(
    (user) =>
      user.role === "PROJECT_MANAGER" ||
      user.role === "ORGANIZATION_ADMIN"
  );

  // --------------------------------------------------
  // UI
  // --------------------------------------------------

  return (
    <div className="space-y-6">

      {/* Page Header */}

      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

        <div>
          <div className="flex items-center gap-3">

            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-slate-900 text-white">
              <FolderKanban size={21} />
            </div>

            <div>
              <h1 className="text-2xl font-bold text-slate-900">
                Projects
              </h1>

              <p className="mt-1 text-sm text-slate-500">
                {canManageProjects
                  ? "Create and manage organization projects."
                  : "View organization projects."}
              </p>
            </div>

          </div>
        </div>

        {/* Create Project Button
            Admin + Project Manager only */}

        {canManageProjects && (
          <button
            type="button"
            onClick={() => {
              setShowCreateForm((value) => !value);
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
                Create Project
              </>
            )}
          </button>
        )}

      </div>

      {/* Alerts */}

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

      {/* Create Project
          Admin + Project Manager only */}

      {canManageProjects && showCreateForm && (
        <section className="rounded-2xl border border-slate-200 bg-white shadow-sm">

          <div className="border-b border-slate-200 px-6 py-5">
            <h2 className="text-lg font-semibold text-slate-900">
              Create Project
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Add a new project to your organization.
            </p>
          </div>

          <form
            onSubmit={handleCreateProject}
            className="space-y-5 p-6"
          >

            {/* Project Name */}

            <div>
              <label
                htmlFor="project-name"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Project Name
              </label>

              <input
                id="project-name"
                type="text"
                value={name}
                onChange={(event) =>
                  setName(event.target.value)
                }
                placeholder="Enter project name"
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
              />
            </div>

            {/* Description */}

            <div>
              <label
                htmlFor="project-description"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Description
              </label>

              <textarea
                id="project-description"
                value={description}
                onChange={(event) =>
                  setDescription(
                    event.target.value
                  )
                }
                placeholder="Enter project description"
                rows={4}
                className="w-full resize-none rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
              />
            </div>

            {/* Project Manager */}

            <div>
              <label
                htmlFor="project-manager"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Project Manager
              </label>

              {isProjectManager ? (
                <div className="flex items-center gap-3 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5">

                  <UserRound
                    size={17}
                    className="text-slate-500"
                  />

                  <div>
                    <p className="text-sm font-medium text-slate-800">
                      {user?.email ||
                        "Project Manager"}
                    </p>

                    <p className="text-xs text-slate-500">
                      You will be assigned as the
                      project manager
                    </p>
                  </div>

                </div>
              ) : (
                <select
                  id="project-manager"
                  value={managerId}
                  onChange={(event) =>
                    setManagerId(
                      event.target.value
                    )
                  }
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                >
                  <option value="">
                    Select a manager
                  </option>

                  {managerUsers.map((manager) => (
                    <option
                      key={manager.id}
                      value={manager.id}
                    >
                      {manager.fullName} (
                      {manager.role})
                    </option>
                  ))}
                </select>
              )}
            </div>

            {/* Actions */}

            <div className="flex justify-end gap-3 border-t border-slate-200 pt-5">

              <button
                type="button"
                onClick={() => {
                  setShowCreateForm(false);
                  setName("");
                  setDescription("");
                  setManagerId("");
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
                  : "Create Project"}
              </button>

            </div>

          </form>

        </section>
      )}

      {/* Project List Header */}

      <div className="flex items-center justify-between">

        <div>
          <h2 className="text-lg font-semibold text-slate-900">
            Project List
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            {projects.length}{" "}
            {projects.length === 1
              ? "project"
              : "projects"}{" "}
            in your organization
          </p>
        </div>

      </div>

      {/* Empty State */}

      {projects.length === 0 ? (

        <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">

          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-slate-100 text-slate-500">
            <FolderKanban size={25} />
          </div>

          <h3 className="mt-4 text-base font-semibold text-slate-900">
            No projects found
          </h3>

          <p className="mx-auto mt-2 max-w-md text-sm text-slate-500">
            {canManageProjects
              ? "Create your first project to start organizing work for your team."
              : "There are currently no projects in your organization."}
          </p>

          {canManageProjects && (
            <button
              type="button"
              onClick={() =>
                setShowCreateForm(true)
              }
              className="mt-5 inline-flex items-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800"
            >
              <Plus size={17} />
              Create Project
            </button>
          )}

        </div>

      ) : (

        /* Project Cards */

        <div className="grid gap-5 lg:grid-cols-2">

          {projects.map((project) => (

            <article
              key={project.id}
              className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md"
            >

              {editingProjectId === project.id &&
              canManageProjects ? (

                /* Edit Form */

                <form
                  onSubmit={(event) =>
                    handleUpdateProject(
                      event,
                      project.id
                    )
                  }
                  className="p-6"
                >

                  <div className="mb-6 flex items-center justify-between">

                    <div>
                      <h3 className="text-lg font-semibold text-slate-900">
                        Edit Project
                      </h3>

                      <p className="mt-1 text-sm text-slate-500">
                        Update project information.
                      </p>
                    </div>

                    <button
                      type="button"
                      onClick={cancelEditing}
                      className="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
                    >
                      <X size={18} />
                    </button>

                  </div>

                  <div className="space-y-5">

                    {/* Project Name */}

                    <div>
                      <label
                        htmlFor={`edit-name-${project.id}`}
                        className="mb-2 block text-sm font-medium text-slate-700"
                      >
                        Project Name
                      </label>

                      <input
                        id={`edit-name-${project.id}`}
                        type="text"
                        value={editName}
                        onChange={(event) =>
                          setEditName(
                            event.target.value
                          )
                        }
                        className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                      />
                    </div>

                    {/* Description */}

                    <div>
                      <label
                        htmlFor={`edit-description-${project.id}`}
                        className="mb-2 block text-sm font-medium text-slate-700"
                      >
                        Description
                      </label>

                      <textarea
                        id={`edit-description-${project.id}`}
                        value={editDescription}
                        onChange={(event) =>
                          setEditDescription(
                            event.target.value
                          )
                        }
                        rows={4}
                        className="w-full resize-none rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                      />
                    </div>

                    {/* Project Manager */}

                    <div>
                      <label
                        htmlFor={`edit-manager-${project.id}`}
                        className="mb-2 block text-sm font-medium text-slate-700"
                      >
                        Project Manager
                      </label>

                      {isProjectManager ? (
                        <div className="flex items-center gap-3 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5">

                          <UserRound
                            size={17}
                            className="text-slate-500"
                          />

                          <span className="text-sm text-slate-700">
                            {project.managerName ||
                              user?.email ||
                              "Project Manager"}
                          </span>

                        </div>
                      ) : (
                        <select
                          id={`edit-manager-${project.id}`}
                          value={editManagerId}
                          onChange={(event) =>
                            setEditManagerId(
                              event.target.value
                            )
                          }
                          className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm outline-none focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                        >
                          <option value="">
                            Select a manager
                          </option>

                          {managerUsers.map(
                            (manager) => (
                              <option
                                key={manager.id}
                                value={manager.id}
                              >
                                {manager.fullName} (
                                {manager.role})
                              </option>
                            )
                          )}
                        </select>
                      )}

                    </div>

                    {/* Status */}

                    <div>
                      <label
                        htmlFor={`edit-status-${project.id}`}
                        className="mb-2 block text-sm font-medium text-slate-700"
                      >
                        Status
                      </label>

                      <select
                        id={`edit-status-${project.id}`}
                        value={editStatus}
                        onChange={(event) =>
                          setEditStatus(
                            event.target.value
                          )
                        }
                        className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm outline-none focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                      >
                        <option value="ACTIVE">
                          ACTIVE
                        </option>

                        <option value="ARCHIVED">
                          ARCHIVED
                        </option>

                        <option value="COMPLETED">
                          COMPLETED
                        </option>
                      </select>
                    </div>

                  </div>

                  {/* Edit Actions */}

                  <div className="mt-6 flex justify-end gap-3 border-t border-slate-200 pt-5">

                    <button
                      type="button"
                      onClick={cancelEditing}
                      disabled={saving}
                      className="rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-60"
                    >
                      Cancel
                    </button>

                    <button
                      type="submit"
                      disabled={saving}
                      className="inline-flex items-center gap-2 rounded-lg bg-slate-900 px-5 py-2.5 text-sm font-semibold text-white hover:bg-slate-800 disabled:opacity-60"
                    >
                      {saving && (
                        <Loader2
                          size={16}
                          className="animate-spin"
                        />
                      )}

                      {saving
                        ? "Saving..."
                        : "Save Changes"}
                    </button>

                  </div>

                </form>

              ) : (

                /* Project Card */

                <>

                  <div className="flex items-start justify-between gap-4 border-b border-slate-200 p-6">

                    <div className="min-w-0">

                      <div className="flex items-center gap-3">

                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-slate-700">
                          <FolderKanban size={19} />
                        </div>

                        <div className="min-w-0">

                          <h3 className="truncate text-base font-semibold text-slate-900">
                            {project.name}
                          </h3>

                          <p className="mt-1 text-xs text-slate-500">
                            Project #{project.id}
                          </p>

                        </div>

                      </div>

                    </div>

                    <span
                      className={`shrink-0 rounded-full px-3 py-1 text-xs font-semibold ${
                        project.status === "ACTIVE"
                          ? "bg-emerald-100 text-emerald-700"
                          : project.status === "ARCHIVED"
                          ? "bg-slate-100 text-slate-600"
                          : "bg-blue-100 text-blue-700"
                      }`}
                    >
                      {project.status}
                    </span>

                  </div>

                  <div className="space-y-5 p-6">

                    {/* Description */}

                    <div>
                      <p className="text-sm leading-6 text-slate-600">
                        {project.description ||
                          "No description provided."}
                      </p>
                    </div>

                    {/* Project Information */}

                    <div className="grid gap-4 sm:grid-cols-2">

                      <div className="rounded-lg bg-slate-50 p-4">

                        <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                          <UserRound size={15} />
                          Manager
                        </div>

                        <p className="mt-2 text-sm font-semibold text-slate-800">
                          {project.managerName ||
                            "Not assigned"}
                        </p>

                      </div>

                      <div className="rounded-lg bg-slate-50 p-4">

                        <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                          <CalendarDays size={15} />
                          Project ID
                        </div>

                        <p className="mt-2 text-sm font-semibold text-slate-800">
                          #{project.id}
                        </p>

                      </div>

                    </div>

                    {/* Management Actions
                        Admin + Project Manager only */}

                    {canManageProjects && (
                      <div className="flex flex-wrap gap-2 border-t border-slate-200 pt-5">

                        <button
                          type="button"
                          onClick={() =>
                            startEditing(project)
                          }
                          className="inline-flex items-center gap-2 rounded-lg border border-slate-300 px-3.5 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50"
                        >
                          <Pencil size={15} />
                          Edit
                        </button>

                        {project.status ===
                        "ARCHIVED" ? (

                          <button
                            type="button"
                            onClick={() =>
                              handleRestore(
                                project.id
                              )
                            }
                            className="inline-flex items-center gap-2 rounded-lg border border-emerald-200 px-3.5 py-2 text-sm font-medium text-emerald-700 transition hover:bg-emerald-50"
                          >
                            <RotateCcw size={15} />
                            Restore
                          </button>

                        ) : (

                          <button
                            type="button"
                            onClick={() =>
                              handleArchive(
                                project.id
                              )
                            }
                            className="inline-flex items-center gap-2 rounded-lg border border-red-200 px-3.5 py-2 text-sm font-medium text-red-600 transition hover:bg-red-50"
                          >
                            <Archive size={15} />
                            Archive
                          </button>

                        )}

                      </div>
                    )}

                  </div>

                </>
              )}

            </article>

          ))}

        </div>
      )}

    </div>
  );
}

export default Projects;