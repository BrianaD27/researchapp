import apiClient from "./client";

import type { professorDto, createProfessorDto, updateProfessorDto } from '../types/dtos';

// Object with API functions for professors
export const professorsApi = {
    // Get all professors
    getAllProfessors: async(): Promise<professorDto[]> => {
        const response = await apiClient.get<professorDto[]>('/professors');
        return response.data;
    },

    // Get by id
    getProfessorById: async(id: number): Promise<professorDto> => {
        const response = await apiClient.get<professorDto>(`/professors/${id}`);
        return response.data;
    },

    // Create Professor
    createProfessor: async(dto: createProfessorDto): Promise<professorDto> => {
        const response = await apiClient.post<professorDto>('/professors', dto);
        return response.data;
    },

    // Update Professor
    updateProfessor: async(id: number, dto: updateProfessorDto): Promise<professorDto> => {
        const response = await apiClient.put<professorDto>(`/professors/${id}`, dto);
        return response.data;
    },

    // Delete Professor 
    deleteProfessor: async(id: number): Promise<void> => {
        await apiClient.delete<void>(`/professors/${id}`)
    }


}