package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class IndexController {

    @GetMapping("/index")
    public String index() {
        return "index";
    }
    @GetMapping("/source-management")
    public String sourceManagement() {
        return "source-management";
    }
}