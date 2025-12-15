package com.vsu.researchapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {


    // Just a preview page now, NOT used by security
    @GetMapping("/preview-login")
    public String login() {
        return "login";   // templates/login.html
    }
}