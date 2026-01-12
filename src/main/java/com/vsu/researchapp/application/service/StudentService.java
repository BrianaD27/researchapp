package com.vsu.researchapp.application.service;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import com.vsu.researchapp.application.dto.CreateStudentDto;
import com.vsu.researchapp.application.dto.StudentDto;
import com.vsu.researchapp.application.dto.UpdateStudentDto;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<StudentDto> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream().map(this::entityToDto).toList();
    }

    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found: " + id));
        return entityToDto(student);
    }

    public List<StudentDto> searchStudents(String term) {
        List<Student> students = studentRepository.searchStudents(term);
        return students.stream().map(this::entityToDto).toList();
    }

    public StudentDto createStudent(CreateStudentDto student) {
        if (student.graduateYear() < 2000 || student.graduateYear() > 2100) {
            throw new IllegalArgumentException("Graduate year must be between 2000 and 2100");
        }

        Student newStudent = new Student();
        newStudent.setName(student.name());
        newStudent.setEmail(student.email());
        newStudent.setMajor(student.major());
        newStudent.setGraduateYear(student.graduateYear());
        newStudent.setDescription(student.description());
        newStudent.setSkills(student.skills());

        Student savedStudent = studentRepository.save(newStudent);
        return entityToDto(savedStudent);
    }

    public StudentDto updateStudent(UpdateStudentDto updated, Long id) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found: " + id));

        if (updated.graduateYear() < 2000 || updated.graduateYear() > 2100) {
            throw new IllegalArgumentException("Graduate year must be between 2000 and 2100");
        }

        existingStudent.setName(updated.name());
        existingStudent.setMajor(updated.major());
        existingStudent.setGraduateYear(updated.graduateYear());
        existingStudent.setDescription(updated.description());
        existingStudent.setSkills(updated.skills());

        Student savedStudent = studentRepository.save(existingStudent);
        return entityToDto(savedStudent);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    private StudentDto entityToDto(Student student) {
        return new StudentDto(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getGraduateYear(),
                student.getMajor(),
                student.getDescription(),
                student.getSkills(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}
