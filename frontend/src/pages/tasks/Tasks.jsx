import { useCallback, useEffect, useState } from "react";
import { useSelector } from "react-redux";
import {
  ListTodo,
  Plus,
  Pencil,
  X,
  CheckCircle2,
  AlertCircle,
  Loader2,
  RotateCcw,
  CalendarDays,
  UserRound,
  FolderKanban,
  SlidersHorizontal,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";

import {
  createTask,
  getTasks,
  updateTask,
  updateTaskStatus,
  assignTask,
} from "../../api/taskApi";

import { getProjects } from "../../api/projectApi";
import { getAssignableUsers } from "../../api/userApi";

function Tasks() {
  const user = useSelector((state) => state.auth.user);

  const isAdmin =
    user?.role === "ORGANIZATION_ADMIN";

  const isProjectManager =
    user?.role === "PROJECT_MANAGER";

  const canManageAssignments =
    isAdmin || isProjectManager;

  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [showCreateForm, setShowCreateForm] =
    useState(false);

  // --------------------------------------------------
  // Create form
  // --------------------------------------------------

  const [title, setTitle] = useState("");
  const [description, setDescription] =
    useState("");
  const [priority, setPriority] =
    useState("MEDIUM");
  const [projectId, setProjectId] =
    useState("");
  const [assigneeId, setAssigneeId] =
    useState("");
  const [dueDate, setDueDate] =
    useState("");

  // --------------------------------------------------
  // Filters
  // --------------------------------------------------

  const [statusFilter, setStatusFilter] =
    useState("");

  const [priorityFilter, setPriorityFilter] =
    useState("");

  const [projectFilter, setProjectFilter] =
    useState("");

  const [assigneeFilter, setAssigneeFilter] =
    useState("");

  // --------------------------------------------------
  // Pagination
  // --------------------------------------------------

  const [page, setPage] = useState(0);
  const pageSize = 10;

  const [totalPages, setTotalPages] =
    useState(0);

  const [totalElements, setTotalElements] =
    useState(0);

  // --------------------------------------------------
  // Sorting
  // --------------------------------------------------

  const [sortField, setSortField] =
    useState("createdAt");

  const [sortDirection, setSortDirection] =
    useState("desc");

  // --------------------------------------------------
  // Edit
  // --------------------------------------------------

  const [editingTaskId, setEditingTaskId] =
    useState(null);

  const [editTitle, setEditTitle] =
    useState("");

  const [editDescription, setEditDescription] =
    useState("");

  const [editPriority, setEditPriority] =
    useState("MEDIUM");

  const [editAssigneeId, setEditAssigneeId] =
    useState("");

  const [editDueDate, setEditDueDate] =
    useState("");

  // --------------------------------------------------
  // Common styles
  // --------------------------------------------------

  const inputClass =
    "w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200";

  const selectClass =
    "w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-2 focus:ring-slate-200";

  const labelClass =
    "mb-2 block text-sm font-medium text-slate-700";

  // --------------------------------------------------
  // Load Tasks
  // --------------------------------------------------

  const loadTasks = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const params = {
        page,
        size: pageSize,
        sort: `${sortField},${sortDirection}`,
      };

      if (statusFilter) {
        params.status = statusFilter;
      }

      if (priorityFilter) {
        params.priority = priorityFilter;
      }

      if (projectFilter) {
        params.projectId =
          Number(projectFilter);
      }

      if (assigneeFilter) {
        params.assigneeId =
          Number(assigneeFilter);
      }

      const data = await getTasks(params);

      setTasks(
        Array.isArray(data?.content)
          ? data.content
          : []
      );

      setTotalPages(
        data?.totalPages || 0
      );

      setTotalElements(
        data?.totalElements || 0
      );
    } catch (err) {
      console.error(
        "Failed to load tasks:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load tasks."
      );
    } finally {
      setLoading(false);
    }
  }, [
    page,
    statusFilter,
    priorityFilter,
    projectFilter,
    assigneeFilter,
    sortField,
    sortDirection,
  ]);

  // --------------------------------------------------
  // Load Projects
  // --------------------------------------------------

  const loadProjects = useCallback(async () => {
    try {
      const data = await getProjects();

      setProjects(
        Array.isArray(data)
          ? data
          : []
      );
    } catch (err) {
      console.error(
        "Failed to load projects:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load projects."
      );
    }
  }, []);

  // --------------------------------------------------
  // Load Assignable Users
  //
  // Backend returns:
  // - same organization
  // - active users
  // - TEAM_MEMBER role only
  //
  // Admin + PM can load them.
  // --------------------------------------------------

  const loadUsers = useCallback(async () => {
    if (!canManageAssignments) {
      setUsers([]);
      return;
    }

    try {
      const data =
        await getAssignableUsers();

      setUsers(
        Array.isArray(data)
          ? data
          : []
      );
    } catch (err) {
      console.error(
        "Failed to load assignable users:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load assignable users."
      );
    }
  }, [canManageAssignments]);

  // --------------------------------------------------
  // Initial data
  // --------------------------------------------------

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
  // Task loading
  // --------------------------------------------------

  useEffect(() => {
    const timer = setTimeout(() => {
      void loadTasks();
    }, 0);

    return () => {
      clearTimeout(timer);
    };
  }, [loadTasks]);

  // --------------------------------------------------
  // Create Task
  // --------------------------------------------------

  const handleCreateTask = async (event) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (!title.trim()) {
      setError("Task title is required.");
      return;
    }

    if (!projectId) {
      setError("Please select a project.");
      return;
    }

    if (!priority) {
      setError("Please select a priority.");
      return;
    }

    try {
      setCreating(true);

      await createTask({
        title: title.trim(),
        description:
          description.trim() || null,
        priority,
        projectId: Number(projectId),
        assigneeId:
          canManageAssignments && assigneeId
            ? Number(assigneeId)
            : null,
        dueDate: dueDate || null,
      });

      setTitle("");
      setDescription("");
      setPriority("MEDIUM");
      setProjectId("");
      setAssigneeId("");
      setDueDate("");

      setSuccess(
        "Task created successfully."
      );

      setShowCreateForm(false);
      setPage(0);

      await loadTasks();
    } catch (err) {
      console.error(
        "Failed to create task:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to create task."
      );
    } finally {
      setCreating(false);
    }
  };

  // --------------------------------------------------
  // Edit
  // --------------------------------------------------

  const startEditing = (task) => {
    setError("");
    setSuccess("");

    setEditingTaskId(task.id);

    setEditTitle(task.title || "");

    setEditDescription(
      task.description || ""
    );

    setEditPriority(
      task.priority || "MEDIUM"
    );

    setEditAssigneeId(
      task.assigneeId
        ? String(task.assigneeId)
        : ""
    );

    setEditDueDate(
      task.dueDate || ""
    );
  };

  const cancelEditing = () => {
    setEditingTaskId(null);
    setEditTitle("");
    setEditDescription("");
    setEditPriority("MEDIUM");
    setEditAssigneeId("");
    setEditDueDate("");
  };

  // --------------------------------------------------
  // Update Task
  //
  // Project cannot be changed here.
  // Backend UpdateTaskRequest does not support
  // project reassignment.
  // --------------------------------------------------

  const handleUpdateTask = async (
    event,
    taskId
  ) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (!editTitle.trim()) {
      setError("Task title is required.");
      return;
    }

    try {
      setSaving(true);

      await updateTask(taskId, {
        title: editTitle.trim(),
        description:
          editDescription.trim() || null,
        priority: editPriority,
        assigneeId:
          canManageAssignments && editAssigneeId
            ? Number(editAssigneeId)
            : null,
        dueDate:
          editDueDate || null,
      });

      setSuccess(
        "Task updated successfully."
      );

      cancelEditing();

      await loadTasks();
    } catch (err) {
      console.error(
        "Failed to update task:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to update task."
      );
    } finally {
      setSaving(false);
    }
  };

  // --------------------------------------------------
  // Status
  // --------------------------------------------------

  const handleStatusChange = async (
    taskId,
    status
  ) => {
    try {
      setError("");
      setSuccess("");

      await updateTaskStatus(
        taskId,
        status
      );

      setSuccess(
        "Task status updated successfully."
      );

      await loadTasks();
    } catch (err) {
      console.error(
        "Failed to update task status:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to update task status."
      );
    }
  };

  // --------------------------------------------------
  // Assignee
  // --------------------------------------------------

  const handleAssigneeChange = async (
    taskId,
    value
  ) => {
    if (!canManageAssignments) {
      return;
    }

    try {
      setError("");
      setSuccess("");

      await assignTask(
        taskId,
        value
          ? Number(value)
          : null
      );

      setSuccess(
        "Task assignment updated successfully."
      );

      await loadTasks();
    } catch (err) {
      console.error(
        "Failed to update task assignment:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to update task assignment."
      );
    }
  };

  // --------------------------------------------------
  // Filters
  // --------------------------------------------------

  const handleResetFilters = () => {
    setStatusFilter("");
    setPriorityFilter("");
    setProjectFilter("");
    setAssigneeFilter("");
    setSortField("createdAt");
    setSortDirection("desc");
    setPage(0);
  };

  const handleSortChange = (event) => {
    const value = event.target.value;

    const [field, direction] =
      value.split(",");

    setSortField(field);
    setSortDirection(direction);
    setPage(0);
  };

  // --------------------------------------------------
  // Badge helpers
  // --------------------------------------------------

  const getStatusClass = (status) => {
    switch (status) {
      case "COMPLETED":
        return "bg-emerald-100 text-emerald-700";

      case "IN_PROGRESS":
        return "bg-blue-100 text-blue-700";

      case "IN_REVIEW":
        return "bg-violet-100 text-violet-700";

      case "BACKLOG":
      default:
        return "bg-slate-100 text-slate-600";
    }
  };

  const getPriorityClass = (priority) => {
    switch (priority) {
      case "CRITICAL":
        return "bg-red-100 text-red-700";

      case "HIGH":
        return "bg-orange-100 text-orange-700";

      case "MEDIUM":
        return "bg-amber-100 text-amber-700";

      case "LOW":
      default:
        return "bg-slate-100 text-slate-600";
    }
  };

  // --------------------------------------------------
  // Loading
  // --------------------------------------------------

  if (
    loading &&
    tasks.length === 0
  ) {
    return (
      <div className="flex min-h-[400px] items-center justify-center">
        <div className="flex items-center gap-3 text-slate-500">
          <Loader2
            size={20}
            className="animate-spin"
          />
          <span>
            Loading tasks...
          </span>
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
            <ListTodo size={21} />
          </div>

          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Tasks
            </h1>

            <p className="mt-1 text-sm text-slate-500">
              Manage and track work across your projects.
            </p>
          </div>

        </div>

        {canManageAssignments && (
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
                Create Task
              </>
            )}
          </button>
        )}

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
          CREATE TASK
      =========================================== */}

      {showCreateForm && canManageAssignments && (
        <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

          <div className="border-b border-slate-200 px-6 py-5">
            <h2 className="text-lg font-semibold text-slate-900">
              Create Task
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Add a new task to a project.
            </p>
          </div>

          <form
            onSubmit={handleCreateTask}
            className="space-y-5 p-6"
          >

            {/* Title */}

            <div>
              <label
                htmlFor="task-title"
                className={labelClass}
              >
                Task Title
              </label>

              <input
                id="task-title"
                type="text"
                value={title}
                onChange={(event) =>
                  setTitle(
                    event.target.value
                  )
                }
                placeholder="Enter task title"
                className={inputClass}
              />
            </div>

            {/* Description */}

            <div>
              <label
                htmlFor="task-description"
                className={labelClass}
              >
                Description
              </label>

              <textarea
                id="task-description"
                value={description}
                onChange={(event) =>
                  setDescription(
                    event.target.value
                  )
                }
                placeholder="Enter task description"
                rows={4}
                className={`${inputClass} resize-none`}
              />
            </div>

            {/* Priority + Project */}

            <div className="grid gap-5 md:grid-cols-2">

              <div>
                <label
                  htmlFor="task-priority"
                  className={labelClass}
                >
                  Priority
                </label>

                <select
                  id="task-priority"
                  value={priority}
                  onChange={(event) =>
                    setPriority(
                      event.target.value
                    )
                  }
                  className={selectClass}
                >
                  <option value="LOW">
                    Low
                  </option>

                  <option value="MEDIUM">
                    Medium
                  </option>

                  <option value="HIGH">
                    High
                  </option>

                  <option value="CRITICAL">
                    Critical
                  </option>
                </select>
              </div>

              <div>
                <label
                  htmlFor="task-project"
                  className={labelClass}
                >
                  Project
                </label>

                <select
                  id="task-project"
                  value={projectId}
                  onChange={(event) =>
                    setProjectId(
                      event.target.value
                    )
                  }
                  className={selectClass}
                >
                  <option value="">
                    Select a project
                  </option>

                  {projects
                    .filter(
                      (project) =>
                        project.status !==
                        "ARCHIVED"
                    )
                    .map((project) => (
                      <option
                        key={project.id}
                        value={project.id}
                      >
                        {project.name}
                      </option>
                    ))}
                </select>
              </div>

            </div>

            {/* Assignee + Due Date */}

            <div className="grid gap-5 md:grid-cols-2">

              <div>
                <label
                  htmlFor="task-assignee"
                  className={labelClass}
                >
                  Assignee
                </label>

                <select
                  id="task-assignee"
                  value={assigneeId}
                  onChange={(event) =>
                    setAssigneeId(
                      event.target.value
                    )
                  }
                  className={selectClass}
                  disabled={!canManageAssignments}
                >
                  <option value="">
                    Unassigned
                  </option>

                  {users.map((member) => (
                    <option
                      key={member.id}
                      value={member.id}
                    >
                      {member.fullName}
                    </option>
                  ))}
                </select>

                {isProjectManager && (
                  <p className="mt-1.5 text-xs text-slate-400">
                    You can assign this task to an active Team Member.
                  </p>
                )}


              </div>

              <div>
                <label
                  htmlFor="task-due-date"
                  className={labelClass}
                >
                  Due Date
                </label>

                <input
                  id="task-due-date"
                  type="date"
                  value={dueDate}
                  onChange={(event) =>
                    setDueDate(
                      event.target.value
                    )
                  }
                  className={inputClass}
                />
              </div>

            </div>

            {/* Actions */}

            <div className="flex justify-end gap-3 border-t border-slate-200 pt-5">

              <button
                type="button"
                onClick={() => {
                  setShowCreateForm(false);
                  setTitle("");
                  setDescription("");
                  setPriority("MEDIUM");
                  setProjectId("");
                  setAssigneeId("");
                  setDueDate("");
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
                  : "Create Task"}
              </button>

            </div>

          </form>

        </section>
      )}

      {/* ==========================================
          FILTERS
      =========================================== */}

      <section className="rounded-2xl border border-slate-200 bg-white shadow-sm">

        <div className="flex flex-col gap-2 border-b border-slate-200 px-6 py-5 sm:flex-row sm:items-center sm:justify-between">

          <div className="flex items-center gap-3">

            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
              <SlidersHorizontal
                size={18}
              />
            </div>

            <div>
              <h2 className="text-base font-semibold text-slate-900">
                Task Filters
              </h2>

              <p className="text-xs text-slate-500">
                Narrow down and sort your tasks.
              </p>
            </div>

          </div>

          <button
            type="button"
            onClick={handleResetFilters}
            className="inline-flex items-center justify-center gap-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
          >
            <RotateCcw size={15} />
            Reset Filters
          </button>

        </div>

        <div className="grid gap-4 p-6 sm:grid-cols-2 lg:grid-cols-5">

          {/* Status */}

          <div>
            <label
              htmlFor="status-filter"
              className={labelClass}
            >
              Status
            </label>

            <select
              id="status-filter"
              value={statusFilter}
              onChange={(event) => {
                setStatusFilter(
                  event.target.value
                );
                setPage(0);
              }}
              className={selectClass}
            >
              <option value="">
                All Statuses
              </option>

              <option value="BACKLOG">
                Backlog
              </option>

              <option value="IN_PROGRESS">
                In Progress
              </option>

              <option value="IN_REVIEW">
                In Review
              </option>

              <option value="COMPLETED">
                Completed
              </option>
            </select>
          </div>

          {/* Priority */}

          <div>
            <label
              htmlFor="priority-filter"
              className={labelClass}
            >
              Priority
            </label>

            <select
              id="priority-filter"
              value={priorityFilter}
              onChange={(event) => {
                setPriorityFilter(
                  event.target.value
                );
                setPage(0);
              }}
              className={selectClass}
            >
              <option value="">
                All Priorities
              </option>

              <option value="LOW">
                Low
              </option>

              <option value="MEDIUM">
                Medium
              </option>

              <option value="HIGH">
                High
              </option>

              <option value="CRITICAL">
                Critical
              </option>
            </select>
          </div>

          {/* Project */}

          <div>
            <label
              htmlFor="project-filter"
              className={labelClass}
            >
              Project
            </label>

            <select
              id="project-filter"
              value={projectFilter}
              onChange={(event) => {
                setProjectFilter(
                  event.target.value
                );
                setPage(0);
              }}
              className={selectClass}
            >
              <option value="">
                All Projects
              </option>

              {projects.map((project) => (
                <option
                  key={project.id}
                  value={project.id}
                >
                  {project.name}
                </option>
              ))}
            </select>
          </div>

          {/* Assignee */}

          {canManageAssignments && (
            <div>
              <label
                htmlFor="assignee-filter"
                className={labelClass}
              >
                Assignee
              </label>

              <select
                id="assignee-filter"
                value={assigneeFilter}
                onChange={(event) => {
                  setAssigneeFilter(event.target.value);
                  setPage(0);
                }}
                className={selectClass}
              >
                <option value="">
                  All Assignees
                </option>

                {users.map((member) => (
                  <option key={member.id} value={member.id}>
                    {member.fullName}
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Sort */}

          <div>
            <label
              htmlFor="sort-tasks"
              className={labelClass}
            >
              Sort
            </label>

            <select
              id="sort-tasks"
              value={`${sortField},${sortDirection}`}
              onChange={handleSortChange}
              className={selectClass}
            >
              <option value="createdAt,desc">
                Newest First
              </option>

              <option value="createdAt,asc">
                Oldest First
              </option>

              <option value="title,asc">
                Title A-Z
              </option>

              <option value="title,desc">
                Title Z-A
              </option>

              <option value="dueDate,asc">
                Due Date Earliest
              </option>

              <option value="dueDate,desc">
                Due Date Latest
              </option>
            </select>
          </div>

        </div>

      </section>

      {/* ==========================================
          TASK LIST HEADER
      =========================================== */}

      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">

        <div>
          <h2 className="text-lg font-semibold text-slate-900">
            Task List
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            {totalElements}{" "}
            {totalElements === 1
              ? "task"
              : "tasks"}{" "}
            found
          </p>
        </div>

        {loading && (
          <div className="flex items-center gap-2 text-xs text-slate-400">
            <Loader2
              size={14}
              className="animate-spin"
            />
            Updating...
          </div>
        )}

      </div>

      {/* ==========================================
          EMPTY STATE
      =========================================== */}

      {tasks.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">

          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-slate-100 text-slate-500">
            <ListTodo size={25} />
          </div>

          <h3 className="mt-4 text-base font-semibold text-slate-900">
            No tasks found
          </h3>

          <p className="mx-auto mt-2 max-w-md text-sm text-slate-500">
            There are no tasks matching your current filters.
          </p>

          <button
            type="button"
            onClick={handleResetFilters}
            className="mt-5 inline-flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50"
          >
            <RotateCcw size={16} />
            Reset Filters
          </button>

        </div>
      ) : (

        /* ========================================
           TASK CARDS
        ========================================= */

        <div className="space-y-4">

          {tasks.map((task) => (

            <article
              key={task.id}
              className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md"
            >

              {editingTaskId === task.id ? (

                /* ==================================
                   EDIT FORM
                ================================== */

                <form
                  onSubmit={(event) =>
                    handleUpdateTask(
                      event,
                      task.id
                    )
                  }
                  className="p-6"
                >

                  <div className="mb-6 flex items-center justify-between">

                    <div>
                      <h3 className="text-lg font-semibold text-slate-900">
                        Edit Task
                      </h3>

                      <p className="mt-1 text-sm text-slate-500">
                        Update task information.
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

                    <div>
                      <label
                        htmlFor={`edit-title-${task.id}`}
                        className={labelClass}
                      >
                        Task Title
                      </label>

                      <input
                        id={`edit-title-${task.id}`}
                        type="text"
                        value={editTitle}
                        onChange={(event) =>
                          setEditTitle(
                            event.target.value
                          )
                        }
                        className={inputClass}
                      />
                    </div>

                    <div>
                      <label
                        htmlFor={`edit-description-${task.id}`}
                        className={labelClass}
                      >
                        Description
                      </label>

                      <textarea
                        id={`edit-description-${task.id}`}
                        value={editDescription}
                        onChange={(event) =>
                          setEditDescription(
                            event.target.value
                          )
                        }
                        rows={4}
                        className={`${inputClass} resize-none`}
                      />
                    </div>

                    <div>
                      <label
                        htmlFor={`edit-priority-${task.id}`}
                        className={labelClass}
                      >
                        Priority
                      </label>

                      <select
                        id={`edit-priority-${task.id}`}
                        value={editPriority}
                        onChange={(event) =>
                          setEditPriority(
                            event.target.value
                          )
                        }
                        className={selectClass}
                      >
                        <option value="LOW">
                          Low
                        </option>

                        <option value="MEDIUM">
                          Medium
                        </option>

                        <option value="HIGH">
                          High
                        </option>

                        <option value="CRITICAL">
                          Critical
                        </option>
                      </select>
                    </div>

                    <div className="grid gap-5 md:grid-cols-2">

                      <div>
                        <label
                          htmlFor={`edit-assignee-${task.id}`}
                          className={labelClass}
                        >
                          Assignee
                        </label>

                        <select
                          id={`edit-assignee-${task.id}`}
                          value={editAssigneeId}
                          onChange={(event) =>
                            setEditAssigneeId(
                              event.target.value
                            )
                          }
                          className={selectClass}
                          disabled={!canManageAssignments}
                        >
                          <option value="">
                            Unassigned
                          </option>

                          {users.map((member) => (
                            <option
                              key={member.id}
                              value={member.id}
                            >
                              {member.fullName}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div>
                        <label
                          htmlFor={`edit-due-date-${task.id}`}
                          className={labelClass}
                        >
                          Due Date
                        </label>

                        <input
                          id={`edit-due-date-${task.id}`}
                          type="date"
                          value={editDueDate}
                          onChange={(event) =>
                            setEditDueDate(
                              event.target.value
                            )
                          }
                          className={inputClass}
                        />
                      </div>

                    </div>

                  </div>

                  <div className="mt-6 flex justify-end gap-3 border-t border-slate-200 pt-5">

                    <button
                      type="button"
                      onClick={cancelEditing}
                      disabled={saving}
                      className="rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:opacity-60"
                    >
                      Cancel
                    </button>

                    <button
                      type="submit"
                      disabled={saving}
                      className="inline-flex items-center gap-2 rounded-lg bg-slate-900 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:opacity-60"
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

                /* ==================================
                   TASK CARD
                ================================== */

                <>
                  <div className="flex flex-col gap-4 border-b border-slate-200 p-6 sm:flex-row sm:items-start sm:justify-between">

                    <div className="flex min-w-0 gap-4">

                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-slate-700">
                        <ListTodo size={19} />
                      </div>

                      <div className="min-w-0">

                        <div className="flex flex-wrap items-center gap-2">

                          <h3 className="text-base font-semibold text-slate-900">
                            {task.title}
                          </h3>

                          <span
                            className={`rounded-full px-2.5 py-1 text-xs font-semibold ${getStatusClass(
                              task.status
                            )}`}
                          >
                            {task.status.replace(
                              "_",
                              " "
                            )}
                          </span>

                        </div>

                        <p className="mt-1 text-xs text-slate-500">
                          Task #{task.id}
                        </p>

                      </div>

                    </div>

                    <span
                      className={`w-fit rounded-full px-3 py-1 text-xs font-semibold ${getPriorityClass(
                        task.priority
                      )}`}
                    >
                      {task.priority}
                    </span>

                  </div>

                  <div className="space-y-5 p-6">

                    <p className="text-sm leading-6 text-slate-600">
                      {task.description ||
                        "No description provided."}
                    </p>

                    {/* Metadata */}

                    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">

                      <div className="rounded-lg bg-slate-50 p-4">
                        <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                          <FolderKanban
                            size={14}
                          />
                          Project
                        </div>

                        <p className="mt-2 truncate text-sm font-semibold text-slate-800">
                          {task.projectName ||
                            task.projectId ||
                            "Unknown"}
                        </p>
                      </div>

                      <div className="rounded-lg bg-slate-50 p-4">
                        <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                          <UserRound
                            size={14}
                          />
                          Assignee
                        </div>

                        <p className="mt-2 truncate text-sm font-semibold text-slate-800">
                          {task.assigneeName ||
                            "Unassigned"}
                        </p>
                      </div>

                      <div className="rounded-lg bg-slate-50 p-4">
                        <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                          <CalendarDays
                            size={14}
                          />
                          Due Date
                        </div>

                        <p className="mt-2 text-sm font-semibold text-slate-800">
                          {task.dueDate ||
                            "No due date"}
                        </p>
                      </div>

                      <div className="rounded-lg bg-slate-50 p-4">
                        <div className="text-xs font-medium uppercase tracking-wide text-slate-500">
                          Priority
                        </div>

                        <p className="mt-2 text-sm font-semibold text-slate-800">
                          {task.priority}
                        </p>
                      </div>

                    </div>

                    {/* Controls */}

                    <div className="grid gap-4 border-t border-slate-200 pt-5 md:grid-cols-2">

                      <div>
                        <label
                          htmlFor={`status-${task.id}`}
                          className={labelClass}
                        >
                          Change Status
                        </label>

                        <select
                          id={`status-${task.id}`}
                          value={task.status}
                          onChange={(event) =>
                            handleStatusChange(
                              task.id,
                              event.target.value
                            )
                          }
                          className={selectClass}
                        >
                          <option value="BACKLOG">
                            Backlog
                          </option>

                          <option value="IN_PROGRESS">
                            In Progress
                          </option>

                          <option value="IN_REVIEW">
                            In Review
                          </option>

                          <option value="COMPLETED">
                            Completed
                          </option>
                        </select>
                      </div>

                      {canManageAssignments && (
                        <div>
                          <label
                            htmlFor={`assignee-${task.id}`}
                            className={labelClass}
                          >
                            Change Assignee
                          </label>

                          <select
                            id={`assignee-${task.id}`}
                            value={task.assigneeId ? String(task.assigneeId) : ""}
                            onChange={(event) =>
                              handleAssigneeChange(task.id, event.target.value)
                            }
                            className={selectClass}
                          >
                            <option value="">Unassigned</option>
                            {users.map((member) => (
                              <option key={member.id} value={member.id}>
                                {member.fullName}
                              </option>
                            ))}
                          </select>
                        </div>
                      )}

                    </div>

                    {/* Footer */}

                    <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-200 pt-5">

                      <p className="text-xs text-slate-400">
                        Task ID: #{task.id}
                      </p>

                      {canManageAssignments && (
                        <button
                          type="button"
                          onClick={() => startEditing(task)}
                          className="inline-flex items-center gap-2 rounded-lg border border-slate-300 px-3.5 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50"
                        >
                          <Pencil size={15} />
                          Edit Task
                        </button>
                      )}

                    </div>

                  </div>
                </>
              )}

            </article>
          ))}

        </div>
      )}

      {/* ==========================================
          PAGINATION
      =========================================== */}

      {totalPages > 0 && (
        <div className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-white px-5 py-4 shadow-sm sm:flex-row sm:items-center sm:justify-between">

          <p className="text-sm text-slate-500">
            Page{" "}
            <span className="font-medium text-slate-700">
              {page + 1}
            </span>{" "}
            of{" "}
            <span className="font-medium text-slate-700">
              {totalPages}
            </span>
          </p>

          <div className="flex items-center gap-2">

            <button
              type="button"
              disabled={page === 0}
              onClick={() =>
                setPage((current) =>
                  Math.max(
                    current - 1,
                    0
                  )
                )
              }
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
            >
              <ChevronLeft size={16} />
              Previous
            </button>

            <button
              type="button"
              disabled={
                page >= totalPages - 1
              }
              onClick={() =>
                setPage((current) =>
                  Math.min(
                    current + 1,
                    totalPages - 1
                  )
                )
              }
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
            >
              Next
              <ChevronRight size={16} />
            </button>

          </div>

        </div>
      )}

    </div>
  );
}

export default Tasks;