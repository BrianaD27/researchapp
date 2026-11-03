package com.vsu.researchapp.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.service.AssociateService;
import com.vsu.researchapp.application.service.AssociateService.RemovalView;
import com.vsu.researchapp.application.service.AssociateService.SessionInfo;
import com.vsu.researchapp.application.service.AssociateService.TeamOverview;

// Ownership and role checks live in AssociateService.
@RestController
@RequestMapping("/api/associates")
public class AssociateController {

    public record AddAssociateRequest(String username, boolean canPublish, boolean canReview) {}
    public record PermissionsRequest(boolean canPublish, boolean canReview) {}
    public record RemovalRequestBody(Long opportunityId, String reason) {}
    public record ProjectsRequest(List<Long> projectIds) {}

    private final AssociateService service;

    public AssociateController(AssociateService service) {
        this.service = service;
    }

    @GetMapping("/session")
    public SessionInfo session() {
        return service.session();
    }

    @GetMapping("/professor/{professorId}")
    public TeamOverview team(@PathVariable Long professorId) {
        return service.team(professorId);
    }

    @PostMapping("/professor/{professorId}")
    public TeamOverview add(@PathVariable Long professorId, @RequestBody AddAssociateRequest body) {
        return service.add(professorId, body.username(), body.canPublish(), body.canReview());
    }

    @PutMapping("/{linkId}")
    public TeamOverview updatePermissions(@PathVariable Long linkId, @RequestBody PermissionsRequest body) {
        return service.updatePermissions(linkId, body.canPublish(), body.canReview());
    }

    @PutMapping("/{linkId}/projects")
    public TeamOverview updateProjects(@PathVariable Long linkId, @RequestBody ProjectsRequest body) {
        return service.updateProjects(linkId, body.projectIds());
    }

    @DeleteMapping("/{linkId}")
    public TeamOverview remove(@PathVariable Long linkId) {
        return service.removeLink(linkId);
    }

    @PostMapping("/removal-requests")
    public RemovalView requestRemoval(@RequestBody RemovalRequestBody body) {
        if (body.opportunityId() == null) {
            throw new IllegalArgumentException("Choose a project.");
        }
        return service.requestRemoval(body.opportunityId(), body.reason());
    }

    @PostMapping("/removal-requests/{requestId}/approve")
    public TeamOverview approve(@PathVariable Long requestId) {
        return service.decideRemoval(requestId, true);
    }

    @PostMapping("/removal-requests/{requestId}/deny")
    public TeamOverview deny(@PathVariable Long requestId) {
        return service.decideRemoval(requestId, false);
    }
}
