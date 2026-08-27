import apiClient from "../client";

import type { createSavedStudentDto, savedStudentDto } from '../../types/dtos';

// Professor-side bookmarking of student profiles.
export const savedStudentsService = {
    getByProfessorId: async(professorId: number): Promise<savedStudentDto[]> => {
        const response = await apiClient.get<savedStudentDto[]>(
            `/saved-students/professor/${professorId}`
        );
        return response.data;
    },

    save: async(dto: createSavedStudentDto): Promise<savedStudentDto> => {
        const response = await apiClient.post<savedStudentDto>('/saved-students', dto);
        return response.data;
    },

    unsave: async(professorId: number, studentId: number): Promise<void> => {
        await apiClient.delete<void>(
            `/saved-students/professor/${professorId}/student/${studentId}`
        );
    },
}
