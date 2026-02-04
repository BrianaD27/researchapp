import apiClient from "../client";

import type { createResearchOpportunityDto, researchOpportunityDto, updateResearchOpportunityDto } from "../../types/dtos";

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

    // Get by upcoming
    getUpcomingResearchOpportunities: async(): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>('/research-opportunities/upcoming');
        return response.data;
    },

    // Search by Date Range
    getResearchOpportunitiesByDateRange: async(startDate: string, endDate: string): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>(`/research-opportunities/date-range`, {
            params: {
                startDate,
                endDate
            }
        });
        return response.data;
    },

    // Search Research Opportunities
    searchResearchOpportunities: async(term: string): Promise<researchOpportunityDto[]> => {
        const response = await apiClient.get<researchOpportunityDto[]>(`/research-opportunities/search`, {
            params: { term }
        });
        return response.data;
    },

    // Create Research Opportunity
    createResearchOpportunity: async(dto: createResearchOpportunityDto): Promise<researchOpportunityDto> => {
        const response = await apiClient.post<researchOpportunityDto>('/research-opportunities', dto);
        return response.data;
    },

    // Update Research Opportunity
    updateResearchOpportunity: async(id: number, dto: updateResearchOpportunityDto): Promise<researchOpportunityDto> => {
        const response = await apiClient.put<researchOpportunityDto>(`/research-opportunities/${id}`, dto);
        return response.data;
    },

    // Delete Research Opportunity 
    deleteResearchOpportunity: async(id: number): Promise<void> => {
        await apiClient.delete<void>(`/research-opportunities/${id}`)
    }   
}