package com.vsu.researchapp.domain.repositoryinterfaces;
import com.vsu.researchapp.domain.model.Application;
import java.util.List;

public interface ApplicationRepository {
    List<Application> getOpportunitiesByStudentIdAndStatus(Long studentId, Application.OpportunityStatus status);

}
