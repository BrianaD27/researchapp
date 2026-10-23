package com.vsu.researchapp.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.CreateStudentDto;
import com.vsu.researchapp.application.dto.StudentDto;
import com.vsu.researchapp.application.dto.UpdateStudentDto;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

@Service
public class StudentService {
    private final StudentRepositoryInterface studentRepository;
    private final CurrentUserService currentUserService;

    public StudentService(StudentRepositoryInterface studentRepository,
            CurrentUserService currentUserService) {
        this.studentRepository = studentRepository;
        this.currentUserService = currentUserService;
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
        if (!dto.email().endsWith("@students.vsu.edu")) {
            throw new IllegalArgumentException("A valid VSU email is required");
        }
        if (studentRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("A student with this email already exists");
        }

        UserAccount currentUser = currentUserService.getCurrentUserAccount();
        boolean isAdmin = currentUserService.isAdmin(currentUser);

        // One student profile per login account. Without this check, a STUDENT
        // account could call this endpoint repeatedly and end up "owning" - and
        // therefore being able to edit/delete - more than one student record.
        if (!isAdmin && studentRepository.getStudentByUserAccountId(currentUser.getId()).isPresent()) {
            throw new IllegalStateException("This account already has a student profile");
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

        // The owning account is taken from the authenticated JWT, never from the
        // request body - otherwise any caller could claim someone else's profile
        // by simply putting a different id in the request. Admin-created records
        // are left unlinked (userAccountId stays null) since an admin is creating
        // the record on someone else's behalf, not for their own account.
        if (!isAdmin) {
            newStudent.setUserAccountId(currentUser.getId());
        }

        return entityToDto(studentRepository.createStudent(newStudent));
    }

    public StudentDto updateStudent(UpdateStudentDto updated, Long id) {
        Student existingStudent = studentRepository.getStudentById(id);
        assertCanModify(existingStudent);

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
        Student existingStudent = studentRepository.getStudentById(id);
        assertCanModify(existingStudent);
        studentRepository.deleteStudent(id);
    }

    // Shared ownership gate for update/delete: an ADMIN can touch any student
    // record; anyone else must be the account that owns this specific profile.
    // SecurityConfig already restricts these endpoints to STUDENT/ADMIN roles, but
    // that only proves "you're SOME student" - this proves "you're THIS student."
    private void assertCanModify(Student student) {
        UserAccount currentUser = currentUserService.getCurrentUserAccount();
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (student.getUserAccountId() == null
                || !student.getUserAccountId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only modify your own student profile");
        }
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
