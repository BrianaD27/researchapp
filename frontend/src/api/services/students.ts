import apiClient from "../client";

import type { studentDto, createStudentDto, updateStudentDto } from './../../types/dtos';


export const studentsService = {

    getallStudents: async(): Promise<studentDto[]> => {
        const response = await apiClient.get<studentDto[]>('/students');
        return response.data;
    },

    getStudentById: async(id: number): Promise<studentDto> => {
        const response = await apiClient.get<studentDto>(`/students/${id}`);
        return response.data
    },

    searchStudents: async(term: string): Promise<studentDto[]> => {
        const response = await apiClient.get<studentDto[]>(`/students/search`, {
            params: { term }
        });

        return response.data;
    },

    createStudent: async(dto: createStudentDto): Promise<studentDto> => {
        const response = await apiClient.post<studentDto>(`/students`, dto);
        return response.data;
    },

    updateStudent: async(id: number, dto: updateStudentDto): Promise<studentDto> => {
        const response = await apiClient.put<studentDto>(`/students/${id}`, dto);
        return response.data;
    },

    deleteStudent: async(id: number): Promise<void> => {
        await apiClient.delete<void>(`/students/${id}`);
    }
}