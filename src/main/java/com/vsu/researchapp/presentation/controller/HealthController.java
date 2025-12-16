package com.vsu.researchapp.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        Map<String, String> body = new HashMap<>();
        body.put("status", "ok");
        body.put("service", "researchapp");
        return body;
    }
}
