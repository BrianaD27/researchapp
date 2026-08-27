import apiClient from "../client";

import type {
    loginRequest,
    loginResponse,
    refreshResponse,
    registerRequest,
    verify2faResponse,
} from '../../types/dtos';

// Auth lives under /api/v1/auth while the resource controllers are under /api/...
// The shared client's baseURL is "/api", so these paths start with "/v1/auth".
export const authService = {
    login: async(dto: loginRequest): Promise<loginResponse> => {
        const response = await apiClient.post<loginResponse>('/v1/auth/login', dto);
        return response.data;
    },

    register: async(dto: registerRequest): Promise<{ message: string; username: string }> => {
        const response = await apiClient.post<{ message: string; username: string }>(
            '/v1/auth/register', dto
        );
        return response.data;
    },

    logout: async(username: string): Promise<{ message: string }> => {
        const response = await apiClient.post<{ message: string }>('/v1/auth/logout', { username });
        return response.data;
    },

    verify2fa: async(username: string, code: string): Promise<verify2faResponse> => {
        const response = await apiClient.post<verify2faResponse>('/v1/auth/verify-2fa', { username, code });
        return response.data;
    },

    refresh: async(refreshToken: string): Promise<refreshResponse> => {
        const response = await apiClient.post<refreshResponse>('/v1/auth/refresh', { refreshToken });
        return response.data;
    },
}
