package com.vsu.researchapp.presentation.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.vsu.researchapp.application.service.CalendarService;
import com.vsu.researchapp.application.service.CalendarService.EntryInput;
import com.vsu.researchapp.application.service.CalendarService.MyCalendar;
import com.vsu.researchapp.application.service.CalendarService.Upcoming;

// Under /api/calendar/**, which SecurityConfig limits to VSU roles (no partner schools).
// Editing is limited to your own calendar in CalendarService.
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService service;

    public CalendarController(CalendarService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public MyCalendar mine(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.mine(from, to);
    }

    @PostMapping("/me/entries")
    public MyCalendar create(@RequestBody EntryInput input) {
        return service.create(input);
    }

    @PutMapping("/me/entries/{id}")
    public MyCalendar update(@PathVariable Long id, @RequestBody EntryInput input) {
        return service.update(id, input);
    }

    @DeleteMapping("/me/entries/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/account/{accountId}/upcoming")
    public Upcoming upcomingForAccount(@PathVariable Long accountId) {
        return service.upcomingForAccount(accountId);
    }

    @GetMapping("/professor/{professorId}/upcoming")
    public Upcoming upcomingForProfessor(@PathVariable Long professorId) {
        return service.upcomingForProfessor(professorId);
    }
}
