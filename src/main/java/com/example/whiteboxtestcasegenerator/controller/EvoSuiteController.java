package com.example.whiteboxtestcasegenerator.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class EvoSuiteController {

    private final String uploadDir = "uploaded-code";
    private final String outputDir = "generated-tests";

    @PostMapping("/generate-tests")
    public String generateTests(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 保存上传的文件
            Path uploadedPath = Paths.get(uploadDir);
            if (!Files.exists(uploadedPath)) {
                Files.createDirectories(uploadedPath);
            }

            Path uploadedFile = uploadedPath.resolve(file.getOriginalFilename());
            file.transferTo(uploadedFile);

            // 2. 使用 EvoSuite 生成测试用例
            String className = getClassNameFromJavaFile(uploadedFile.toFile());
            String command = String.format(
                    "java -jar evosuite.jar -class %s -projectCP %s -Doutput_directory=%s",
                    className,
                    uploadDir,
                    outputDir
            );

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            return "测试用例生成完成！";
        } catch (Exception e) {
            e.printStackTrace();
            return "生成失败：" + e.getMessage();
        }
    }

    @GetMapping("/testcases")
    public List<String> getGeneratedTestCases() throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) return Collections.emptyList();

        return Files.walk(dir.toPath())
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toString)
                .map(s -> s.replace("\\", "/")) // for Windows
                .collect(Collectors.toList());
    }

    private String getClassNameFromJavaFile(File file) throws IOException {
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
