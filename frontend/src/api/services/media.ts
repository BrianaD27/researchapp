import apiClient from "../client";

import type { mediaUploadResponseDto } from '../../types/dtos';

const multipart = { headers: { 'Content-Type': 'multipart/form-data' } };

const fileBody = (file: File): FormData => {
    const form = new FormData();
    form.append('file', file);
    return form;
};

// Multipart uploads. The backend part name is always "file".
export const mediaService = {
    uploadStudentProfilePicture: async(studentId: number, file: File): Promise<mediaUploadResponseDto> => {
        const response = await apiClient.post<mediaUploadResponseDto>(
            `/students/${studentId}/profile-picture`, fileBody(file), multipart
        );
        return response.data;
    },

    uploadProfessorProfilePicture: async(professorId: number, file: File): Promise<mediaUploadResponseDto> => {
        const response = await apiClient.post<mediaUploadResponseDto>(
            `/professors/${professorId}/profile-picture`, fileBody(file), multipart
        );
        return response.data;
    },

    uploadResearchMedia: async(opportunityId: number, file: File): Promise<mediaUploadResponseDto> => {
        const response = await apiClient.post<mediaUploadResponseDto>(
            `/research-opportunities/${opportunityId}/media`, fileBody(file), multipart
        );
        return response.data;
    },

    listResearchMedia: async(opportunityId: number): Promise<string[]> => {
        const response = await apiClient.get<string[]>(
            `/research-opportunities/${opportunityId}/media`
        );
        return response.data;
    },

    replaceResearchMedia: async(
        opportunityId: number,
        oldUrl: string,
        file: File
    ): Promise<mediaUploadResponseDto> => {
        const response = await apiClient.put<mediaUploadResponseDto>(
            `/research-opportunities/${opportunityId}/media`,
            fileBody(file),
            { ...multipart, params: { url: oldUrl } }
        );
        return response.data;
    },

    deleteResearchMedia: async(opportunityId: number, url: string): Promise<void> => {
        await apiClient.delete<void>(`/research-opportunities/${opportunityId}/media`, {
            params: { url }
        });
    },
}
