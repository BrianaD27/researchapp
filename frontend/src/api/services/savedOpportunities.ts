import apiClient from "../client";

import type { createSavedOpportunityDto, savedOpportunityDto } from '../../types/dtos';

// Student-side bookmarking of research opportunities.
export const savedOpportunitiesService = {
    getByStudentId: async(studentId: number): Promise<savedOpportunityDto[]> => {
        const response = await apiClient.get<savedOpportunityDto[]>(
            `/saved-opportunities/student/${studentId}`
        );
        return response.data;
    },

    save: async(dto: createSavedOpportunityDto): Promise<savedOpportunityDto> => {
        const response = await apiClient.post<savedOpportunityDto>('/saved-opportunities', dto);
        return response.data;
    },

    unsave: async(studentId: number, opportunityId: number): Promise<void> => {
        await apiClient.delete<void>(
            `/saved-opportunities/student/${studentId}/opportunity/${opportunityId}`
        );
    },
}
