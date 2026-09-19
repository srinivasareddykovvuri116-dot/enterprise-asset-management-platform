import axiosClient from "./axiosClient";

// ============================================================
// WORKSPACE DASHBOARD
// Available to Admin, Project Manager, Team Member
// ============================================================

export const getDashboardAnalytics = async () => {
    const response = await axiosClient.get("/api/dashboard");

    return response.data;
};

// ============================================================
// RESOURCE ALLOCATION
// Available to Admin and Project Manager
// ============================================================

export const getResourceAllocation = async () => {
    const response = await axiosClient.get(
        "/api/analytics/tasks/resource-allocation"
    );

    return response.data;
};