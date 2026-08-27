import axios from 'axios';

import { getToken, setToken, clearToken } from './token';

// Base URL comes from the environment so the Vite dev proxy ("/api" ->
// http://localhost:8080) is used in development and a real host can be set in
// production. See frontend/.env.development.
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach the JWT to every request when we have one.
apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle auth globally on the response side.
apiClient.interceptors.response.use(
  (response) => {
    // The backend rotates tokens that are close to expiry and returns the new
    // one in a response header - keep it if present.
    const rotated = response.headers['x-new-token'];
    if (rotated) {
      setToken(rotated);
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      clearToken();
      // TODO: redirect to the login route once routing/auth context exists.
      console.error('Unauthorized access - token cleared');
    }
    return Promise.reject(error);
  }
);

export default apiClient;
