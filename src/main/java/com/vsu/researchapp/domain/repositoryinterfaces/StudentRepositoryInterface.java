package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.queryObjects.StudentSearchCriteria;

public interface StudentRepositoryInterface {
    // CRUD Operations
    Student createStudent(Student student);
    Student getStudentById(Long id);
    Student updateStudent(Student student, Long id);
    void deleteStudent(Long id);
    List<Student> getAllStudents();

    // Lookups
    Optional<Student> getStudentByEmail(String email);
    boolean existsByEmail(String email);
    List<Student> searchStudents(String term);
    List<Student> searchStudentsByCriteria(StudentSearchCriteria criteria);
}
