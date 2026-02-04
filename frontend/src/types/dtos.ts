// These Dtos match the backend Dtos located in the backend project
// Only has JSON types

// Professor DTOs
export interface professorDto {
    id: number,
    name: string,
    email: string,
    department: string,
    title: string,
    createdAt: string,
    updatedAt: string
}

export interface createProfessorDto {
    name: string,
    email: string,
    department: string,
    title: string
}

export interface updateProfessorDto {
    name: string,
    department: string,
    title: string
}

// Student DTOs
export interface studentDto {
    id: number,
    name: string,
    email: string,
    graduateYear: number,
    major: string,
    description: string,
    skills: string,
    createdAt: string,
    updatedAt: string
}

export interface createStudentDto {
    name: string,
    email: string,
    graduateYear: number,
    major: string,
    description: string,
    skills: string
}

export interface updateStudentDto {
    name: string,
    graduateYear: number,
    major: string,
    description: string,
    skills: string
}

// Research Opportunity Dtos 
export interface researchOpportunityDto {
    id: number,
    title: string,
    description: string,
    requirements: string,
    beginDate: string,
    endDate: string,
    createdAt: string,
    updatedAt: string,
    createdById: number,
    createdByName: string
}

export interface createResearchOpportunityDto {
    title: string,
    description: string,
    requirements: string,
    beginDate: string,
    endDate: string
}

export interface updateResearchOpportunityDto {
    title: string,
    description: string,
    requirements: string,
    beginDate: string,
    endDate: string
}

// Research Event Dtos 
export interface researchEventDto {
    id: number,
    title: string,
    description: string,
    address: string,
    registrationLink: string,
    beginDate: string,
    endDate: string,
    startTime: string,
    endTime: string,
    createdAt: string,
    updatedAt: string,
    createdById: number,
    createdByName: string
}

export interface createResearchEventDto {
    title: string,
    description: string,
    address: string,
    registrationLink: string,
    beginDate: string,
    endDate: string,
    startTime: string,
    endTime: string
}

export interface updateResearchEventDto {
    title: string,
    description: string,
    address: string,
    registrationLink: string,
    beginDate: string,
    endDate: string,
    startTime: string,
    endTime: string
}