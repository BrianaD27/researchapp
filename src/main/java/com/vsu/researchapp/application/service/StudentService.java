package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    // constructor injection
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // this is what your controller is calling
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // this is what getStudentById() in the controller calls
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found: " + id));
    }

    // create a new student (if you need it later)
    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    // update an existing student (add/remove fields as your Student model has)
    public Student updateStudent(Long id, Student updated) {
        Student existing = getStudentById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        // add any other fields you have on Student here
        return studentRepository.save(existing);
    }

    // delete by id
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}
