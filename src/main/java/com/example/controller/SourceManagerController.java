package com.example.controller;

import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/source")
public class SourceManagerController {

    @GetMapping("/list")
    public List<String> listSources() {
        File dir = new File("sources");
        if (!dir.exists()) return Collections.emptyList();
        return Arrays.asList(Objects.requireNonNull(dir.list((d, name) -> name.endsWith(".java") || new File(d, name).isDirectory())));
    }

    @PostMapping("/run")
    public String runSource(@RequestParam String path) {
        try {
            ProcessBuilder pb = new ProcessBuilder("javac", path);
            pb.directory(new File("sources"));
            Process compile = pb.start();
            compile.waitFor();

            String className = new File(path).getName().replace(".java", "");
            pb = new ProcessBuilder("java", className);
            pb.directory(new File("sources"));
            Process exec = pb.start();
            Scanner sc = new Scanner(exec.getInputStream()).useDelimiter("\\A");
            return sc.hasNext() ? sc.next() : "无输出";
        } catch (Exception e) {
            return "运行失败：" + e.getMessage();
        }
    }
}