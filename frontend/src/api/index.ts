// This file serves as a central export point for all API services and related dto types.
export { default as apiClient } from './client';
export {
    getToken,
    setToken,
    clearToken,
    getRefreshToken,
    setRefreshToken,
    getUsername,
    setUsername,
    clearSession,
} from './token';
export { decodeToken, isTokenExpired } from './jwt';
export type { DecodedToken } from './jwt';

export { authService } from './services/auth';
export { applicationsService } from './services/applications';
export { mediaService } from './services/media';
export { professorsService } from './services/professors';
export { researchEventsService } from './services/ResearchEvents';
export { researchOpportunitiesService } from './services/ResearchOpportunities';
export { savedOpportunitiesService } from './services/savedOpportunities';
export { savedStudentsService } from './services/savedStudents';
export { studentsService } from './services/students';

export * from '../types/dtos';
