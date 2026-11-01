package com.vsu.researchapp.presentation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.service.MessageService;
import com.vsu.researchapp.application.service.MessageService.ContactDto;
import com.vsu.researchapp.application.service.MessageService.ConversationDto;
import com.vsu.researchapp.application.service.MessageService.MessageDto;
import com.vsu.researchapp.application.service.MessageService.ThreadDto;
import com.vsu.researchapp.application.service.PresenceService;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    public record StartRequest(Long targetId) {}
    public record SendRequest(String body) {}
    public record ShowActiveRequest(boolean show) {}

    private static final String ACTING = "X-Acting-For";

    private final MessageService service;
    private final PresenceService presence;
    private final CurrentUserService currentUser;

    public MessageController(MessageService service, PresenceService presence, CurrentUserService currentUser) {
        this.service = service;
        this.presence = presence;
        this.currentUser = currentUser;
    }

    @GetMapping("/conversations")
    public List<ConversationDto> conversations(@RequestHeader(value = ACTING, required = false) String actingFor) {
        return service.conversations(service.context(actingFor));
    }

    @GetMapping("/conversations/{id}")
    public ThreadDto thread(@PathVariable Long id, @RequestHeader(value = ACTING, required = false) String actingFor) {
        return service.thread(service.context(actingFor), id);
    }

    @PostMapping("/conversations/{id}")
    public MessageDto send(@PathVariable Long id, @RequestBody SendRequest request,
            @RequestHeader(value = ACTING, required = false) String actingFor) {
        return service.send(service.context(actingFor), id, request.body());
    }

    @PostMapping("/start")
    public ConversationDto start(@RequestBody StartRequest request,
            @RequestHeader(value = ACTING, required = false) String actingFor) {
        return service.start(service.context(actingFor), request.targetId());
    }

    @GetMapping("/contacts")
    public List<ContactDto> contacts(@RequestHeader(value = ACTING, required = false) String actingFor) {
        return service.contacts(service.context(actingFor));
    }

    // Admins and others without messaging get 0 instead of an error, so the top bar stays quiet.
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@RequestHeader(value = ACTING, required = false) String actingFor) {
        try {
            return Map.of("count", service.unreadCount(service.context(actingFor)));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return Map.of("count", 0L);
        }
    }

    // Green dots for lists of people. Everyone in the VSU app can see who is online.
    @GetMapping("/presence/students")
    public Map<Long, Boolean> studentPresence(@RequestParam(defaultValue = "") List<Long> ids) {
        return service.studentPresence(ids);
    }

    @GetMapping("/presence/professors")
    public Map<Long, Boolean> professorPresence(@RequestParam(defaultValue = "") List<Long> ids) {
        return service.professorPresence(ids);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat() {
        presence.heartbeat(currentUser.getCurrentUserAccount().getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/settings")
    public Map<String, Boolean> settings() {
        return Map.of("showActive", presence.showActive(currentUser.getCurrentUserAccount().getId()));
    }

    @PutMapping("/settings")
    public Map<String, Boolean> updateSettings(@RequestBody ShowActiveRequest request) {
        Long me = currentUser.getCurrentUserAccount().getId();
        presence.setShowActive(me, request.show());
        return Map.of("showActive", presence.showActive(me));
    }
}
