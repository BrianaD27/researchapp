package com.vsu.researchapp.presentation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.service.MessageService;
import com.vsu.researchapp.application.service.MessageService.ConversationDto;
import com.vsu.researchapp.application.service.MessageService.MessageDto;
import com.vsu.researchapp.application.service.MessageService.PersonDto;
import com.vsu.researchapp.application.service.MessageService.ThreadDto;
import com.vsu.researchapp.application.service.PresenceService;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// Anyone in the VSU app can message anyone else. Partner-school accounts are
// blocked from /api/messages in SecurityConfig.
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    public record StartRequest(String kind, Long id) {}
    public record SendRequest(String body) {}
    public record ReportRequest(String reason, boolean block) {}
    public record ShowActiveRequest(boolean show) {}

    private final MessageService service;
    private final PresenceService presence;
    private final CurrentUserService currentUser;

    public MessageController(MessageService service, PresenceService presence, CurrentUserService currentUser) {
        this.service = service;
        this.presence = presence;
        this.currentUser = currentUser;
    }

    @GetMapping("/conversations")
    public List<ConversationDto> conversations() {
        return service.conversations();
    }

    @GetMapping("/conversations/{id}")
    public ThreadDto thread(@PathVariable Long id) {
        return service.thread(id);
    }

    @PostMapping("/conversations/{id}")
    public MessageDto send(@PathVariable Long id, @RequestBody SendRequest request) {
        return service.send(id, request.body());
    }

    @PostMapping("/start")
    public ConversationDto start(@RequestBody StartRequest request) {
        return service.start(request.kind(), request.id());
    }

    @GetMapping("/people")
    public List<PersonDto> people(@RequestParam(defaultValue = "") String q) {
        return service.people(q);
    }

    @PostMapping("/messages/{id}/report")
    public ResponseEntity<Void> report(@PathVariable Long id, @RequestBody ReportRequest request) {
        service.report(id, request.reason(), request.block());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/blocks/{accountId}")
    public ResponseEntity<Void> block(@PathVariable Long accountId) {
        service.block(accountId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/blocks/{accountId}")
    public ResponseEntity<Void> unblock(@PathVariable Long accountId) {
        service.unblock(accountId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", service.unreadCount());
    }

    // Green dots for lists of people. Everyone in the VSU app can see who is online.
    @GetMapping("/presence/students")
    public Map<Long, Boolean> studentPresence(@RequestParam(defaultValue = "") List<Long> ids) {
        return service.studentPresence(ids);
    }

    @GetMapping("/presence/accounts")
    public Map<Long, Boolean> accountPresence(@RequestParam(defaultValue = "") List<Long> ids) {
        return service.accountPresence(ids);
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
