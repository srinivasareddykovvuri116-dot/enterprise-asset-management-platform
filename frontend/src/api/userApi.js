import axiosClient from "./axiosClient";

export const getUsers = async () => {
  const response = await axiosClient.get("/api/users");

  return response.data;
};

export const createUser = async (data) => {
  const response = await axiosClient.post(
    "/api/users",
    data
  );

  return response.data;
};

export const getAssignableUsers = async () => {
  const response = await axiosClient.get(
    "/api/users/assignable"
  );

  return response.data;
};