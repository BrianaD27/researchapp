package com.vsu.researchapp.infrastructure.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.vsu.researchapp.domain.exception.OpportunityNotFoundException;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.queryObjects.OpportunitySearchCriteria;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;

@Repository
public class ResearchOpportunityRepositoryImpl implements ResearchOpportunityRepositoryInterface {

    private final ResearchOpportunityRepository opportunityRepository;

    public ResearchOpportunityRepositoryImpl(ResearchOpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    @Override
    public ResearchOpportunity createResearchOpportunity(ResearchOpportunity opportunity) {
        return opportunityRepository.save(opportunity);
    }

    @Override
    public ResearchOpportunity getResearchOpportunity(Long id) {
        return opportunityRepository.findById(id)
            .orElseThrow(() -> new OpportunityNotFoundException("There is no research opportunity with the id: " + id));
    }

    @Override
    public ResearchOpportunity updateResearchOpportunity(ResearchOpportunity opportunity) {
        if (!opportunityRepository.existsById(opportunity.getId())) {
            throw new OpportunityNotFoundException("There is no research opportunity with the id: " + opportunity.getId());
        }
        return opportunityRepository.save(opportunity);
    }

    @Override
    public void deleteResearchOpportunity(Long id) {
        if (!opportunityRepository.existsById(id)) {
            throw new OpportunityNotFoundException("There is no research opportunity with the id: " + id);
        }
        opportunityRepository.deleteById(id);
    }

    @Override
    public List<ResearchOpportunity> getAllResearchOpportunities() {
        return opportunityRepository.findAll();
    }

    @Override
    public List<ResearchOpportunity> getResearchOpportunitiesByProfessorId(Long professorId) {
        return opportunityRepository.findByProfessorId(professorId);
    }

    @Override
    public List<ResearchOpportunity> getResearchOpportunitiesByDepartment(String department) {
        return opportunityRepository.findByDepartmentIgnoreCase(department);
    }

    @Override
    public List<ResearchOpportunity> getResearchOpportunitiesByUpcoming() {
        return opportunityRepository.findUpcoming();
    }

    @Override
    public List<ResearchOpportunity> getResearchOpportunitiesByDateRange(LocalDate earliestDate, LocalDate latestDate) {
        return opportunityRepository.findByDateRange(earliestDate, latestDate);
    }

    @Override
    public List<ResearchOpportunity> getOpenForApplications() {
        return opportunityRepository.findOpenForApplications();
    }

    @Override
    public List<ResearchOpportunity> searchOpportunities(String term) {
        return opportunityRepository.searchByTerm(term);
    }

    @Override
    public List<ResearchOpportunity> searchOpportunitiesByCriteria(OpportunitySearchCriteria criteria) {
        return opportunityRepository.findAll(buildSpecification(criteria));
    }

    // Note: criteria.getAvailability() (an hours/week number meant for Student search) has no
    // equivalent here — ResearchOpportunity.availability is a free-text String, not a number, so
    // it is intentionally not filtered on. Revisit once that field's shape is settled.
    private Specification<ResearchOpportunity> buildSpecification(OpportunitySearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getInput() != null && !criteria.getInput().isBlank()) {
                String like = "%" + criteria.getInput().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (criteria.getMajor() != null && !criteria.getMajor().isBlank()) {
                query.distinct(true);
                Join<Object, Object> majorJoin = root.join("requiredMajors");
                predicates.add(cb.equal(cb.lower(majorJoin.as(String.class)), criteria.getMajor().toLowerCase()));
            }
            if (criteria.getClassification() != null && !criteria.getClassification().isBlank()) {
                query.distinct(true);
                Join<Object, Object> classificationJoin = root.join("requiredClassifications");
                predicates.add(cb.equal(cb.lower(classificationJoin.as(String.class)), criteria.getClassification().toLowerCase()));
            }
            if (criteria.getGpa() != null) {
                predicates.add(cb.or(
                    cb.isNull(root.get("minimumGpa")),
                    cb.lessThanOrEqualTo(root.get("minimumGpa"), criteria.getGpa())
                ));
            }
            if (criteria.getSkills() != null && !criteria.getSkills().isEmpty()) {
                query.distinct(true);
                Join<Object, Object> skillJoin = root.join("requiredSkills");
                List<String> lowerSkills = criteria.getSkills().stream().map(String::toLowerCase).toList();
                predicates.add(cb.lower(skillJoin.as(String.class)).in(lowerSkills));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
