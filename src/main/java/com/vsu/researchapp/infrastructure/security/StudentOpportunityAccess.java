package com.vsu.researchapp.infrastructure.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.ApplicationRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateLinkRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;

@Service("studentOpportunityAccess")
@Transactional(readOnly = true)
public class StudentOpportunityAccess {
    private final CurrentUserService currentUser;
    private final StudentRepositoryInterface students;
    private final ResearchOpportunityRepositoryInterface opportunities;
    private final ApplicationRepositoryInterface applications;
    private final AssociateLinkRepository associateLinks;

    public StudentOpportunityAccess(
            CurrentUserService currentUser,
            StudentRepositoryInterface students,
            ResearchOpportunityRepositoryInterface opportunities,
            ApplicationRepositoryInterface applications,
            AssociateLinkRepository associateLinks) {
        this.currentUser = currentUser;
        this.students = students;
        this.opportunities = opportunities;
        this.applications = applications;
        this.associateLinks = associateLinks;
    }

    // True when the user is a Research Associate on this professor's team with
    // the needed permission. "publish" = post/edit projects; "review" = applicants.
    private boolean associateFor(UserAccount user, Professor professor, boolean publish, boolean review) {
        if (!"ASSOCIATE".equalsIgnoreCase(user.getRole()) || professor == null
                || professor.getId() == null || user.getId() == null) {
            return false;
        }
        return associateLinks.findByProfessorIdAndAssociateUserAccountId(professor.getId(), user.getId())
            .map(link -> (!publish || link.isCanPublish()) && (!review || link.isCanReview()))
            .orElse(false);
    }

    // Used when creating a project or changing its media: the professor themself,
    // an admin, or one of their associates who may publish.
    public boolean canPublishFor(Professor professor) {
        UserAccount user = currentUser.getCurrentUserAccount();
        if (currentUser.isAdmin(user)) return true;
        boolean owner = professor != null && professor.getUserAccountId() != null
            && professor.getUserAccountId().equals(user.getId())
            && "PROFESSOR".equalsIgnoreCase(user.getRole());
        return owner || associateFor(user, professor, true, false);
    }

    // Associates who review applicants for at least one professor see the same
    // applicant details (GPA, resume) that faculty do.
    public boolean isReviewingAssociate(UserAccount user) {
        return "ASSOCIATE".equalsIgnoreCase(user.getRole()) && user.getId() != null
            && associateLinks.existsByAssociateUserAccountIdAndCanReviewTrue(user.getId());
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

    // Edit a project: owner, admin, or an associate who may publish for its professor.
    public boolean opportunity(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        if (currentUser.isAdmin(user)) return true;
        ResearchOpportunity opportunity = opportunities.getResearchOpportunity(id);
        return ownsOpportunity(user, opportunity)
            || associateFor(user, opportunity.getProfessor(), true, false);
    }

    // See a project's applicants: owner, admin, or an associate who may review.
    public boolean opportunityApplicants(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        if (currentUser.isAdmin(user)) return true;
        ResearchOpportunity opportunity = opportunities.getResearchOpportunity(id);
        return ownsOpportunity(user, opportunity)
            || associateFor(user, opportunity.getProfessor(), false, true);
    }

    // Delete a project: owner or admin only. Associates send a removal request.
    public boolean deleteOpportunity(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        return currentUser.isAdmin(user)
            || ownsOpportunity(user, opportunities.getResearchOpportunity(id));
    }

    public boolean readApplication(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        if (currentUser.isAdmin(user)) return true;
        Application application = applications.getApplicationById(id);
        return ownsStudent(user, application.getStudent())
            || ownsOpportunity(user, application.getResearchOpportunity())
            || associateFor(user, application.getResearchOpportunity().getProfessor(), false, true);
    }

    public boolean decideApplication(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        if (currentUser.isAdmin(user)) return true;
        ResearchOpportunity opportunity = applications.getApplicationById(id).getResearchOpportunity();
        return ownsOpportunity(user, opportunity)
            || associateFor(user, opportunity.getProfessor(), false, true);
    }

    public boolean deleteApplication(Long id) {
        UserAccount user = currentUser.getCurrentUserAccount();
        return currentUser.isAdmin(user)
            || ownsStudent(user, applications.getApplicationById(id).getStudent());
    }
}
