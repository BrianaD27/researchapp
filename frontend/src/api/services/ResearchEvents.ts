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

    // Search by Date Range
    getResearchEventsByDateRange: async(startDate: string, endDate: string): Promise<researchEventDto[]> => {
        const response = await apiClient.get<researchEventDto[]>(`/research-events/date-range`, {
            params: {
                startDate,
                endDate
            }
        });
        return response.data;
    },

    // Create Research Event
    createResearchEvent: async(dto: createResearchEventDto): Promise<researchEventDto> => {
        const response = await apiClient.post<researchEventDto>('/research-events', dto);
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