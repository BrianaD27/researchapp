import apiClient from "../client";

import type {
    createResearchOpportunityDto,
    researchOpportunityDto,
    researchOpportunityCriteria,
    updateResearchOpportunityDto,
} from "../../types/dtos";

export const researchOpportunitiesService = {
    // Get all research opportunities
    getAllResearchOpportunities: async(): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>('/research-opportunities');
        return response.data;
    },

    // Get by id
    getResearchOpportunityById: async(id: number): Promise<researchOpportunityDto> => {
        const response = await apiClient.get<researchOpportunityDto>(`/research-opportunities/${id}`);
        return response.data;
    },

    // Get every opportunity posted by a given professor
    getResearchOpportunitiesByProfessorId: async(professorId: number): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>(
            `/research-opportunities/professor/${professorId}`
        );
        return response.data;
    },

    // Get by upcoming
    getUpcomingResearchOpportunities: async(): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>('/research-opportunities/upcoming');
        return response.data;
    },

    // Opportunities still open for applications
    getOpenForApplications: async(): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>(
            '/research-opportunities/open-for-applications'
        );
        return response.data;
    },

    // Search by date range (backend params: earliestDate / latestDate, "yyyy-MM-dd")
    getResearchOpportunitiesByDateRange: async(
        earliestDate: string,
        latestDate: string
    ): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>('/research-opportunities/date-range', {
            params: { earliestDate, latestDate }
        });
        return response.data;
    },

    // Free-text search
    searchResearchOpportunities: async(term: string): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>('/research-opportunities/search', {
            params: { term }
        });
        return response.data;
    },

    // Structured search - every field is optional
    searchByCriteria: async(criteria: researchOpportunityCriteria): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>('/research-opportunities/search/criteria', {
            params: criteria
        });
        return response.data;
    },

    // Create Research Opportunity - professorId is a required query param
    createResearchOpportunity: async(
        dto: createResearchOpportunityDto,
        professorId: number
    ): Promise<researchOpportunityDto> => {
        const response = await apiClient.post<researchOpportunityDto>('/research-opportunities', dto, {
            params: { professorId }
        });
        return response.data;
    },

    // Update Research Opportunity
    updateResearchOpportunity: async(
        id: number,
        dto: updateResearchOpportunityDto
    ): Promise<researchOpportunityDto> => {
        const response = await apiClient.put<researchOpportunityDto>(`/research-opportunities/${id}`, dto);
        return response.data;
    },

    // Delete Research Opportunity
    deleteResearchOpportunity: async(id: number): Promise<void> => {
        await apiClient.delete<void>(`/research-opportunities/${id}`)
    }
}
