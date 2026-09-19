import axiosClient from "./axiosClient";

export const getTasks = async (params = {}) => {
  const response = await axiosClient.get("/api/tasks", {
    params,
  });

  return response.data;
};

export const getTask = async (taskId) => {
  const response = await axiosClient.get(
    `/api/tasks/${taskId}`
  );

  return response.data;
};

export const createTask = async (data) => {
  const response = await axiosClient.post(
    "/api/tasks",
    data
  );

  return response.data;
};

export const updateTask = async (taskId, data) => {
  const response = await axiosClient.put(
    `/api/tasks/${taskId}`,
    data
  );

  return response.data;
};

export const updateTaskStatus = async (
  taskId,
  status
) => {
  const response = await axiosClient.patch(
    `/api/tasks/${taskId}/status`,
    {
      status,
    }
  );

  return response.data;
};

export const assignTask = async (
  taskId,
  assigneeId
) => {
  const response = await axiosClient.patch(
    `/api/tasks/${taskId}/assignee`,
    {
      assigneeId,
    }
  );

  return response.data;
};