import apiClient from "../client";

import type { researchEventDto, createResearchEventDto, updateResearchEventDto } from '../../types/dtos';

// Object with API functions for research events
export const researchEventsService = {
    // Get all research events
    getAllResearchEvents: async(): Promise<researchEventDto[]> => {
        const response = await apiClient.get<researchEventDto[]>('/research-events');
        return response.data;
    },

    // Get by id
    getResearchEventById: async(id: number): Promise<researchEventDto> => {
        const response = await apiClient.get<researchEventDto>(`/research-events/${id}`);
        return response.data;
    },

    // Get by upcoming
    getUpcomingResearchEvents: async(): Promise<researchEventDto[]> => {
        const response = await apiClient.get<researchEventDto[]>('/research-events/upcoming');
        return response.data;
    },

    // Search by date range (backend params: earliestDate / latestDate, "yyyy-MM-dd")
    getResearchEventsByDateRange: async(
        earliestDate: string,
        latestDate: string
    ): Promise<researchEventDto[]> => {
        const response = await apiClient.get<researchEventDto[]>('/research-events/date-range', {
            params: { earliestDate, latestDate }
        });
        return response.data;
    },

    // Create Research Event - professorId is a required query param
    createResearchEvent: async(
        dto: createResearchEventDto,
        professorId: number
    ): Promise<researchEventDto> => {
        const response = await apiClient.post<researchEventDto>('/research-events', dto, {
            params: { professorId }
        });
        return response.data;
    },

    // Update Research Event
    updateResearchEvent: async(id: number, dto: updateResearchEventDto): Promise<researchEventDto> => {
        const response = await apiClient.put<researchEventDto>(`/research-events/${id}`, dto);
        return response.data;
    },

    // Delete Research Event
    deleteResearchEvent: async(id: number): Promise<void> => {
        await apiClient.delete<void>(`/research-events/${id}`)
    }
}
