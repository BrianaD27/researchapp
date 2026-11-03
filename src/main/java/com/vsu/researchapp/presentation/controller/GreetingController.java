package com.vsu.researchapp.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.service.GreetingService;
import com.vsu.researchapp.application.service.GreetingService.Greeting;

// "Good morning, ___" for the signed-in person. Only your own name.
@RestController
public class GreetingController {

    public record UpdateRequest(String preferredName) {}

    private final GreetingService service;

    public GreetingController(GreetingService service) {
        this.service = service;
    }

    @GetMapping("/api/me/greeting")
    public Greeting mine() {
        return service.mine();
    }

    @PutMapping("/api/me/greeting")
    public Greeting update(@RequestBody UpdateRequest request) {
        return service.update(request.preferredName());
    }
}
