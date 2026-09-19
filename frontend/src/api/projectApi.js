import axiosClient from "./axiosClient";

export const getProjects = async () => {
  const response = await axiosClient.get("/api/projects");
  return response.data;
};

export const createProject = async (data) => {
  const response = await axiosClient.post(
    "/api/projects",
    data
  );

  return response.data;
};

export const updateProject = async (projectId, data) => {
  const response = await axiosClient.put(
    `/api/projects/${projectId}`,
    data
  );

  return response.data;
};

export const archiveProject = async (projectId) => {
  const response = await axiosClient.patch(
    `/api/projects/${projectId}/archive`
  );

  return response.data;
};

export const restoreProject = async (projectId) => {
  const response = await axiosClient.patch(
    `/api/projects/${projectId}/restore`
  );

  return response.data;
};