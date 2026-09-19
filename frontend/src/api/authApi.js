import axiosClient from "./axiosClient";

export const registerUser = async (data) => {
  const response = await axiosClient.post("/api/auth/register", data);

  return response.data;
};

export const loginUser = async (data) => {
  const response = await axiosClient.post("/api/auth/login", data);

  return response.data;
};