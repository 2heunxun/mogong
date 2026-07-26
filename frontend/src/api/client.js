import axios from 'axios';
import { getAccessToken, getRefreshToken, setAccessToken, setRefreshToken, clearTokens } from './tokenStore';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const api = axios.create({ baseURL: API_BASE_URL });

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshPromise = null;

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const refreshToken = getRefreshToken();

    if (error.response?.status === 401 && original && !original._retry && refreshToken) {
      original._retry = true;
      try {
        if (!refreshPromise) {
          refreshPromise = axios
            .post(`${API_BASE_URL}/api/auth/refresh`, { refreshToken })
            .then((res) => res.data)
            .finally(() => {
              refreshPromise = null;
            });
        }
        const data = await refreshPromise;
        setAccessToken(data.accessToken);
        setRefreshToken(data.refreshToken);
        original.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(original);
      } catch (refreshError) {
        clearTokens();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
