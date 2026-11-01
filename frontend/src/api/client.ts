import axios from "axios";
import { API_ORIGIN } from "./config";

const apiClient = axios.create({
  baseURL: `${API_ORIGIN}/api`,
  timeout: 30000,
  headers: { "Content-Type": "application/json" },
});

apiClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("researchapp.accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    const replacement = response.headers["x-new-token"];
    const current = sessionStorage.getItem("researchapp.accessToken");
    const sent = response.config.headers.Authorization;

    // Accept rotation only for the session that made this request.
    // Late responses must not restore a logged-out or different session.
    if (typeof replacement === "string"
        && replacement.split(".").length === 3
        && current
        && sent === `Bearer ${current}`) {
      sessionStorage.setItem("researchapp.accessToken", replacement);
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem("researchapp.accessToken");
      sessionStorage.removeItem("researchapp.refreshToken");
    }
    return Promise.reject(error);
  },
);

export default apiClient;
