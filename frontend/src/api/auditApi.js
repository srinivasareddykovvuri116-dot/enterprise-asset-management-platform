import axiosClient from "./axiosClient";

// ============================================================
// GET AUDIT LOGS
// Admin only
//
// Supported:
// GET /api/audit-logs
// GET /api/audit-logs?action=TASK_CREATED
// GET /api/audit-logs?actorId=1732
// GET /api/audit-logs?action=TASK_CREATED&actorId=1732
// ============================================================

export const getAuditLogs = async (filters = {}) => {
    const params = {};

    if (filters.action) {
        params.action = filters.action;
    }

    if (filters.actorId) {
        params.actorId = filters.actorId;
    }

    const response = await axiosClient.get(
        "/api/audit-logs",
        {
            params,
        }
    );

    return response.data;
};