import { useEffect, useMemo, useState } from "react";

import {
  ScrollText,
  ClipboardList,
  UserRound,
  FileText,
  Clock3,
  Loader2,
  AlertCircle,
  ShieldCheck,
  Filter,
  X,
} from "lucide-react";

import { getAuditLogs } from "../../api/auditApi";

function AuditLogs() {
  const [logs, setLogs] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [selectedAction, setSelectedAction] = useState("");
  const [selectedActor, setSelectedActor] = useState("");

  // ============================================================
  // AUDIT ACTIONS
  // ============================================================

  const auditActions = [
    "ORGANIZATION_CREATED",
    "USER_CREATED",
    "USER_ACTIVATED",
    "USER_SUSPENDED",
    "PROJECT_CREATED",
    "PROJECT_UPDATED",
    "PROJECT_ARCHIVED",
    "TASK_CREATED",
    "TASK_UPDATED",
    "TASK_ASSIGNED",
    "TASK_STATUS_CHANGED",
  ];

  // ============================================================
  // LOAD AUDIT LOGS
  // ============================================================

  useEffect(() => {
    let active = true;

    const fetchAuditLogs = async () => {
      try {
        setLoading(true);
        setError("");

        const filters = {};

        if (selectedAction) {
          filters.action = selectedAction;
        }

        if (selectedActor) {
          filters.actorId = selectedActor;
        }

        const data = await getAuditLogs(filters);

        if (!active) {
          return;
        }

        setLogs(Array.isArray(data) ? data : []);
      } catch (err) {
        if (!active) {
          return;
        }

        console.error(
          "Failed to load audit logs:",
          err
        );

        setError(
          err.response?.data?.message ||
            "Failed to load audit logs."
        );
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void fetchAuditLogs();

    return () => {
      active = false;
    };
  }, [selectedAction, selectedActor]);

  // ============================================================
  // UNIQUE ACTORS
  // ============================================================

  const actors = useMemo(() => {
    const actorMap = new Map();

    logs.forEach((log) => {
      if (log.actorId !== null && log.actorId !== undefined) {
        actorMap.set(log.actorId, {
          id: log.actorId,
          name:
            log.actorName ||
            `User #${log.actorId}`,
        });
      }
    });

    return Array.from(actorMap.values()).sort(
      (a, b) =>
        a.name.localeCompare(b.name)
    );
  }, [logs]);

  // ============================================================
  // ACTION STYLE
  // ============================================================

  const getActionClass = (action) => {
    if (action?.includes("CREATED")) {
      return "bg-emerald-100 text-emerald-700";
    }

    if (
      action?.includes("UPDATED") ||
      action?.includes("ASSIGNED") ||
      action?.includes("CHANGED")
    ) {
      return "bg-blue-100 text-blue-700";
    }

    if (
      action?.includes("ARCHIVED") ||
      action?.includes("SUSPENDED")
    ) {
      return "bg-red-100 text-red-700";
    }

    if (action?.includes("ACTIVATED")) {
      return "bg-violet-100 text-violet-700";
    }

    return "bg-slate-100 text-slate-600";
  };

  // ============================================================
  // FORMAT ACTION
  // ============================================================

  const formatAction = (action) => {
    if (!action) {
      return "Unknown Action";
    }

    return action
      .replaceAll("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (letter) =>
        letter.toUpperCase()
      );
  };

  // ============================================================
  // FORMAT ENTITY
  // ============================================================

  const formatEntityType = (type) => {
    if (!type) {
      return "N/A";
    }

    return type
      .replaceAll("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (letter) =>
        letter.toUpperCase()
      );
  };

  // ============================================================
  // FORMAT DATE
  // ============================================================

  const formatDate = (value) => {
    if (!value) {
      return "N/A";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return date.toLocaleString();
  };

  // ============================================================
  // CLEAR FILTERS
  // ============================================================

  const clearFilters = () => {
    setSelectedAction("");
    setSelectedActor("");
  };

  const hasFilters =
    selectedAction !== "" ||
    selectedActor !== "";

  // ============================================================
  // LOADING
  // ============================================================

  if (loading) {
    return (
      <div className="flex min-h-[400px] items-center justify-center">
        <div className="flex items-center gap-3 text-slate-500">
          <Loader2
            size={20}
            className="animate-spin"
          />

          <span>
            Loading audit logs...
          </span>
        </div>
      </div>
    );
  }

  // ============================================================
  // PAGE
  // ============================================================

  return (
    <div className="space-y-6">

      {/* ======================================================
          HEADER
      ====================================================== */}

      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

        <div className="flex items-center gap-3">

          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-slate-900 text-white">
            <ScrollText size={21} />
          </div>

          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Audit Logs
            </h1>

            <p className="mt-1 text-sm text-slate-500">
              Track important activities across your organization.
            </p>
          </div>

        </div>

        <div className="inline-flex w-fit items-center gap-2 rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-medium text-slate-600 shadow-sm">
          <ShieldCheck size={16} />
          Admin Only
        </div>

      </div>

      {/* ======================================================
          ERROR
      ====================================================== */}

      {error && (
        <div className="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">

          <AlertCircle
            size={18}
            className="mt-0.5 shrink-0"
          />

          <span>{error}</span>

        </div>
      )}

      {/* ======================================================
          FILTERS
      ====================================================== */}

      <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">

        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">

          <div className="flex items-center gap-2">

            <Filter
              size={18}
              className="text-slate-600"
            />

            <div>
              <h2 className="text-sm font-semibold text-slate-900">
                Filters
              </h2>

              <p className="text-xs text-slate-500">
                Narrow the activity history.
              </p>
            </div>

          </div>

          {hasFilters && (
            <button
              type="button"
              onClick={clearFilters}
              className="inline-flex w-fit items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-xs font-medium text-slate-600 transition hover:bg-slate-50"
            >
              <X size={14} />
              Clear Filters
            </button>
          )}

        </div>

        <div className="grid gap-4 sm:grid-cols-2">

          {/* Action */}

          <div>
            <label
              htmlFor="audit-action"
              className="mb-2 block text-xs font-medium text-slate-600"
            >
              Action
            </label>

            <select
              id="audit-action"
              value={selectedAction}
              onChange={(event) =>
                setSelectedAction(
                  event.target.value
                )
              }
              className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 outline-none transition focus:border-slate-400 focus:ring-2 focus:ring-slate-100"
            >
              <option value="">
                All Actions
              </option>

              {auditActions.map((action) => (
                <option
                  key={action}
                  value={action}
                >
                  {formatAction(action)}
                </option>
              ))}
            </select>
          </div>

          {/* Actor */}

          <div>
            <label
              htmlFor="audit-actor"
              className="mb-2 block text-xs font-medium text-slate-600"
            >
              Actor
            </label>

            <select
              id="audit-actor"
              value={selectedActor}
              onChange={(event) =>
                setSelectedActor(
                  event.target.value
                )
              }
              className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 outline-none transition focus:border-slate-400 focus:ring-2 focus:ring-slate-100"
            >
              <option value="">
                All Actors
              </option>

              {actors.map((actor) => (
                <option
                  key={actor.id}
                  value={actor.id}
                >
                  {actor.name}
                </option>
              ))}
            </select>
          </div>

        </div>

      </section>

      {/* ======================================================
          SUMMARY
      ====================================================== */}

      <section className="grid gap-4 sm:grid-cols-2">

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">

          <div className="flex items-center gap-3">

            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
              <ClipboardList size={19} />
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                Total Events
              </p>

              <p className="mt-1 text-2xl font-bold text-slate-900">
                {logs.length}
              </p>
            </div>

          </div>

        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">

          <div className="flex items-center gap-3">

            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
              <Clock3 size={19} />
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                Activity History
              </p>

              <p className="mt-1 text-sm font-semibold text-slate-800">
                {hasFilters
                  ? "Filtered organization events"
                  : "Organization events"}
              </p>
            </div>

          </div>

        </div>

      </section>

      {/* ======================================================
          LOG LIST
      ====================================================== */}

      <section>

        <div className="mb-4 flex items-end justify-between">

          <div>
            <h2 className="text-lg font-semibold text-slate-900">
              Activity History
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Recent actions performed within your organization.
            </p>
          </div>

        </div>

        {logs.length === 0 ? (

          /* ==================================================
             EMPTY STATE
          ================================================== */

          <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">

            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-slate-100 text-slate-500">
              <ScrollText size={25} />
            </div>

            <h3 className="mt-4 text-base font-semibold text-slate-900">
              No audit logs found
            </h3>

            <p className="mx-auto mt-2 max-w-md text-sm text-slate-500">
              {hasFilters
                ? "No audit events match the selected filters."
                : "Organization activity will appear here as users perform actions."
              }
            </p>

            {hasFilters && (
              <button
                type="button"
                onClick={clearFilters}
                className="mt-4 inline-flex items-center gap-2 rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800"
              >
                <X size={15} />
                Clear Filters
              </button>
            )}

          </div>

        ) : (

          /* ==================================================
             LOG CARDS
          ================================================== */

          <div className="space-y-4">

            {logs.map((log) => (

              <article
                key={log.id}
                className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md"
              >

                {/* Log Header */}

                <div className="flex flex-col gap-4 border-b border-slate-200 p-5 sm:flex-row sm:items-center sm:justify-between">

                  <div className="flex min-w-0 items-center gap-4">

                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
                      <FileText size={18} />
                    </div>

                    <div className="min-w-0">

                      <div className="flex flex-wrap items-center gap-2">

                        <h3 className="text-sm font-semibold text-slate-900">
                          {formatAction(
                            log.action
                          )}
                        </h3>

                        <span
                          className={`rounded-full px-2.5 py-1 text-xs font-semibold ${getActionClass(
                            log.action
                          )}`}
                        >
                          {log.action ||
                            "UNKNOWN"}
                        </span>

                      </div>

                      <p className="mt-1 text-xs text-slate-500">
                        Audit Event #{log.id}
                      </p>

                    </div>

                  </div>

                  <div className="flex items-center gap-2 text-xs text-slate-500">

                    <Clock3 size={14} />

                    <span>
                      {formatDate(
                        log.createdAt
                      )}
                    </span>

                  </div>

                </div>

                {/* Log Details */}

                <div className="grid gap-4 p-5 sm:grid-cols-2 lg:grid-cols-4">

                  {/* Entity */}

                  <div className="rounded-lg bg-slate-50 p-4">

                    <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                      <FileText size={14} />
                      Entity
                    </div>

                    <p className="mt-2 text-sm font-semibold text-slate-800">
                      {formatEntityType(
                        log.entityType
                      )}
                    </p>

                  </div>

                  {/* Entity ID */}

                  <div className="rounded-lg bg-slate-50 p-4">

                    <div className="text-xs font-medium uppercase tracking-wide text-slate-500">
                      Entity ID
                    </div>

                    <p className="mt-2 text-sm font-semibold text-slate-800">
                      {log.entityId !== null &&
                      log.entityId !== undefined
                        ? `#${log.entityId}`
                        : "N/A"}
                    </p>

                  </div>

                  {/* Actor */}

                  <div className="rounded-lg bg-slate-50 p-4">

                    <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                      <UserRound size={14} />
                      Actor
                    </div>

                    <p className="mt-2 truncate text-sm font-semibold text-slate-800">
                      {log.actorName ||
                        log.actorId ||
                        "System"}
                    </p>

                  </div>

                  {/* Time */}

                  <div className="rounded-lg bg-slate-50 p-4">

                    <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
                      <Clock3 size={14} />
                      Created At
                    </div>

                    <p className="mt-2 text-sm font-semibold text-slate-800">
                      {formatDate(
                        log.createdAt
                      )}
                    </p>

                  </div>

                </div>

                {/* Details */}

                <div className="border-t border-slate-200 px-5 py-4">

                  <p className="mb-1 text-xs font-medium uppercase tracking-wide text-slate-500">
                    Details
                  </p>

                  <p className="text-sm leading-6 text-slate-600">
                    {log.details ||
                      "No additional details provided."}
                  </p>

                </div>

              </article>

            ))}

          </div>

        )}

      </section>

    </div>
  );
}

export default AuditLogs;