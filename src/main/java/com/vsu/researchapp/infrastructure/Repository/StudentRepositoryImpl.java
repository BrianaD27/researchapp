package com.vsu.researchapp.infrastructure.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.vsu.researchapp.domain.exception.StudentNotFoundException;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.queryObjects.StudentSearchCriteria;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;

@Repository
public class StudentRepositoryImpl implements StudentRepositoryInterface {

    private final StudentJpaRepository studentRepository;

    public StudentRepositoryImpl(StudentJpaRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException("There is no student with the id: " + id));
    }

    @Override
    public Student updateStudent(Student student, Long id) {
        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException("There is no student with the id: " + id);
        }
        student.setId(id);
        return studentRepository.save(student);
    }

    @Override
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException("There is no student with the id: " + id);
        }
        studentRepository.deleteById(id);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }

    @Override
    public Optional<Student> getStudentByUserAccountId(Long userAccountId) {
        return studentRepository.findByUserAccountId(userAccountId);
    }

    @Override
    public List<Student> searchStudents(String term) {
        return studentRepository.searchByTerm(term);
    }

    @Override
    public List<Student> searchStudentsByCriteria(StudentSearchCriteria criteria) {
        return studentRepository.findAll(buildSpecification(criteria));
    }

    private Specification<Student> buildSpecification(StudentSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getInput() != null && !criteria.getInput().isBlank()) {
                String like = "%" + criteria.getInput().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("email")), like)
                ));
            }
            if (criteria.getMajor() != null && !criteria.getMajor().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("major")), criteria.getMajor().toLowerCase()));
            }
            if (criteria.getClassification() != null && !criteria.getClassification().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("classification")), criteria.getClassification().toLowerCase()));
            }
            if (criteria.getGpa() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("gpa"), criteria.getGpa()));
            }
            if (criteria.getAvailability() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("availableHoursPerWeek"), criteria.getAvailability()));
            }
            if (criteria.getSkills() != null && !criteria.getSkills().isEmpty()) {
                query.distinct(true);
                Join<Object, Object> skillJoin = root.join("skills");
                List<String> lowerSkills = criteria.getSkills().stream().map(String::toLowerCase).toList();
                predicates.add(cb.lower(skillJoin.as(String.class)).in(lowerSkills));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
