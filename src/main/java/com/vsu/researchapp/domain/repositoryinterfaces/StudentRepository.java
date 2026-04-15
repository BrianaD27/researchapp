package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;

import com.vsu.researchapp.domain.model.Student;

public interface StudentRepository {
    Student createStudent(Student student);
    Student getStudentById(Long id);
    Student updateStudent(Student student);
    void deleteStudent(Long id);
    List<Student> getAllStudents();
    List<Student> searchStudentsByName(String name);
    List<Student> searchStudentsByMajor(String major);
    List<Student> searchStudentsBySkills(String skill);
    List<Student> searchStudentsByClassification(String classification);
    List<Student> searchStudentsByAvailability(Integer availability);

} 
