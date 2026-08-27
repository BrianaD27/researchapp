// These types mirror the backend DTOs in
// src/main/java/com/vsu/researchapp/application/dto/. Only JSON-facing fields.
// Dates are ISO strings: LocalDate -> "yyyy-MM-dd", LocalTime -> "HH:mm:ss",
// LocalDateTime -> "yyyy-MM-ddTHH:mm:ss".

// ---------------------------------------------------------------------------
// Shared enums (serialized as strings)
// ---------------------------------------------------------------------------
export type opportunityStatus = 'BOOKMARKED' | 'APPLIED' | 'COMPLETED';
export type applicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

// ---------------------------------------------------------------------------
// Student DTOs
// ---------------------------------------------------------------------------
export interface studentDto {
    id: number;
    name: string;
    email: string;
    major: string;
    graduationYear: number;
    classification: string;
    description: string;
    previousExperience: string | null;
    gpa: number | null;
    availableHoursPerWeek: number | null;
    resumeUrl: string | null;
    profilePictureUrl: string | null;
    skills: string[];
    createdAt: string;
    updatedAt: string;
}

export interface createStudentDto {
    name: string;
    email: string;
    major: string;
    graduationYear: number;
    classification: string;
    description: string;
    previousExperience?: string;
    gpa?: number;
    availableHoursPerWeek?: number;
    skills: string[];
}

export interface updateStudentDto {
    name?: string;
    major?: string;
    graduationYear?: number;
    classification?: string;
    description?: string;
    previousExperience?: string;
    gpa?: number;
    availableHoursPerWeek?: number;
    resumeUrl?: string;
    skills?: string[];
}

// ---------------------------------------------------------------------------
// Professor DTOs
// ---------------------------------------------------------------------------
export interface professorDto {
    id: number;
    name: string;
    email: string;
    department: string;
    officeLocation: string | null;
    description: string | null;
    profilePictureUrl: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface createProfessorDto {
    name: string;
    email: string;
    department: string;
    officeLocation: string;
}

export interface updateProfessorDto {
    name?: string;
    department?: string;
    officeLocation?: string;
    description?: string;
    profilePictureUrl?: string;
}

// ---------------------------------------------------------------------------
// Research Opportunity DTOs
// ---------------------------------------------------------------------------
export interface researchOpportunityDto {
    id: number;
    title: string;
    description: string;
    department: string;
    requiredMajors: string[];
    requiredClassifications: string[];
    requiredSkills: string[];
    availability: string;
    minimumGpa: number | null;
    applicationDeadline: string;
    startDate: string;
    endDate: string;
    researchMediaUrls: string[];
    createdAt: string;
    updatedAt: string;
    professorId: number;
    professorName: string;
}

export interface createResearchOpportunityDto {
    title: string;
    description: string;
    department: string;
    requiredMajors: string[];
    requiredClassifications?: string[];
    requiredSkills?: string[];
    availability: string;
    minimumGpa?: number;
    applicationDeadline: string;
    startDate: string;
    endDate: string;
    researchMediaUrls?: string[];
}

export interface updateResearchOpportunityDto {
    title?: string;
    description?: string;
    department?: string;
    requiredMajors?: string[];
    requiredClassifications?: string[];
    requiredSkills?: string[];
    availability?: string;
    minimumGpa?: number;
    applicationDeadline?: string;
    startDate?: string;
    endDate?: string;
    researchMediaUrls?: string[];
}

export interface researchOpportunityCriteria {
    input?: string;
    major?: string;
    classification?: string;
    gpa?: number;
    availability?: number;
    skills?: string[];
}

// ---------------------------------------------------------------------------
// Research Event DTOs
// ---------------------------------------------------------------------------
export interface researchEventDto {
    id: number;
    title: string;
    description: string;
    address: string;
    registrationLink: string | null;
    beginDate: string;
    endDate: string;
    startTime: string;
    endTime: string;
    createdAt: string;
    updatedAt: string;
    createdById: number;
    createdByName: string;
}

export interface createResearchEventDto {
    title: string;
    description: string;
    address: string;
    registrationLink?: string;
    beginDate: string;
    endDate: string;
    startTime: string;
    endTime: string;
}

export interface updateResearchEventDto {
    title?: string;
    description?: string;
    address?: string;
    registrationLink?: string;
    beginDate?: string;
    endDate?: string;
    startTime?: string;
    endTime?: string;
}

// ---------------------------------------------------------------------------
// Application DTOs
// ---------------------------------------------------------------------------
export interface applicationDto {
    id: number;
    studentId: number;
    studentName: string;
    researchOpportunityId: number;
    researchOpportunityTitle: string;
    opportunityStatus: opportunityStatus;
    applicationStatus: applicationStatus;
    appliedAt: string;
    updatedAt: string;
}

export interface createApplicationDto {
    studentId: number;
    researchOpportunityId: number;
}

export interface updateApplicationDto {
    opportunityStatus?: opportunityStatus;
    applicationStatus?: applicationStatus;
}

// ---------------------------------------------------------------------------
// Saved Opportunity / Saved Student DTOs
// ---------------------------------------------------------------------------
export interface savedOpportunityDto {
    id: number;
    studentId: number;
    studentName: string;
    opportunityId: number;
    opportunityTitle: string;
    savedAt: string;
}

export interface createSavedOpportunityDto {
    studentId: number;
    opportunityId: number;
}

export interface savedStudentDto {
    id: number;
    professorId: number;
    professorName: string;
    studentId: number;
    studentName: string;
    savedAt: string;
    updatedAt: string;
}

export interface createSavedStudentDto {
    professorId: number;
    studentId: number;
}

// ---------------------------------------------------------------------------
// Media
// ---------------------------------------------------------------------------
export interface mediaUploadResponseDto {
    url: string;
    originalFilename: string;
    contentType: string;
    size: number;
}

// ---------------------------------------------------------------------------
// User account / auth
// ---------------------------------------------------------------------------
export interface userAccountDto {
    id: number;
    username: string;
    email: string;
    role: string;
    active: boolean;
    accountLocked: boolean;
    twoFactorEnabled: boolean;
    lastLoginAt: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface loginRequest {
    username: string;
    password: string;
}

export interface registerRequest {
    username: string;
    email: string;
    password: string;
    role?: 'STUDENT' | 'PROFESSOR';
}

export interface loginResponse {
    token?: string;
    refreshToken?: string;
    type?: string;
    // Present instead of tokens when the account has 2FA enabled.
    status?: '2FA_REQUIRED';
    message?: string;
}

export interface verify2faResponse {
    token: string;
    type: string;
}

export interface refreshResponse {
    token: string;
    type?: string;
}
