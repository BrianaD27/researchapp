import axios from 'axios';

import { getToken, setToken, clearSession } from './token';

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
    // A 401 means the server rejected our token (missing, expired or invalid).
    if (error.response?.status === 401) {
      // Don't react to a failed login attempt itself - the login page shows its
      // own "wrong username or password" message. Only react when a token we
      // thought was good has stopped working.
      const requestUrl: string = error.config?.url ?? '';
      const isLoginRequest = requestUrl.includes('/v1/auth/login');

      if (!isLoginRequest) {
        clearSession();
        // This interceptor runs outside React, so we can't use useNavigate here.
        // A plain browser navigation is fine and also guarantees a clean state.
        if (window.location.pathname !== '/StudentLogin') {
          window.location.assign('/StudentLogin');
        }
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
