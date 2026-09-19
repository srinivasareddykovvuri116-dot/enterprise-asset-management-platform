import { useEffect, useMemo, useState } from "react";

import {
  BarChart,
  Bar,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
} from "recharts";

import {
  CheckCircle2,
  Clock3,
  ListTodo,
  AlertTriangle,
  Users,
} from "lucide-react";

import { getDashboardAnalytics, getResourceAllocation } from "../../api/analyticsApi";

function Dashboard() {
  const [analytics, setAnalytics] = useState(null);
  const [resourceAllocation, setResourceAllocation] = useState([]);

  const [loading, setLoading] = useState(true);
  const [resourceLoading, setResourceLoading] = useState(false);

  const [error, setError] = useState("");
  const [resourceError, setResourceError] = useState("");

  // ============================================================
  // GET CURRENT USER ROLE
  // ============================================================

  const user = useMemo(() => {
    try {
      const storedUser = localStorage.getItem("user");

      if (!storedUser) {
        return null;
      }

      return JSON.parse(storedUser);
    } catch {
      return null;
    }
  }, []);

  const role = user?.role;

  const canViewResourceAllocation =
    role === "ORGANIZATION_ADMIN" ||
    role === "PROJECT_MANAGER";

  // ============================================================
  // LOAD DASHBOARD
  // ============================================================

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        setLoading(true);
        setError("");

        const data = await getDashboardAnalytics();

        setAnalytics(data);
      } catch (err) {
        console.error(
          "Failed to load dashboard analytics:",
          err
        );

        setError(
          err.response?.data?.message ||
            "Failed to load dashboard analytics."
        );
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, []);

  // ============================================================
  // LOAD RESOURCE ALLOCATION
  // ADMIN + PROJECT MANAGER ONLY
  // ============================================================

  useEffect(() => {
    const loadResourceAllocation = async () => {
      if (!canViewResourceAllocation) {
        setResourceAllocation([]);
        return;
      }

      try {
        setResourceLoading(true);
        setResourceError("");

        const data = await getResourceAllocation();

        setResourceAllocation(
          Array.isArray(data) ? data : []
        );
      } catch (err) {
        console.error(
          "Failed to load resource allocation:",
          err
        );

        setResourceError(
          err.response?.data?.message ||
            "Failed to load resource allocation."
        );
      } finally {
        setResourceLoading(false);
      }
    };

    loadResourceAllocation();
  }, [canViewResourceAllocation]);

  // ============================================================
  // STATUS DATA
  // ============================================================

  const statusData = useMemo(() => {
    if (!analytics?.tasksByStatus) {
      return [];
    }

    return Object.entries(
      analytics.tasksByStatus
    ).map(([status, count]) => ({
      name: status.replaceAll("_", " "),
      count,
    }));
  }, [analytics]);

  // ============================================================
  // PRIORITY DATA
  // ============================================================

  const priorityData = useMemo(() => {
    if (!analytics?.tasksByPriority) {
      return [];
    }

    return Object.entries(
      analytics.tasksByPriority
    ).map(([priority, count]) => ({
      name: priority,
      value: count,
    }));
  }, [analytics]);

  // ============================================================
  // LOADING
  // ============================================================

  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <div className="text-center">
          <div className="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-slate-200 border-t-slate-900" />

          <p className="text-sm text-slate-500">
            Loading dashboard...
          </p>
        </div>
      </div>
    );
  }

  // ============================================================
  // ERROR
  // ============================================================

  if (error) {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 p-6">
        <h2 className="text-lg font-semibold text-red-800">
          Dashboard Error
        </h2>

        <p className="mt-2 text-sm text-red-600">
          {error}
        </p>
      </div>
    );
  }

  // ============================================================
  // DASHBOARD
  // ============================================================

  return (
    <div className="space-y-8">

      {/* ======================================================
          PAGE HEADER
      ====================================================== */}

      <div>
        <h2 className="text-2xl font-bold tracking-tight text-slate-900">
          Dashboard
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          Organization analytics overview.
        </p>
      </div>

      {/* ======================================================
          KPI CARDS
      ====================================================== */}

      <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">

        {/* Total Tasks */}

        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">

            <div>
              <p className="text-sm font-medium text-slate-500">
                Total Tasks
              </p>

              <h3 className="mt-2 text-3xl font-bold text-slate-900">
                {analytics?.totalTasks ?? 0}
              </h3>
            </div>

            <div className="rounded-lg bg-slate-100 p-3">
              <ListTodo
                size={22}
                className="text-slate-700"
              />
            </div>

          </div>
        </div>

        {/* Completed */}

        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">

            <div>
              <p className="text-sm font-medium text-slate-500">
                Completed
              </p>

              <h3 className="mt-2 text-3xl font-bold text-slate-900">
                {analytics?.completedTasks ?? 0}
              </h3>
            </div>

            <div className="rounded-lg bg-slate-100 p-3">
              <CheckCircle2
                size={22}
                className="text-slate-700"
              />
            </div>

          </div>
        </div>

        {/* In Progress */}

        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">

            <div>
              <p className="text-sm font-medium text-slate-500">
                In Progress
              </p>

              <h3 className="mt-2 text-3xl font-bold text-slate-900">
                {analytics?.tasksByStatus?.IN_PROGRESS ?? 0}
              </h3>
            </div>

            <div className="rounded-lg bg-slate-100 p-3">
              <Clock3
                size={22}
                className="text-slate-700"
              />
            </div>

          </div>
        </div>

        {/* Overdue */}

        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">

            <div>
              <p className="text-sm font-medium text-slate-500">
                Overdue
              </p>

              <h3 className="mt-2 text-3xl font-bold text-slate-900">
                {analytics?.overdueTasks ?? 0}
              </h3>
            </div>

            <div className="rounded-lg bg-slate-100 p-3">
              <AlertTriangle
                size={22}
                className="text-slate-700"
              />
            </div>

          </div>
        </div>

      </div>

      {/* ======================================================
          CHARTS
      ====================================================== */}

      <div className="grid gap-6 lg:grid-cols-2">

        {/* Status Chart */}

        <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-6">
            <h3 className="text-lg font-semibold text-slate-900">
              Tasks by Status
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Distribution of tasks across workflow stages.
            </p>
          </div>

          {statusData.length > 0 ? (
            <div className="h-[320px] w-full">

              <ResponsiveContainer
                width="100%"
                height="100%"
              >
                <BarChart
                  data={statusData}
                  margin={{
                    top: 10,
                    right: 10,
                    left: -20,
                    bottom: 20,
                  }}
                >

                  <CartesianGrid
                    strokeDasharray="3 3"
                    vertical={false}
                  />

                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 12 }}
                    tickLine={false}
                    axisLine={false}
                  />

                  <YAxis
                    allowDecimals={false}
                    tick={{ fontSize: 12 }}
                    tickLine={false}
                    axisLine={false}
                  />

                  <Tooltip />

                  <Bar
                    dataKey="count"
                    radius={[6, 6, 0, 0]}
                  />

                </BarChart>
              </ResponsiveContainer>

            </div>
          ) : (
            <div className="flex h-[320px] items-center justify-center">

              <p className="text-sm text-slate-500">
                No status data available.
              </p>

            </div>
          )}

        </section>

        {/* Priority Chart */}

        <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-6">
            <h3 className="text-lg font-semibold text-slate-900">
              Tasks by Priority
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Distribution of task priorities.
            </p>
          </div>

          {priorityData.length > 0 ? (
            <div className="h-[320px] w-full">

              <ResponsiveContainer
                width="100%"
                height="100%"
              >
                <PieChart>

                  <Pie
                    data={priorityData}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    outerRadius={105}
                    label
                  >

                    {priorityData.map(
                      (entry, index) => (
                        <Cell
                          key={`cell-${index}`}
                        />
                      )
                    )}

                  </Pie>

                  <Tooltip />

                </PieChart>
              </ResponsiveContainer>

            </div>
          ) : (
            <div className="flex h-[320px] items-center justify-center">

              <p className="text-sm text-slate-500">
                No priority data available.
              </p>

            </div>
          )}

        </section>

      </div>

      {/* ======================================================
          RESOURCE ALLOCATION
          ADMIN + PROJECT MANAGER ONLY
      ====================================================== */}

      {canViewResourceAllocation && (
        <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-6 flex items-start justify-between">

            <div>
              <div className="flex items-center gap-3">

                <div className="rounded-lg bg-slate-100 p-3">
                  <Users
                    size={22}
                    className="text-slate-700"
                  />
                </div>

                <div>
                  <h3 className="text-lg font-semibold text-slate-900">
                    Resource Allocation
                  </h3>

                  <p className="mt-1 text-sm text-slate-500">
                    Task assignments across team members.
                  </p>
                </div>

              </div>
            </div>

            <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600">
              {resourceAllocation.length} Team Members
            </span>

          </div>

          {resourceLoading ? (
            <div className="flex min-h-[160px] items-center justify-center">

              <div className="text-center">

                <div className="mx-auto mb-3 h-7 w-7 animate-spin rounded-full border-4 border-slate-200 border-t-slate-900" />

                <p className="text-sm text-slate-500">
                  Loading resource allocation...
                </p>

              </div>

            </div>
          ) : resourceError ? (
            <div className="rounded-lg border border-red-200 bg-red-50 p-4">

              <p className="text-sm text-red-600">
                {resourceError}
              </p>

            </div>
          ) : resourceAllocation.length === 0 ? (
            <div className="rounded-lg border border-dashed border-slate-300 p-8 text-center">

              <Users
                size={30}
                className="mx-auto mb-3 text-slate-400"
              />

              <p className="text-sm font-medium text-slate-700">
                No resource allocation data
              </p>

              <p className="mt-1 text-sm text-slate-500">
                Assign tasks to team members to see allocation data.
              </p>

            </div>
          ) : (
            <div className="overflow-hidden rounded-lg border border-slate-200">

              <table className="w-full">

                <thead className="bg-slate-50">

                  <tr className="border-b border-slate-200">

                    <th className="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Team Member
                    </th>

                    <th className="px-5 py-3 text-right text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Assigned Tasks
                    </th>

                  </tr>

                </thead>

                <tbody className="divide-y divide-slate-100 bg-white">

                  {resourceAllocation.map(
                    (member) => (
                      <tr
                        key={member.userId}
                        className="transition-colors hover:bg-slate-50"
                      >

                        <td className="px-5 py-4">

                          <div className="flex items-center gap-3">

                            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-sm font-semibold text-slate-700">
                              {member.userName
                                ?.charAt(0)
                                ?.toUpperCase() || "U"}
                            </div>

                            <div>

                              <p className="font-medium text-slate-900">
                                {member.userName}
                              </p>

                              <p className="text-xs text-slate-500">
                                User ID: {member.userId}
                              </p>

                            </div>

                          </div>

                        </td>

                        <td className="px-5 py-4 text-right">

                          <span className="inline-flex min-w-10 items-center justify-center rounded-full bg-slate-100 px-3 py-1.5 text-sm font-semibold text-slate-700">
                            {member.assignedTasks}
                          </span>

                        </td>

                      </tr>
                    )
                  )}

                </tbody>

              </table>

            </div>
          )}

        </section>
      )}

      {/* ======================================================
          SUMMARY
      ====================================================== */}

      <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">

        <div className="mb-5">

          <h3 className="text-lg font-semibold text-slate-900">
            Task Summary
          </h3>

          <p className="mt-1 text-sm text-slate-500">
            Current organization task overview.
          </p>

        </div>

        <div className="grid gap-4 sm:grid-cols-3">

          <div className="rounded-lg bg-slate-50 p-4">

            <p className="text-sm text-slate-500">
              Total Tasks
            </p>

            <p className="mt-1 text-xl font-semibold text-slate-900">
              {analytics?.totalTasks ?? 0}
            </p>

          </div>

          <div className="rounded-lg bg-slate-50 p-4">

            <p className="text-sm text-slate-500">
              Completed
            </p>

            <p className="mt-1 text-xl font-semibold text-slate-900">
              {analytics?.completedTasks ?? 0}
            </p>

          </div>

          <div className="rounded-lg bg-slate-50 p-4">

            <p className="text-sm text-slate-500">
              Overdue
            </p>

            <p className="mt-1 text-xl font-semibold text-slate-900">
              {analytics?.overdueTasks ?? 0}
            </p>

          </div>

        </div>

      </section>

    </div>
  );
}

export default Dashboard;