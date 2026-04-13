package com.financetracker.fintrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "login"; // default page
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}