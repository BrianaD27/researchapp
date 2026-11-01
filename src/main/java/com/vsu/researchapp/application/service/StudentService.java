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
import com.vsu.researchapp.infrastructure.security.StudentOpportunityAccess;

@Service
public class StudentService {
    private final StudentRepositoryInterface studentRepository;
    private final CurrentUserService currentUserService;

    private final StudentOpportunityAccess access;

    public StudentService(StudentRepositoryInterface studentRepository,
            CurrentUserService currentUserService, StudentOpportunityAccess access) {
        this.studentRepository = studentRepository;
        this.currentUserService = currentUserService;
        this.access = access;
    }

    public Optional<StudentDto> getMyStudentProfile() {
        UserAccount currentUser = currentUserService.getCurrentUserAccount();
        return studentRepository.getStudentByUserAccountId(currentUser.getId())
            .map(this::entityToDto);
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
        validateRanges(dto.gpa(), dto.availableHoursPerWeek());

        UserAccount currentUser = currentUserService.getCurrentUserAccount();
        boolean isAdmin = currentUserService.isAdmin(currentUser);

        // A student's own profile must use the email they signed up with, so nobody
        // can claim a profile under someone else's address. Admins are exempt.
        if (!isAdmin && (currentUser.getEmail() == null
                || !currentUser.getEmail().trim().equalsIgnoreCase(dto.email().trim()))) {
            throw new IllegalArgumentException("Use the same email address you signed up with.");
        }

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
        newStudent.setOutsideInterests(dto.outsideInterests());
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
        validateRanges(updated.gpa(), updated.availableHoursPerWeek());

        Optional.ofNullable(updated.name()).ifPresent(existingStudent::setName);
        Optional.ofNullable(updated.major()).ifPresent(existingStudent::setMajor);
        Optional.ofNullable(updated.graduationYear()).ifPresent(existingStudent::setGraduationYear);
        Optional.ofNullable(updated.classification()).ifPresent(existingStudent::setClassification);
        Optional.ofNullable(updated.description()).ifPresent(existingStudent::setDescription);
        Optional.ofNullable(updated.previousExperience()).ifPresent(existingStudent::setPreviousExperience);
        Optional.ofNullable(updated.outsideInterests()).ifPresent(existingStudent::setOutsideInterests);
        Optional.ofNullable(updated.gpa()).ifPresent(existingStudent::setGpa);
        Optional.ofNullable(updated.availableHoursPerWeek()).ifPresent(existingStudent::setAvailableHoursPerWeek);
        // resumeUrl is set only by the resume upload endpoint. A link sent by the
        // browser is ignored, so a profile can't point professors at another site.
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

    // GPA and the resume link are visible to the student who owns the profile,
    // faculty, and admins - not to other students browsing profiles.
    private boolean canSeePrivateFields(Student student) {
        UserAccount viewer = currentUserService.getCurrentUserAccount();
        if (currentUserService.isAdmin(viewer) || "PROFESSOR".equalsIgnoreCase(viewer.getRole())
                || access.isReviewingAssociate(viewer)) {
            return true;
        }
        return student.getUserAccountId() != null
            && student.getUserAccountId().equals(viewer.getId());
    }

    private StudentDto entityToDto(Student student) {
        boolean showPrivate = canSeePrivateFields(student);
        return new StudentDto(
            student.getId(),
            student.getName(),
            student.getEmail(),
            student.getMajor(),
            student.getGraduationYear(),
            student.getClassification(),
            student.getDescription(),
            student.getPreviousExperience(),
            student.getOutsideInterests(),
            showPrivate ? student.getGpa() : null,
            student.getAvailableHoursPerWeek(),
            showPrivate ? student.getResumeUrl() : null,
            student.getProfilePictureUrl(),
            student.getSkills(),
            student.getCreatedAt(),
            student.getUpdatedAt()
        );
    }

    private void validateRanges(Float gpa, Integer hours) {
        if (gpa != null && (gpa < 0f || gpa > 4.0f)) {
            throw new IllegalArgumentException("GPA must be between 0.0 and 4.0.");
        }
        if (hours != null && (hours < 0 || hours > 80)) {
            throw new IllegalArgumentException("Available hours per week must be between 0 and 80.");
        }
    }
}
