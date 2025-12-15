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

   @GetMapping("/home")
public String homeAlias() {
    return "dashboard";
}

}
