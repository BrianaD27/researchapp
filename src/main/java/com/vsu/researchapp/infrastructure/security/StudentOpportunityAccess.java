package com.vsu.researchapp.infrastructure.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.ApplicationRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;

@Service("studentOpportunityAccess")
@Transactional(readOnly = true)
public class StudentOpportunityAccess {
    private final CurrentUserService currentUser;
    private final StudentRepositoryInterface students;
    private final ResearchOpportunityRepositoryInterface opportunities;
    private final ApplicationRepositoryInterface applications;

    public StudentOpportunityAccess(
            CurrentUserService currentUser,
            StudentRepositoryInterface students,
            ResearchOpportunityRepositoryInterface opportunities,
            ApplicationRepositoryInterface applications) {
        this.currentUser = currentUser;
        this.students = students;
        this.opportunities = opportunities;
        this.applications = applications;
    }

    public boolean admin() {
        return currentUser.isAdmin(currentUser.getCurrentUserAccount());
    }

    private boolean ownsStudent(UserAccount user, Student student) {
        return "STUDENT".equalsIgnoreCase(user.getRole())
            && user.getId() != null
            && user.getId().equals(student.getUserAccountId());
    }

    private boolean ownsOpportunity(UserAccount user, ResearchOpportunity opportunity) {
        return "PROFESSOR".equalsIgnoreCase(user.getRole())
            && opportunity.getProfessor() != null
            && user.getId() != null
            && user.getId().equals(opportunity.getProfessor().getUserAccountId());
    }

    public boolean student(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        return currentUser.isAdmin(user)
            || ownsStudent(user, students.getStudentById(id));
    }

    public boolean opportunity(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        return currentUser.isAdmin(user)
            || ownsOpportunity(user, opportunities.getResearchOpportunity(id));
    }

    public boolean readApplication(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        if (currentUser.isAdmin(user)) return true;
        Application application = applications.getApplicationById(id);
        return ownsStudent(user, application.getStudent())
            || ownsOpportunity(user, application.getResearchOpportunity());
    }

    public boolean decideApplication(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        if (currentUser.isAdmin(user)) return true;
        return ownsOpportunity(user,
            applications.getApplicationById(id).getResearchOpportunity());
    }

    public boolean deleteApplication(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        return currentUser.isAdmin(user)
            || ownsStudent(user, applications.getApplicationById(id).getStudent());
    }
}
