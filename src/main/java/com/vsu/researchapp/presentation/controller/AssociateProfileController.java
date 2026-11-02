package com.vsu.researchapp.presentation.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vsu.researchapp.application.dto.EducationDto;
import com.vsu.researchapp.application.service.AssociateProfileService;
import com.vsu.researchapp.application.service.AssociateProfileService.ProfileView;
import com.vsu.researchapp.application.service.AssociateProfileService.TeamMember;

// Under /api/associates/**, which SecurityConfig limits to VSU roles.
@RestController
public class AssociateProfileController {

    public record UpdateRequest(String name, String title, String department, String about,
                                String outsideInterests, List<EducationDto> education) {}

    private final AssociateProfileService service;

    public AssociateProfileController(AssociateProfileService service) {
        this.service = service;
    }

    @GetMapping("/api/associates/profile/me")
    public ProfileView mine() {
        return service.mine();
    }

    @PutMapping("/api/associates/profile/me")
    public ProfileView update(@RequestBody UpdateRequest request) {
        return service.update(request.name(), request.title(), request.department(), request.about(),
            request.outsideInterests(), request.education());
    }

    @PostMapping(value = "/api/associates/profile/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileView uploadPhoto(@RequestPart("file") MultipartFile file) throws IOException {
        return service.uploadPhoto(file);
    }

    @DeleteMapping("/api/associates/profile/me/photo")
    public ProfileView deletePhoto() {
        return service.deletePhoto();
    }

    @GetMapping("/api/associates/profile/{userAccountId}")
    public ProfileView byAccount(@PathVariable Long userAccountId) {
        return service.byAccount(userAccountId);
    }

    @GetMapping("/api/associates/team-public/{professorId}")
    public List<TeamMember> team(@PathVariable Long professorId) {
        return service.team(professorId);
    }
}
