import axios from "axios";

const axiosClient = axios.create({
  baseURL:
    import.meta.env.VITE_API_BASE_URL ||
    "http://localhost:8080",

  headers: {
    "Content-Type": "application/json",
  },
});

// Attach JWT to authenticated requests
axiosClient.interceptors.request.use(
  (config) => {
    const isAuthRequest =
      config.url === "/api/auth/login" ||
      config.url === "/api/auth/register";

    // Do not attach an old JWT to login/register requests
    if (!isAuthRequest) {
      const token = localStorage.getItem("token");

      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle authentication failures
axiosClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");

      window.dispatchEvent(new Event("auth:logout"));
    }

    return Promise.reject(error);
  }
);

export default axiosClient;