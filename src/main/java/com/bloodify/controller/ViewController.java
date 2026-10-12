package com.bloodify.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/find-donors")
    public String publicDonors() {
        return "public-donors";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/staff/add-donor")
    public String addDonor(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("isAdmin", hasAdminRole(auth));
        return "add-donor";
    }

    @GetMapping("/staff/donors")
    public String staffDonors(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("isAdmin", hasAdminRole(auth));
        return "donors";
    }

    private static boolean hasAdminRole(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}