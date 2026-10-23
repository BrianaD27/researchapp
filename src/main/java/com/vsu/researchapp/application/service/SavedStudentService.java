package com.vsu.researchapp.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.CreateSavedStudentDto;
import com.vsu.researchapp.application.dto.SavedStudentDto;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.SavedStudent;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.SavedStudentRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;

// Thin by design: a bookmark has no business rules beyond "don't save it twice."
@Service
public class SavedStudentService {

    private final SavedStudentRepositoryInterface savedStudentRepository;
    private final ProfessorRepositoryInterface professorRepository;
    private final StudentRepositoryInterface studentRepository;

    public SavedStudentService(
            SavedStudentRepositoryInterface savedStudentRepository,
            ProfessorRepositoryInterface professorRepository,
            StudentRepositoryInterface studentRepository) {
        this.savedStudentRepository = savedStudentRepository;
        this.professorRepository = professorRepository;
        this.studentRepository = studentRepository;
    }

    public List<SavedStudentDto> getSavedStudentsByProfessorId(Long professorId) {
        return savedStudentRepository.getSavedStudentsByProfessorId(professorId).stream().map(this::entityToDto).toList();
    }

    public SavedStudentDto saveStudent(CreateSavedStudentDto dto) {
        if (savedStudentRepository.isStudentSavedByProfessor(dto.professorId(), dto.studentId())) {
            throw new IllegalArgumentException("This student is already saved");
        }

        Professor professor = professorRepository.getProfessorById(dto.professorId());
        Student student = studentRepository.getStudentById(dto.studentId());

        SavedStudent savedStudent = new SavedStudent();
        savedStudent.setProfessor(professor);
        savedStudent.setStudent(student);

        return entityToDto(savedStudentRepository.saveStudent(savedStudent));
    }

    public void unsaveStudent(Long professorId, Long studentId) {
        savedStudentRepository.unsaveStudentByProfessorIdAndStudentId(professorId, studentId);
    }

    private SavedStudentDto entityToDto(SavedStudent savedStudent) {
        return new SavedStudentDto(
            savedStudent.getId(),
            savedStudent.getProfessor().getId(),
            savedStudent.getProfessor().getName(),
            savedStudent.getStudent().getId(),
            savedStudent.getStudent().getName(),
            savedStudent.getSavedAt(),
            savedStudent.getUpdatedAt()
        );
    }
}
