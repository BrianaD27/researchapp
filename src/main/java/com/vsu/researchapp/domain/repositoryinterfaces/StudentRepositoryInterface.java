package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;

import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.queryObjects.StudentSearchCriteria;

public interface StudentRepositoryInterface {
    Student createStudent(Student student);
    Student getStudentById(Long id);
    Student updateStudent(Student student);
    void deleteStudent(Long id);
    List<Student> getAllStudents();
    List<Student> searchStudentsByCriteria(StudentSearchCriteria criteria);
    
} 
