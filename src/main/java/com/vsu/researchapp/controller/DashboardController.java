package com.vsu.researchapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        // This returns the "dashboard.html" view
        return "dashboard";
    }

    // Optional: send "/" to dashboard too
    @GetMapping("/")
    public String home() {
        return "dashboard";
    }
}
