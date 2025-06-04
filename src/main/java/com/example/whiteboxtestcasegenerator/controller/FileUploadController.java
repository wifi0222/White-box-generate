package com.example.whiteboxtestcasegenerator.controller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class FileUploadController {

    // 直接定义质谱清言 API 的 URL
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    // 直接定义质谱清言 API 的认证密钥（如果有）
    private static final String API_KEY = "1ab053b0d4194225b25b2139ba6fcd68.98mhImT6Nke9ofce";


    @GetMapping("/deep")
    public String showUploadForm() {
        return "deep";
    }

    @PostMapping("/deep")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        if (!file.isEmpty()) {
            try {
                String fileContent = readFileContent(file);
                // 调用质谱清言 API
                String testCaseCode = callMassSpectrometryClearSpeechApi(fileContent);

                // 保存生成的测试用例到新的 Java 文件
                String fileName = file.getOriginalFilename().replace(".java", "Test.java");
                File testFile = createTestFile(fileName);
                try (FileOutputStream fos = new FileOutputStream(testFile)) {
                    fos.write(testCaseCode.getBytes(StandardCharsets.UTF_8));
                }

                model.addAttribute("message", "文件上传成功，测试用例已生成并保存。");
            } catch (IOException e) {
                model.addAttribute("message", "上传文件时发生错误: " + e.getMessage());
            } catch (Exception e) {
                model.addAttribute("message", "调用 API 或保存文件时发生错误: " + e.getMessage());
            }
        } else {
            model.addAttribute("message", "请选择一个文件上传。");
        }
        return "deep";
    }

    private String readFileContent(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private File createTestFile(String fileName) throws IOException {
        File testDir = new File("generated-tests");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        return new File(testDir, fileName);
    }

    private String callMassSpectrometryClearSpeechApi(String fileContent) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(API_URL);

        // 设置请求头，根据质谱清言 API 要求可能需要调整
        httpPost.setHeader("Authorization", "Bearer " + API_KEY);
        httpPost.setHeader("Content-Type", "application/json");

        // 构建提示信息和请求体，根据质谱清言 API 要求可能需要调整
        String prompt = "请为以下 Java 代码生成单元测试用例，要符合 JUnit 5 的语法规范。代码如下：";
        String input = prompt + fileContent;
        String requestBody = "{\"input\": \"" + Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8)) + "\"}";

        StringEntity entity = new StringEntity(requestBody);
        httpPost.setEntity(entity);

        // 发送请求
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity responseEntity = response.getEntity();
        return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
    }
}