package com.example.controller;

import com.example.eval.JavaCoverageEvaluator;
import com.example.evosuite.EvoSuiteRunner;
import com.example.llm.LLMTestCaseGenerator;
import com.example.pojo.Code;
import com.example.pojo.TestResult;
import com.example.pojo.User;
import com.example.service.CodeService;
import com.example.service.TestResultService;
import com.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class TestResultController {
    @Autowired
    private TestResultService testResultService;

    @Autowired
    private UserService userService;
    @Autowired
    private CodeService codeService;



    @PostMapping("/evosuite2")
    public ResponseEntity<String> runEvo(@RequestParam String className, @RequestParam String cp) throws Exception {
        return ResponseEntity.ok(EvoSuiteRunner.generateTests(className, cp));
    }

    @PostMapping("/llm2")
    public ResponseEntity<String> runLLM(@RequestParam String method, @RequestParam String lang) throws Exception {
        return ResponseEntity.ok(LLMTestCaseGenerator.generateTest(method, lang));
    }

    @GetMapping("/java-coverage2")
    public ResponseEntity<Double> getJavaCoverage(@RequestParam String reportPath) throws Exception {
        return ResponseEntity.ok(JavaCoverageEvaluator.parseCoverageFromJacoco(reportPath));
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveTestResult(@RequestBody TestResult testResult) {
        testResultService.insert(testResult);
        return ResponseEntity.ok("{\"message\":\"测试结果已成功保存\"}");
    }

    @RequestMapping(value = "/exampleHistory")
    public String toExampleHistory(Model model, @RequestParam Integer userId, HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user",user);

        List<TestResult> testResultList = new ArrayList<>();
        List<Code> testCodeList = new ArrayList<>();
        testResultList = testResultService.findByUserId(userId);
        for (TestResult t : testResultList) {
            Code code = codeService.findCodeByCodeId(t.getCodeId());
            testCodeList.add(code);
        }
        model.addAttribute("testResultList",testResultList);
        model.addAttribute("testCodeList",testCodeList);
        return "exampleHistory";
    }

    @RequestMapping(value = "/exampleGeneration")
    public String toExampleGeneration(Model model, @RequestParam Integer userId,
                                      @RequestParam(required = false) Integer codeId,
                                      HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        // 生成唯一 sessionToken
        String sessionToken = UUID.randomUUID().toString();
        Map<String, Object> scopedData = new HashMap<>();
        session.setAttribute("sessionToken_" + sessionToken, scopedData);
        model.addAttribute("sessionToken", sessionToken); // 给页面使用

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user", user);

        if(codeId != null){
            Code code = codeService.findCodeByCodeId(codeId);
            if (code == null) {
                model.addAttribute("error", "代码未找到");
                return "error"; // 跳转到错误页面
            }
            model.addAttribute("code", code);

            String codeContent = "";
            try {
                Path path = Paths.get(code.getCodePath());
                byte[] fileContent = Files.readAllBytes(path);
                codeContent = new String(fileContent, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "无法读取代码文件内容");
            }
            model.addAttribute("codeContent", codeContent);
            String codeType = code.getCodeType();
            String codeName = code.getCodeName();
            System.out.println(codeContent);
            model.addAttribute("codeType", codeType);
            model.addAttribute("codeName", codeName);
        }
        return "exampleGeneration";
    }


    @RequestMapping(value = "/viewTestResult")
    public String viewDataSource(Model model, @RequestParam Integer userId, @RequestParam Integer testResultId,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user", user);

        TestResult testResult = testResultService.findByTestId(testResultId);
        model.addAttribute("testRecord", testResult);

        Code code = codeService.findCodeByCodeId(testResult.getCodeId());
        model.addAttribute("testCode", code);

        // 读取测试用例文件内容
        try {
            String exampleContent = readFileContent(testResult.getExamplePath());
            model.addAttribute("exampleContent", exampleContent);
        } catch (IOException e) {
            model.addAttribute("exampleContent", "文件读取失败: " + e.getMessage());
        }

        // 处理评价报告路径
        String judgementPath = processJacocoReportPath(testResult.getJudgement());
        model.addAttribute("judgementPath", judgementPath);
        System.out.println(judgementPath);

        return "viewTestResult";  // 测试记录显示页面
    }

    // 读取文件内容的辅助方法
    private String readFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    private String processJacocoReportPath(String originalPath) {
        if (originalPath == null) return "";

        // 替换反斜杠为正斜杠
        String normalizedPath = originalPath.replace("\\", "/");

        // 定义需要处理的关键字及其替换规则
        Map<String, String> keywordMap = new LinkedHashMap<>();
        keywordMap.put("apijacoco", "apijacoco");
        keywordMap.put("jacoco", "jacoco");
        keywordMap.put("coverage", "coverage");
        keywordMap.put("coverageapi", "coverageapi");

        // 查找第一个匹配的关键字
        String relativePath = normalizedPath;
        String matchedKeyword = null;

        for (String keyword : keywordMap.keySet()) {
            String searchPath = "/" + keyword + "/";
            int index = normalizedPath.indexOf(searchPath);
            if (index != -1) {
                matchedKeyword = keyword;
                relativePath = normalizedPath.substring(index + searchPath.length());
                break;
            }
        }

        // 构建最终路径
        if (matchedKeyword != null) {
            return "/" + keywordMap.get(matchedKeyword) + "/" + relativePath;
        } else {
            return normalizedPath;
        }
    }

    @RequestMapping(value = "/deleteTestResult")
    public String deleteDataSource(Model model, @RequestParam Integer userId, @RequestParam Integer testResultId,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }
        User user = userService.findUserByUserId(userId);
        model.addAttribute("user",user);
        if (testResultService.findByTestId(testResultId) != null) {
            testResultService.delete(testResultId);
        }
        List<TestResult> testResultList = new ArrayList<>();
        List<Code> testCodeList = new ArrayList<>();
        testResultList = testResultService.findByUserId(userId);
        for (TestResult t : testResultList) {
            Code code = codeService.findCodeByCodeId(t.getCodeId());
            testCodeList.add(code);
        }
        model.addAttribute("testResultList",testResultList);
        model.addAttribute("testCodeList",testCodeList);
        return "redirect:/exampleHistory?userId=" + userId;
    }

    @RequestMapping(value = "/testSelect")
    public String testSelect(Model model, @RequestParam Integer userId, @RequestParam String testKey,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }
        User user = userService.findUserByUserId(userId);
        model.addAttribute("user",user);

        List<TestResult> testResultList = new ArrayList<>();
        testResultList = testResultService.findTestByKey(testKey);
        List<Code> testCodeList = new ArrayList<>();
        for (TestResult t : testResultList) {
            Code code = codeService.findCodeByCodeId(t.getCodeId());
            testCodeList.add(code);
        }
        model.addAttribute("testResultList",testResultList);
        model.addAttribute("testCodeList",testCodeList);

        return "exampleHistory";  // 提示删除成功
    }
}
