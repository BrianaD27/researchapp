package com.vsu.researchapp.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.CreateStudentDto;
import com.vsu.researchapp.application.dto.StudentDto;
import com.vsu.researchapp.application.dto.UpdateStudentDto;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;

@Service
public class StudentService {
    private final StudentRepositoryInterface studentRepository;

    public StudentService(StudentRepositoryInterface studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<StudentDto> getAllStudents() {
        return studentRepository.getAllStudents().stream().map(this::entityToDto).toList();
    }

    public StudentDto getStudentById(Long id) {
        return entityToDto(studentRepository.getStudentById(id));
    }

    public List<StudentDto> searchStudents(String term) {
        return studentRepository.searchStudents(term).stream().map(this::entityToDto).toList();
    }

    public StudentDto createStudent(CreateStudentDto dto) {
        if (studentRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("A student with this email already exists");
        }

        Student newStudent = new Student();
        newStudent.setName(dto.name());
        newStudent.setEmail(dto.email());
        newStudent.setMajor(dto.major());
        newStudent.setGraduationYear(dto.graduationYear());
        newStudent.setClassification(dto.classification());
        newStudent.setDescription(dto.description());
        newStudent.setPreviousExperience(dto.previousExperience());
        newStudent.setGpa(dto.gpa());
        newStudent.setAvailableHoursPerWeek(dto.availableHoursPerWeek());
        newStudent.setSkills(dto.skills());

        return entityToDto(studentRepository.createStudent(newStudent));
    }

    public StudentDto updateStudent(UpdateStudentDto updated, Long id) {
        Student existingStudent = studentRepository.getStudentById(id);

        Optional.ofNullable(updated.name()).ifPresent(existingStudent::setName);
        Optional.ofNullable(updated.major()).ifPresent(existingStudent::setMajor);
        Optional.ofNullable(updated.graduationYear()).ifPresent(existingStudent::setGraduationYear);
        Optional.ofNullable(updated.classification()).ifPresent(existingStudent::setClassification);
        Optional.ofNullable(updated.description()).ifPresent(existingStudent::setDescription);
        Optional.ofNullable(updated.previousExperience()).ifPresent(existingStudent::setPreviousExperience);
        Optional.ofNullable(updated.gpa()).ifPresent(existingStudent::setGpa);
        Optional.ofNullable(updated.availableHoursPerWeek()).ifPresent(existingStudent::setAvailableHoursPerWeek);
        Optional.ofNullable(updated.resumeUrl()).ifPresent(existingStudent::setResumeUrl);
        Optional.ofNullable(updated.skills()).ifPresent(existingStudent::setSkills);

        return entityToDto(studentRepository.updateStudent(existingStudent, id));
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteStudent(id);
    }

    private StudentDto entityToDto(Student student) {
        return new StudentDto(
            student.getId(),
            student.getName(),
            student.getEmail(),
            student.getMajor(),
            student.getGraduationYear(),
            student.getClassification(),
            student.getDescription(),
            student.getPreviousExperience(),
            student.getGpa(),
            student.getAvailableHoursPerWeek(),
            student.getResumeUrl(),
            student.getProfilePictureUrl(),
            student.getSkills(),
            student.getCreatedAt(),
            student.getUpdatedAt()
        );
    }
}
