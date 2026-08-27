import apiClient from "../client";

import type {
    applicationDto,
    applicationStatus,
    createApplicationDto,
    updateApplicationDto,
} from '../../types/dtos';

export const applicationsService = {
    getAllApplications: async(): Promise<applicationDto[]> => {
        const response = await apiClient.get<applicationDto[]>('/applications');
        return response.data;
    },

    getApplicationById: async(id: number): Promise<applicationDto> => {
        const response = await apiClient.get<applicationDto>(`/applications/${id}`);
        return response.data;
    },

    getApplicationsByStudentId: async(studentId: number): Promise<applicationDto[]> => {
        const response = await apiClient.get<applicationDto[]>(`/applications/student/${studentId}`);
        return response.data;
    },

    getApplicationsByOpportunityId: async(
        opportunityId: number,
        status?: applicationStatus
    ): Promise<applicationDto[]> => {
        const response = await apiClient.get<applicationDto[]>(
            `/applications/opportunity/${opportunityId}`,
            { params: status ? { status } : undefined }
        );
        return response.data;
    },

    applyToOpportunity: async(dto: createApplicationDto): Promise<applicationDto> => {
        const response = await apiClient.post<applicationDto>('/applications', dto);
        return response.data;
    },

    updateApplication: async(id: number, dto: updateApplicationDto): Promise<applicationDto> => {
        const response = await apiClient.put<applicationDto>(`/applications/${id}`, dto);
        return response.data;
    },

    // Note: the backend expects the new status as a query param, not a body.
    updateApplicationStatus: async(id: number, status: applicationStatus): Promise<applicationDto> => {
        const response = await apiClient.patch<applicationDto>(`/applications/${id}/status`, null, {
            params: { status }
        });
        return response.data;
    },

    deleteApplication: async(id: number): Promise<void> => {
        await apiClient.delete<void>(`/applications/${id}`);
    },

    completeExpired: async(): Promise<applicationDto[]> => {
        const response = await apiClient.post<applicationDto[]>('/applications/complete-expired');
        return response.data;
    },
}
