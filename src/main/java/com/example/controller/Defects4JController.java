package com.example.controller;

import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/defects4j")
public class Defects4JController {

    @GetMapping("/projects")
    public List<String> listProjects() {
        File dir = new File("defects4j/projects");
        if (!dir.exists()) return Collections.emptyList();
        return Arrays.asList(Objects.requireNonNull(dir.list((d, name) -> new File(d, name).isDirectory())));
    }

    @PostMapping("/load")
    public String loadProject(@RequestParam String projectName) {
        String path = "defects4j/projects/" + projectName;
        File pomFile = new File(path + "/pom.xml");
        return pomFile.exists() ? "项目加载成功：" + projectName : "未找到项目：" + projectName;
    }

    @PostMapping("/run")
    public String runTest(@RequestParam String projectName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "test");
            pb.directory(new File("defects4j/projects/" + projectName));
            Process proc = pb.start();
            proc.waitFor();
            return "测试执行完成：" + projectName;
        } catch (Exception e) {
            return "运行失败：" + e.getMessage();
        }
    }
}