package com.example.controller;

import com.example.evosuite.EvoSuiteRunner;
import com.example.llm.LLMTestCaseGenerator;
import com.example.eval.JavaCoverageEvaluator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestGenController {

    @PostMapping("/evosuite")
    public ResponseEntity<String> runEvo(@RequestParam String className, @RequestParam String cp) throws Exception {
        return ResponseEntity.ok(EvoSuiteRunner.generateTests(className, cp));
    }

    @PostMapping("/llm")
    public ResponseEntity<String> runLLM(@RequestParam String method, @RequestParam String lang) throws Exception {
        return ResponseEntity.ok(LLMTestCaseGenerator.generateTest(method, lang));
    }

    @GetMapping("/java-coverage")
    public ResponseEntity<Double> getJavaCoverage(@RequestParam String reportPath) throws Exception {
        return ResponseEntity.ok(JavaCoverageEvaluator.parseCoverageFromJacoco(reportPath));
    }
}