package com.example.whiteboxtestcasegenerator.controller;


import com.example.whiteboxtestcasegenerator.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AIController {

    @Autowired
    private AIService aiService;

    @GetMapping("/callAI")
    public String index() {
        return "deep";
    }

    @PostMapping("/callAI")
    public String callAI(@RequestParam("input") String input, Model model) {
        String response = aiService.callAI(input);
        model.addAttribute("response", response);
        return "deep";
    }
}