package com.vsu.researchapp.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/home")
    public String homeAlias() {
        return "dashboard";
    }

    // ADD THIS
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
