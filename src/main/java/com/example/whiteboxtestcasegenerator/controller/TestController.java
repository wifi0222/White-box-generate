package com.example.whiteboxtestcasegenerator.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.pojo.Code;
import com.example.pojo.TestResult;
import com.example.pojo.User;
import com.example.service.CodeService;
import com.example.service.TestResultService;
import com.example.service.UserService;

import com.example.util.SessionScopedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import java.io.BufferedReader;
import java.util.stream.Stream;

@Controller
public class TestController {
    private static final int COMMAND_TIMEOUT_SECONDS = 600; // 命令执行超时时间，单位：秒
    @Autowired
    private CodeService codeService;
    @Autowired
    private UserService userService;
    @Autowired
    private TestResultService testResultService;

    private Integer userID;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/test/upload")
    @ResponseBody
    public Map<String, String> uploadCode(@RequestParam("file") MultipartFile file,
                                          @RequestParam("language") String language, @RequestParam Integer userId,
                                          @RequestParam("sessionToken") String sessionToken,
                                          HttpSession session, Model model) {
        Map<String, String> response = new HashMap<>();
        System.out.println("检测文件");
        userID=userId;

        try {
            Map<String, Object> scopedSession = SessionScopedUtil.getScopedSession(session, sessionToken);

            // 确定子目录路径
            String subDir = language.equals("java") ? "uploaded-code/java" : "uploaded-code/python";
            Path uploadPath = Paths.get(subDir).toAbsolutePath();
            System.out.println(uploadPath);

            // 如果目录不存在，则创建目录
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("创建目录：" + uploadPath);
            }

            // 构建目标文件路径
            Path dest = uploadPath.resolve(file.getOriginalFilename());
            System.out.println("目标文件路径：" + dest);

            // 保存上传的文件
            file.transferTo(dest.toFile());
            System.out.println("文件保存成功");

            // 检查文件是否已成功保存
            if (Files.exists(dest)) {

                scopedSession.put("uploadedFilePath", dest.toString());
                scopedSession.put("language", language);
                response.put("message", "上传成功：" + file.getOriginalFilename());
                response.put("sessionToken", sessionToken);
                System.out.println(sessionToken);
                Code c=new Code();
                c.setCodeName(file.getOriginalFilename());
                c.setCodePath(dest.toString());
                c.setVersion(1.0F);
                String type = "";
                int lastIndex = file.getOriginalFilename().lastIndexOf('.');
                if (lastIndex != -1 && lastIndex < file.getOriginalFilename().length() - 1) {
                    type = file.getOriginalFilename().substring(lastIndex);
                }

                c.setCodeType(type);
                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String time = currentDateTime.format(formatter);
                c.setUpdateTime(time);
                //c.setCodeInfo(codeInfo);
                c.setUserId(userId);
                if(codeService.findCodeByPath(c.getCodePath())==null)
                    codeService.insertCode(c);
                else{
                    codeService.updateCode(c);
                }
                User user=userService.findUserByUserId(userId);
                model.addAttribute("user",user);

            } else {
                response.put("message", "上传失败：文件未成功保存！");
            }

        } catch (IOException e) {
            System.out.println("上传失败：" + e.getMessage());
            response.put("message", "上传失败：" + e.getMessage());
        }

        return response;
    }

    // 处理通过ID加载的代码内容上传
    @PostMapping("/test/uploadContent")
    @ResponseBody
    public Map<String, String> uploadCodeContent(
            @RequestParam("codeContent") String codeContent,
            @RequestParam("codeName") String codeName,
            @RequestParam("codeType") String codeType,
            @RequestParam("sessionToken") String sessionToken,
            @RequestParam Integer userId,
            HttpSession session, Model model) {

        Map<String, String> response = new HashMap<>();
        userID=userId;

        try {
            Map<String, Object> scopedSession = SessionScopedUtil.getScopedSession(session, sessionToken);
            // 确定子目录路径
            String subDir = codeType.equals(".java") ? "uploaded-code/java" : "uploaded-code/python";
            Path uploadPath = Paths.get(subDir).toAbsolutePath();

            // 如果目录不存在，则创建目录
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 构建目标文件路径
            Path dest = uploadPath.resolve(codeName);

            // 保存代码内容到文件
            try (BufferedWriter writer = Files.newBufferedWriter(dest, StandardCharsets.UTF_8)) {
                writer.write(codeContent);
            }

            scopedSession.put("uploadedFilePath", dest.toString());
            scopedSession.put("language", codeType.equals(".java") ? "java" : "python");
            response.put("message", "上传成功：" + codeName);
            response.put("sessionToken", sessionToken);

            // 更新或插入代码到数据库
            Code c = codeService.findByName(codeName);
            if (c == null) {
                c = new Code();
                c.setCodeName(codeName);
            }

            c.setCodePath(dest.toString());
            c.setCodeType(codeType);
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String time = currentDateTime.format(formatter);
            c.setUpdateTime(time);
            c.setUserId(userId);
            if (c.getCodeId() == null) {
                codeService.insertCode(c);
            } else {
                codeService.updateCode(c);
            }

            User user = userService.findUserByUserId(userId);
            model.addAttribute("user", user);

        } catch (IOException e) {
            System.out.println("上传失败：" + e.getMessage());
            response.put("message", "上传失败：" + e.getMessage());
        }

        return response;
    }

//    @PostMapping("/test/upload")
//    public String uploadCode(@RequestParam("file") MultipartFile file,
//                             @RequestParam("language") String language,
//                             HttpSession session,
//                             RedirectAttributes redirectAttributes) {
//        System.out.println("检测文件");
//
//        try {
//            // 确定子目录路径
//            String subDir = language.equals("java") ? "uploaded-code/java" : "uploaded-code/python";
//            Path uploadPath = Paths.get(subDir).toAbsolutePath();
//            System.out.println(uploadPath);
//
//            System.out.println("检测文件1");
//            // 如果目录不存在，则创建目录
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//                System.out.println("检测文件2");
//            }
//
//            System.out.println(file.getOriginalFilename());
//            System.out.println(uploadPath);           // 构建目标文件路径
//            Path dest = uploadPath.resolve(file.getOriginalFilename());
//            System.out.println(dest);
//            System.out.println("检测文件7");
//            // 保存上传的文件
//            file.transferTo(dest.toFile());
//            System.out.println("检测文件3");
//            // 检查文件是否已成功保存
//            if (Files.exists(dest)) {
//                session.setAttribute("message", "上传成功：" + file.getOriginalFilename());
//
//                session.setAttribute("uploadedFilePath", dest.toString());
//                session.setAttribute("language", language);
//                System.out.println("检测文件4");
//            } else {
//                session.setAttribute("message", "上传失败：文件未成功保存！");
//                System.out.println("检测文件6");
//            }
//
//        } catch (IOException e) {
//            System.out.println("检测文件8");
//            System.out.println(e.getMessage());
//            redirectAttributes.addFlashAttribute("message", "上传失败：" + e.getMessage());
//        }
//        System.out.println("检测文件5");
//        return "redirect:/";
//    }



    //    public Map<String, String> generateEvoSuite(HttpSession session, HttpServletResponse response) {
//        Map<String, String> result = new HashMap<>();
//        String filePath = (String) session.getAttribute("uploadedFilePath");
//        System.out.println(filePath);
//        String language = (String) session.getAttribute("language");
//
//        if (filePath == null || !"java".equals(language)) {
//            response.setStatus(400);
//            result.put("message", "请上传 Java 文件后再生成！");
//            return result;
//        }
//
//        try {
//            // 确保 generated-tests 目录存在
//            Path outputDir = Paths.get("generated-tests").toAbsolutePath();
//            System.out.println(outputDir);
//            if (!Files.exists(outputDir)) {
//                Files.createDirectories(outputDir);
//            }
//
//            // 提取类名
//            String className = Paths.get(filePath).getFileName().toString().replace(".java", "");
//            System.out.println(className);
//            // 构建命令
//            String command = String.format(
//                    "java -jar D:\\evosuite\\evosuite-1.2.0.jar -class %s -projectCP uploaded-code/class",
//                    className);
//            System.out.println(command);
//
//            String currentDirectory = System.getProperty("user.dir");
//            System.out.println("当前所在目录: " + currentDirectory);
//            // 执行命令
//            Process process = Runtime.getRuntime().exec(command);
//            int exitCode = process.waitFor();
//
//            // 读取命令输出
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            StringBuilder output = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//
//            if (exitCode == 0) {
//                result.put("message", "Java 测试用例生成成功！");
//            } else {
//                response.setStatus(500);
//                result.put("message", "生成失败，EvoSuite 返回错误代码：" + exitCode + "\n输出信息：\n" + output.toString());
//            }
//        } catch (Exception e) {
//            response.setStatus(500);
//            result.put("message", "生成失败：" + e.getMessage());
//        }
//        return result;
//    }
    @PostMapping("/api/generateEvoSuiteTest")
    @ResponseBody
    public Map<String, String> generateEvoSuite(@RequestParam("sessionToken") String sessionToken,HttpSession session, HttpServletResponse response,Model model) {
        Map<String, Object> scopedSession = SessionScopedUtil.getScopedSession(session, sessionToken);
        Map<String, String> result = new HashMap<>();
        String filePath = (String) scopedSession.get("uploadedFilePath");
        String language = (String) scopedSession.get("language");
        System.out.println("上传文件路径: " + filePath);

        if (filePath == null || !"java".equals(language)) {
            response.setStatus(400);
            result.put("message", "请上传 Java 文件后再生成！");
            return result;
        }

        try {

            // 提取类名
            String className = Paths.get(filePath).getFileName().toString().replace(".java", "");
            System.out.println("提取的类名: " + className);

            // 读取 Java 文件内容，提取包名
            String packageName = extractPackageName(filePath);
            String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;
            System.out.println("完整的类名: " + fullClassName);

            // 构建输出目录路径
            Path outputBaseDir = Paths.get("evosuite").toAbsolutePath();
            Path outputDir = outputBaseDir.resolve(className);

            // 确保 evosuite 目录存在
            if (!Files.exists(outputBaseDir)) {
                Files.createDirectories(outputBaseDir);
            }

            // 确保 evosuite/类名 目录存在
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            String javacCommand = String.format(
                    "javac -d uploaded-code/class uploaded-code/java/%s.java",
                    className);
            System.out.println(javacCommand);
            Process process1 = new ProcessBuilder(javacCommand.split(" ")).start();
            int exitCode1 = process1.waitFor();
            if (exitCode1 == 0) {
                System.out.println("编译成功");
            } else {
                System.err.println("编译失败，退出码: " + exitCode1);
                response.setStatus(500);
                result.put("message", "编译失败，退出码: " + exitCode1);
                return result;
            }

            // 构建命令
            String command = String.format(
                    "java -jar src/main/resources/static/evosuite/evosuite-1.2.0.jar -class %s -projectCP uploaded-code/class -D report_dir=%s -D test_dir=%s",
                    fullClassName, outputDir, outputDir);
            System.out.println("执行的命令: " + command);

            String currentDirectory = System.getProperty("user.dir");
            System.out.println("当前所在目录: " + currentDirectory);

            // 使用 ProcessBuilder 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            Process process = processBuilder.start();

            // 异步处理标准输出
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> outputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> errorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> exitCodeFuture = executor.submit(() -> {
                try {
                    return process.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });
            int exitCode;
            try {
                exitCode = exitCodeFuture.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                process.destroyForcibly();
                response.setStatus(500);
                result.put("message", "生成失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String output = outputFuture.get();
            String errorOutput = errorFuture.get();

            executor.shutdown();

            if (exitCode == 0) {
                result.put("message", "Java 测试用例生成成功！");
                scopedSession.put("generatedTestDir", outputDir.toString());
                // 读取生成的测试用例文件内容
                Path estestFilePath = findESTestFilePath(Paths.get(outputDir.toString()), className);
                String testCaseCode = readTestCaseFile(estestFilePath);
                System.out.println(testCaseCode);
                result.put("testCaseCode", testCaseCode);
                result.put("sessionToken", sessionToken);

                TestResult r=new TestResult();
                String codePath=filePath;

                Path estestpath = estestFilePath;
                r.setExamplePath(estestpath.toString());
                Code c=codeService.findCodeByPath(codePath);
                if(c!=null){
                    r.setCodeId(c.getCodeId());
                }
                r.setUserId(userID);
                r.setTestMethod("EvoSuite");

                if(testResultService.findByExamplePath(r.getExamplePath())==null){
                    testResultService.insert(r);
                }
                else{
                    testResultService.update(r);
                }


            } else {
                response.setStatus(500);
                result.put("message", "生成失败，EvoSuite 返回错误代码：" + exitCode + "\n输出信息：\n" + output + "\n错误信息：\n" + errorOutput);
            }
        } catch (IOException e) {
            response.setStatus(500);
            result.put("message", "生成失败：IO 异常 - " + e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            response.setStatus(500);
            result.put("message", "生成失败：线程执行异常 - " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/test-case-results")
    @ResponseBody
    public String showResults() throws IOException {
        Path base = Paths.get("generated-tests");
        StringBuilder sb = new StringBuilder("<h2>生成结果</h2>");
        if (!Files.exists(base)) return "<p>暂无生成内容</p>";
        Files.walk(base)
                .filter(Files::isRegularFile)
                .forEach(p -> sb.append("<p>").append(p.getFileName()).append("</p>"));
        return sb.toString();
    }

    @PostMapping("/api/performJacocoEvaluation")
    @ResponseBody
    public Map<String, String> performJacocoEvaluation(@RequestParam("sessionToken") String sessionToken,HttpSession session, HttpServletResponse response) {
        Map<String, Object> scopedSession = SessionScopedUtil.getScopedSession(session, sessionToken);
        Map<String, String> result = new HashMap<>();
        String filePath = (String) scopedSession.get("uploadedFilePath");
        System.out.println(filePath);
        String language = (String) scopedSession.get("language");
        String generatedTestDir = (String) scopedSession.get("generatedTestDir");
        System.out.println(generatedTestDir);
        if (filePath == null ||!"java".equals(language) || generatedTestDir == null) {
            response.setStatus(400);
            result.put("message", "请上传 Java 文件并成功生成测试用例后再进行测评！");
            return result;
        }


        try {
            // 提取类名
            String className = Paths.get(filePath).getFileName().toString().replace(".java", "");

            // 读取 Java 文件内容，提取包名
            String packageName = extractPackageName(filePath);
            String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;
            System.out.println("完整的类名: " + fullClassName);

            // 构建 JaCoCo 输出目录路径
            Path jacocoBaseDir = Paths.get("jacoco").toAbsolutePath();
            Path jacocoOutputDir = jacocoBaseDir.resolve(className);

            // 确保 jacoco 目录存在
            if (!Files.exists(jacocoBaseDir)) {
                Files.createDirectories(jacocoBaseDir);
            }

            // 确保 jacoco/类名 目录存在
            if (!Files.exists(jacocoOutputDir)) {
                Files.createDirectories(jacocoOutputDir);
            }

            // JaCoCo 代理 JAR 文件路径
            String jacocoAgentPath = "src/main/resources/static/jacoco-0.8.13/lib/jacocoagent.jar";
            // JaCoCo 执行文件路径
            String jacocoExecFilePath = jacocoOutputDir.resolve("jacoco.exec").toString();
            // JaCoCo CLI JAR 文件路径
            String jacocoCliJarPath = "src/main/resources/static/jacoco-0.8.13/lib/jacococli.jar";
            // 项目类路径
            String projectClassPath = "uploaded-code/class";
            // 项目源文件路径
            String sourceFilePath = "uploaded-code/java";
            System.out.println(jacocoExecFilePath);

            // 动态获取 _ESTest.java 文件路径
            Path estestFilePath = findESTestFilePath(Paths.get(generatedTestDir), className);
            System.out.println(estestFilePath);
            try {
                // 读取文件内容并过滤
                StringBuilder filteredContent = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(estestFilePath)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("@RunWith")) {
                            filteredContent.append(line).append(System.lineSeparator());
                        }
                    }
                }

                // 将过滤后的内容写入文件
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(estestFilePath)))) {
                    writer.write(filteredContent.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 动态获取 _ESTest_scaffolding.java 文件路径
            Path estestScaffoldingFilePath = findESTestScaffoldingFilePath(Paths.get(generatedTestDir), className);

            // 编译 ESTest_scaffolding
            String javaCommand = String.format(
                    "javac -cp \".;src/main/resources/static/junit/junit-4.13.2.jar;src/main/resources/static/evosuite/evosuite-standalone-runtime-1.2.0.jar;uploaded-code\\class\" -d evosuite\\%s %s",
                    className, estestScaffoldingFilePath);
            Process process1 = new ProcessBuilder(javaCommand.split(" ")).start();
            int exitCode1 = process1.waitFor();
            if (exitCode1 == 0) {
                System.out.println("ESTest_scaffolding成功");
            } else {
                System.err.println("ESTest_scaffolding失败，退出码: " + exitCode1);
                response.setStatus(500);
                result.put("message", "ESTest_scaffolding 编译失败，退出码: " + exitCode1);
                return result;
            }

            // 编译 ESTest
            String java2Command = String.format(
                    "javac -cp \".;src/main/resources/static/junit/junit-4.13.2.jar;src/main/resources/static/evosuite/evosuite-standalone-runtime-1.2.0.jar;evosuite\\%s;uploaded-code\\class\" -d evosuite\\%s %s",
                    className, className, estestFilePath);
            System.out.println(java2Command);
            Process process2 = new ProcessBuilder(java2Command.split(" ")).start();
            int exitCode2 = process2.waitFor();
            if (exitCode2 == 0) {
                System.out.println("ESTest成功");
            } else {
                System.err.println("ESTest失败，退出码: " + exitCode2);
                response.setStatus(500);
                result.put("message", "ESTest 编译失败，退出码: " + exitCode2);
                return result;
            }

            // 构建运行测试用例并收集覆盖率数据的命令
            // 使用完整类名
            String fullESTestClassName = packageName.isEmpty() ? className + "_ESTest" : packageName + "." + className + "_ESTest";
            String runTestCommand = String.format(
                    "java -javaagent:%s=destfile=%s -cp %s;%s;src/main/resources/static/junit/junit-4.13.2.jar;src/main/resources/static/evosuite/evosuite-standalone-runtime-1.2.0.jar;src/main/resources/static/hamcrest/hamcrest-core-1.3.jar org.junit.runner.JUnitCore %s",
                    jacocoAgentPath, jacocoExecFilePath, projectClassPath, generatedTestDir, fullESTestClassName);
            System.out.println("执行的运行测试用例命令: " + runTestCommand);

            // 使用 ProcessBuilder 执行命令
            ProcessBuilder runTestProcessBuilder = new ProcessBuilder(runTestCommand.split(" "));
            Process runTestProcess = runTestProcessBuilder.start();

            // 异步处理标准输出
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> runTestOutputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runTestProcess.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> runTestErrorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runTestProcess.getErrorStream()))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> runTestExitCodeFuture = executor.submit(() -> {
                try {
                    return runTestProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });
            int runTestExitCode;
            try {
                runTestExitCode = runTestExitCodeFuture.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                runTestProcess.destroyForcibly();
                response.setStatus(500);
                result.put("message", "运行测试用例失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String runTestOutput = runTestOutputFuture.get();
            String runTestErrorOutput = runTestErrorFuture.get();

//            if (runTestExitCode != 0) {
//                response.setStatus(500);
//                result.put("message", "运行测试用例失败，返回错误代码：" + runTestExitCode + "\n输出信息：\n" + runTestOutput + "\n错误信息：\n" + runTestErrorOutput);
//                System.out.println(runTestOutput);
//                System.out.println(runTestErrorOutput);
//                executor.shutdown();
//                return result;
//            }
            if (runTestExitCode != 0) {
                // 只记录错误信息，不提前返回
                result.put("testExecutionError", "运行测试用例失败，返回错误代码：" + runTestExitCode + "\n输出信息：\n" + runTestOutput + "\n错误信息：\n" + runTestErrorOutput);
                System.out.println(runTestOutput);
                System.out.println(runTestErrorOutput);
            }


            // 构建 JaCoCo 报告生成命令，添加 --includes 参数
            String jacocoReportCommand = String.format(
                    "java -jar %s report %s --classfiles %s --sourcefiles %s --html %s",
                    jacocoCliJarPath, jacocoExecFilePath, projectClassPath, sourceFilePath, jacocoOutputDir);
            System.out.println("执行的 JaCoCo 报告生成命令: " + jacocoReportCommand);

            // 使用 ProcessBuilder 执行命令
            ProcessBuilder jacocoReportProcessBuilder = new ProcessBuilder(jacocoReportCommand.split(" "));
            Process jacocoReportProcess = jacocoReportProcessBuilder.start();

            // 异步处理标准输出
            Future<String> jacocoReportOutputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(jacocoReportProcess.getInputStream(), "UTF-8"))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> jacocoReportErrorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(jacocoReportProcess.getErrorStream(), "UTF-8"))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> jacocoReportExitCodeFuture = executor.submit(() -> {
                try {
                    return jacocoReportProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });
            int jacocoReportExitCode;
            try {
                jacocoReportExitCode = jacocoReportExitCodeFuture.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                jacocoReportProcess.destroyForcibly();
                response.setStatus(500);
                result.put("message", "生成 JaCoCo 报告失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String jacocoReportOutput = jacocoReportOutputFuture.get();
            String jacocoReportErrorOutput = jacocoReportErrorFuture.get();

            executor.shutdown();

            Path htmlFilePath = findHtmlFile(jacocoOutputDir, className + ".html");

            if (jacocoReportExitCode == 0) {
                // 创建线程池（建议使用单例或Spring管理）
                ExecutorService executor2 = Executors.newFixedThreadPool(2);

                // 提交第一个任务
                CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                    try {
                        String codePath=filePath;

                        Code c=codeService.findCodeByPath(codePath);
                        // 获取测试结果列表
                        List<TestResult> results = testResultService.findByCodeIdandMethodandUserId(c.getCodeId(),"EvoSuite",userID);
                        TestResult r = results.get(0);
                        r.setCodeId(c.getCodeId());

                        LocalDateTime currentDateTime = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String time = currentDateTime.format(formatter);
                        r.setTestTime(time);
                        r.setJudgement(htmlFilePath.toString());
                        testResultService.update(r);
                    } catch (Exception e) {
                        System.err.println("Error in task 1: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, executor2);

                // 提交第二个任务
                CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
                    try {
                        result.put("message", "JaCoCo 测评成功！");
                        result.put("jacocoReportPath", htmlFilePath.toString());
                        result.put("sessionToken", sessionToken);
                    } catch (Exception e) {
                        System.err.println("Error in task 2: " + e.getMessage());
                    }
                }, executor2);

                // 等待所有任务完成
                CompletableFuture.allOf(task1, task2).join();
                executor2.shutdown();
                try {
                    if (!executor2.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor2.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor2.shutdownNow();
                }
            }else {
                response.setStatus(500);
                result.put("message", "生成 JaCoCo 报告失败，返回错误代码：" + jacocoReportExitCode + "\n输出信息：\n" + jacocoReportOutput + "\n错误信息：\n" + jacocoReportErrorOutput);
            }
        } catch (IOException e) {
            response.setStatus(500);
            result.put("message", "测评失败：IO 异常 - " + e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            response.setStatus(500);
            result.put("message", "测评失败：线程执行异常 - " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/api/generatePynguinTest")
    @ResponseBody
    public Map<String, String> generatePynguinTest(@RequestParam("sessionToken") String sessionToken,HttpSession session, HttpServletResponse response) {
        Map<String, Object> scopedSession = SessionScopedUtil.getScopedSession(session, sessionToken);
        Map<String, String> result = new HashMap<>();
        String filePath = (String) scopedSession.get("uploadedFilePath");
        String language = (String) scopedSession.get("language");
        System.out.println(filePath);

        if (filePath == null || !"python".equals(language)) {
            response.setStatus(400);
            result.put("message", "请上传 Python 文件后再生成！");
            return result;
        }

        try {
            // 提取文件名（不包含扩展名）
            String moduleName = Paths.get(filePath).getFileName().toString().replace(".py", "");

            // 构建输出目录路径
            Path outputPath = Paths.get("pynguin", moduleName);
            // 确保输出目录存在
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            // 构建 pynguin 命令，在激活环境后设置环境变量
            String command = String.format(
                    "cmd.exe /c \"C:\\Users\\Wangfei\\anaconda3\\Scripts\\activate py310_env && set PYNGUIN_DANGER_AWARE=true && pynguin --project-path uploaded-code/python --module_name %s --output-path %s --report-dir=%s\"",
                    moduleName, outputPath.toString(), outputPath.toString());
            System.out.println(command);

            // 使用 ProcessBuilder 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = processBuilder.start();

            // 异步处理标准输出
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> outputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> errorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> exitCodeFuture = executor.submit(() -> {
                try {
                    return process.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });

            int exitCode;
            try {
                exitCode = exitCodeFuture.get(600, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                process.destroyForcibly();
                response.setStatus(500);
                result.put("message", "生成失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String output = outputFuture.get();
            String errorOutput = errorFuture.get();

            executor.shutdown();

            if (exitCode == 0) {
                result.put("message", "Python 测试用例生成成功！");
                // 读取生成的测试用例文件内容
                String testCaseCode = readTestCaseFile(outputPath, "test_" + moduleName + ".py");
                result.put("testCaseCode", testCaseCode);
                System.out.println(testCaseCode);
                result.put("sessionToken", sessionToken);

                TestResult r=new TestResult();
                String codePath=filePath;

                Path pytestPath = Paths.get("", String.valueOf(outputPath), "test_" + moduleName + ".py").toAbsolutePath();
                r.setExamplePath(pytestPath.toString());
                Code c=codeService.findCodeByPath(codePath);
                if(c!=null){
                    r.setCodeId(c.getCodeId());
                }
                r.setUserId(userID);
                r.setTestMethod("Pynguin");

                if(testResultService.findByExamplePath(r.getExamplePath())==null){
                    testResultService.insert(r);
                }
                else{
                    testResultService.update(r);
                }
            }
            else {
                response.setStatus(500);
                result.put("message", "生成失败，返回错误代码：" + exitCode + "\n输出信息：\n" + output + "\n错误信息：\n" + errorOutput);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            response.setStatus(500);
            result.put("message", "生成失败：" + e.getMessage());
        }
        return result;
    }
    @PostMapping("/api/performPythonCoverageEvaluation")
    @ResponseBody
    public Map<String, String> performPythonCoverageEvaluation(@RequestParam("sessionToken") String sessionToken,HttpSession session, HttpServletResponse response) {
        Map<String, Object> scopedSession = SessionScopedUtil.getScopedSession(session, sessionToken);
        Map<String, String> result = new HashMap<>();
        String filePath = (String) scopedSession.get("uploadedFilePath");
        String language = (String) scopedSession.get("language");

        if (filePath == null || !"python".equals(language)) {
            response.setStatus(400);
            result.put("message", "请上传 Python 文件并成功生成测试用例后再进行测评！");
            return result;
        }

        try {
            // 提取文件名（不包含扩展名）
            String moduleName = Paths.get(filePath).getFileName().toString().replace(".py", "");

            // 构建输出目录路径
            Path coverageOutputPath = Paths.get("coverage", moduleName);
            // 确保输出目录存在
            if (!Files.exists(coverageOutputPath)) {
                Files.createDirectories(coverageOutputPath);
            }

            // 设置 PYTHONPATH 环境变量
            String pythonPath = "uploaded-code/python";
            String envPath = System.getenv("PYTHONPATH");
            if (envPath != null && !envPath.isEmpty()) {
                pythonPath = envPath + File.pathSeparator + pythonPath;
            }

            // 构建 coverage run 命令
            String coverageRunCommand = String.format(
                    "cmd.exe /c \"C:\\Users\\Wangfei\\anaconda3\\Scripts\\activate py310_env && set PYTHONPATH=%s && coverage run -m pytest pynguin/%s/test_%s.py\"",
                    pythonPath, moduleName, moduleName);
            System.out.println(coverageRunCommand);

            // 使用 ProcessBuilder 执行 coverage run 命令
            ProcessBuilder coverageRunProcessBuilder = new ProcessBuilder("cmd.exe", "/c", coverageRunCommand);
            Process coverageRunProcess = coverageRunProcessBuilder.start();

            // 异步处理标准输出
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> coverageRunOutputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(coverageRunProcess.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> coverageRunErrorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(coverageRunProcess.getErrorStream()))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> coverageRunExitCodeFuture = executor.submit(() -> {
                try {
                    return coverageRunProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });

            int coverageRunExitCode;
            try {
                coverageRunExitCode = coverageRunExitCodeFuture.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                coverageRunProcess.destroyForcibly();
                response.setStatus(500);
                result.put("message", "运行 coverage run 失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String coverageRunOutput = coverageRunOutputFuture.get();
            String coverageRunErrorOutput = coverageRunErrorFuture.get();

            if (coverageRunExitCode != 0) {
                response.setStatus(500);
                result.put("message", "运行 coverage run 失败，返回错误代码：" + coverageRunExitCode + "\n输出信息：\n" + coverageRunOutput + "\n错误信息：\n" + coverageRunErrorOutput);
                executor.shutdown();
                return result;
            }

            // 构建 coverage html 命令
            String coverageHtmlCommand = String.format(
                    "cmd.exe /c \"C:\\Users\\Wangfei\\anaconda3\\Scripts\\activate py310_env && coverage html --directory=%s\"",
                    coverageOutputPath.toString());

            // 使用 ProcessBuilder 执行 coverage html 命令
            ProcessBuilder coverageHtmlProcessBuilder = new ProcessBuilder("cmd.exe", "/c", coverageHtmlCommand);
            Process coverageHtmlProcess = coverageHtmlProcessBuilder.start();

            // 异步处理标准输出
            Future<String> coverageHtmlOutputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(coverageHtmlProcess.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> coverageHtmlErrorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(coverageHtmlProcess.getErrorStream()))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> coverageHtmlExitCodeFuture = executor.submit(() -> {
                try {
                    return coverageHtmlProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });

            int coverageHtmlExitCode;
            try {
                coverageHtmlExitCode = coverageHtmlExitCodeFuture.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                coverageHtmlProcess.destroyForcibly();
                response.setStatus(500);
                result.put("message", "运行 coverage html 失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String coverageHtmlOutput = coverageHtmlOutputFuture.get();
            String coverageHtmlErrorOutput = coverageHtmlErrorFuture.get();

            executor.shutdown();

            Path htmlPath = coverageOutputPath.resolve("index.html");
            Path htmlFilePath = Paths.get(System.getProperty("user.dir"), String.valueOf(htmlPath));

            if (coverageHtmlExitCode == 0) {
                // 创建线程池（建议使用单例或Spring管理）
                ExecutorService executor2 = Executors.newFixedThreadPool(2);

                // 提交第一个任务
                CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                    try {
                        String codePath=filePath;
                        Path outputPath = Paths.get(System.getProperty("user.dir"), "pynguin");

                        Code c=codeService.findCodeByPath(codePath);
                        // 获取测试结果列表
                        List<TestResult> results = testResultService.findByCodeIdandMethodandUserId(c.getCodeId(),"Pynguin",userID);
                        TestResult r = results.get(0);
                        r.setCodeId(c.getCodeId());

                        LocalDateTime currentDateTime = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String time = currentDateTime.format(formatter);
                        r.setTestTime(time);
                        r.setJudgement(htmlFilePath.toString());
                        testResultService.update(r);
                    } catch (Exception e) {
                        System.err.println("Error in task 1: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, executor2);

                // 提交第二个任务
                CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
                    try {
                        result.put("message", "Python 覆盖率测评成功！");
                        result.put("coverageReportPath", htmlFilePath.toString());
                        result.put("sessionToken", sessionToken);
                    } catch (Exception e) {
                        System.err.println("Error in task 2: " + e.getMessage());
                    }
                }, executor2);

                // 等待所有任务完成
                CompletableFuture.allOf(task1, task2).join();
                executor2.shutdown();
                try {
                    if (!executor2.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor2.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor2.shutdownNow();
                }
            } else {
                response.setStatus(500);
                result.put("message", "运行 coverage html 失败，返回错误代码：" + coverageHtmlExitCode + "\n输出信息：\n" + coverageHtmlOutput + "\n错误信息：\n" + coverageHtmlErrorOutput);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            response.setStatus(500);
            result.put("message", "测评失败：" + e.getMessage());
        }
        return result;
    }
    private static String extractPackageName(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("package")) {
                    int endIndex = line.indexOf(';');
                    if (endIndex != -1) {
                        return line.substring(8, endIndex).trim();
                    }
                }
            }
        }
        return "";
    }
    private Path findESTestFilePath(Path evosuiteDir, String className) {
        if (Files.exists(evosuiteDir) && evosuiteDir.toFile().isDirectory()) {
            return findESTestFilePathRecursive(evosuiteDir.toFile(), className);
        }
        throw new RuntimeException("找不到 _ESTest.java 文件，目录不存在或不是目录");
    }

    private Path findESTestFilePathRecursive(File directory, String className) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归调用查找子目录
                    Path result = findESTestFilePathRecursive(file, className);
                    if (result != null) {
                        return result;
                    }
                } else if (file.getName().equals(className + "_ESTest.java")) {
                    System.out.println(file.toPath());
                    return file.toPath();
                }
            }
        }
        return null;
    }

    private Path findESTestScaffoldingFilePath(Path evosuiteDir, String className) {
        if (Files.exists(evosuiteDir) && evosuiteDir.toFile().isDirectory()) {
            return findESTestScaffoldingFilePathRecursive(evosuiteDir.toFile(), className);
        }
        throw new RuntimeException("找不到 _ESTest_scaffolding.java 文件，目录不存在或不是目录");
    }

    private Path findESTestScaffoldingFilePathRecursive(File directory, String className) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归调用查找子目录
                    Path result = findESTestScaffoldingFilePathRecursive(file, className);
                    if (result != null) {
                        return result;
                    }
                } else if (file.getName().equals(className + "_ESTest_scaffolding.java")) {
                    return file.toPath();
                }
            }
        }
        return null;
    }

    private Path findtestFilePath(Path evosuiteDir, String className) {
        if (Files.exists(evosuiteDir) && evosuiteDir.toFile().isDirectory()) {
            return findtestPathRecursive(evosuiteDir.toFile(), className);
        }
        throw new RuntimeException("找不到 _ESTest_scaffolding.java 文件，目录不存在或不是目录");
    }

    private Path findtestPathRecursive(File directory, String className) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归调用查找子目录
                    Path result = findtestPathRecursive(file, className);
                    if (result != null) {
                        return result;
                    }
                } else if (file.getName().equals(className + "_test.java")) {
                    return file.toPath();
                }
            }
        }
        return null;
    }

    @PostMapping("/api/generateLLMTest")
    @ResponseBody
    public Map<String, String> generateLLMTest(@RequestParam("sessionToken") String sessionToken,HttpSession session, HttpServletResponse response) {
        Map<String, Object> scopedSession = SessionScopedUtil.getScopedSession(session, sessionToken);
        Map<String, String> result = new HashMap<>();
        String filePath = (String) scopedSession.get("uploadedFilePath");
        String language = (String) scopedSession.get("language");
        String fileNameWithoutExtension = scopedSession.get("uploadedFilePath") != null ? Paths.get((String) scopedSession.get("uploadedFilePath")).getFileName().toString().replaceFirst("[.][^.]+$", "") : null;

        if (filePath == null) {
            response.setStatus(400);
            result.put("message", "请先上传文件！");
            return result;
        }

        try {
            String command;
            String currentDir = System.getProperty("user.dir");

            if ("java".equals(language)) {
                command = String.format(
                        "C:\\Users\\Wangfei\\anaconda3\\python.exe %s\\src\\api\\api.py %s",
                        currentDir,
                        filePath
                );
            } else if ("python".equals(language)) {
                command = String.format(
                        "C:\\Users\\Wangfei\\anaconda3\\python.exe %s\\src\\api\\apipp.py %s",
                        currentDir,
                        filePath
                );

            } else {
                response.setStatus(400);
                result.put("message", "不支持的语言类型！");
                return result;
            }

            System.out.println("执行的命令: " + command);

            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            Process process = processBuilder.start();

            // 异步处理标准输出
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> outputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> errorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待命令执行完成，设置超时时间
            boolean completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                response.setStatus(500);
                result.put("message", "命令执行超时！");
                return result;
            }

            // 获取命令执行结果
            String output = outputFuture.get();
            String errorOutput = errorFuture.get();

            if (process.exitValue() == 0) {
                result.put("message", "LLM 测试用例生成成功！");
                result.put("sessionToken", sessionToken);

                // 读取生成的测试用例文件内容
                String testCaseCode;
                if ("java".equals(language)) {
                    String outpath = "llm/java";
                    Path estestFilePath = findtestFilePath(Paths.get("llm/java/"), fileNameWithoutExtension);
                    testCaseCode = readTestCaseFile(estestFilePath);

                    TestResult r=new TestResult();
                    String codePath=filePath;

                    Path javallmpath = Paths.get("", outpath, fileNameWithoutExtension + "_test.java").toAbsolutePath();
                    r.setExamplePath(javallmpath.toString());
                    Code c=codeService.findCodeByPath(codePath);
                    if(c!=null){
                        r.setCodeId(c.getCodeId());
                    }
                    r.setUserId(userID);
                    r.setTestMethod("javaLLM");

                    if(testResultService.findByExamplePath(r.getExamplePath())==null){
                        testResultService.insert(r);
                    }
                    else{
                        testResultService.update(r);
                    }
                } else if ("python".equals(language)) {
                    String outpath = "llm/python";
                    testCaseCode = readTestCaseFile(Paths.get(outpath), fileNameWithoutExtension +"_test"+ ".py");

                    TestResult r=new TestResult();
                    String codePath=filePath;

                    Path pyllmpath = Paths.get("", outpath, fileNameWithoutExtension + "_test.py").toAbsolutePath();
                    r.setExamplePath(pyllmpath.toString());
                    Code c=codeService.findCodeByPath(codePath);
                    if(c!=null){
                        r.setCodeId(c.getCodeId());
                    }
                    r.setUserId(userID);
                    r.setTestMethod("pythonLLM");

                    if(testResultService.findByExamplePath(r.getExamplePath())==null){
                        testResultService.insert(r);
                    }
                    else{
                        testResultService.update(r);
                    }
                } else{
                    response.setStatus(400);
                    result.put("message", "不支持的语言类型！");
                    return result;
                }
                result.put("testCaseCode", testCaseCode);
                System.out.println(testCaseCode);
            } else {
                System.err.println("LLM 测试用例生成失败：" + errorOutput);
                response.setStatus(500);
                result.put("message", "LLM 测试用例生成失败：" + errorOutput);
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("执行命令时发生错误：" + e.getMessage());
            response.setStatus(500);
            result.put("message", "执行命令时发生错误：" + e.getMessage());
        }

        return result;
    }


    @PostMapping("/api/apiperformJacocoEvaluation")
    @ResponseBody
    public Map<String, String> apiperformJacocoEvaluation(@RequestParam("sessionToken") String sessionToken,HttpSession session, HttpServletResponse response) {

        Map<String, Object> scopedSession = SessionScopedUtil.getScopedSession(session, sessionToken);
        Map<String, String> result = new HashMap<>();
        String filePath = (String) scopedSession.get("uploadedFilePath");
        String language = (String) scopedSession.get("language");
        Path currentDir = Paths.get("").toAbsolutePath();
        Path generatedTestDir = currentDir.resolve("llm/java");


        if (filePath == null ||!"java".equals(language)) {
            response.setStatus(400);
            result.put("message", "请上传 Java 文件并成功生成测试用例后再进行测评！");
            return result;
        }

        try {
            // 提取类名
            String className = Paths.get(filePath).getFileName().toString().replace(".java", "");

            String javacCommand = String.format(
                    "javac -d uploaded-code/class uploaded-code/java/%s.java",
                    className);
            System.out.println(javacCommand);
            Process process1 = new ProcessBuilder(javacCommand.split(" ")).start();
            int exitCode1 = process1.waitFor();
            if (exitCode1 == 0) {
                System.out.println("编译成功");
            } else {
                System.err.println("编译失败，退出码: " + exitCode1);
                response.setStatus(500);
                result.put("message", "编译失败，退出码: " + exitCode1);
                return result;
            }
            // 读取 Java 文件内容，提取包名
            String packageName = extractPackageName(filePath);
            String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;
            System.out.println("完整的类名: " + fullClassName);

            // 构建 JaCoCo 输出目录路径
            Path jacocoBaseDir = Paths.get("apijacoco").toAbsolutePath();
            Path jacocoOutputDir = jacocoBaseDir.resolve(className);

            // 确保 jacoco 目录存在
            if (!Files.exists(jacocoBaseDir)) {
                Files.createDirectories(jacocoBaseDir);
            }

            // 确保 jacoco/类名 目录存在
            if (!Files.exists(jacocoOutputDir)) {
                Files.createDirectories(jacocoOutputDir);
            }

            // JaCoCo 代理 JAR 文件路径
            String jacocoAgentPath = "src/main/resources/static/jacoco-0.8.13/lib/jacocoagent.jar";
            // JaCoCo 执行文件路径
            String jacocoExecFilePath = jacocoOutputDir.resolve("jacoco.exec").toString();
            // JaCoCo CLI JAR 文件路径
            String jacocoCliJarPath = "src/main/resources/static/jacoco-0.8.13/lib/jacococli.jar";
            // 项目类路径
            String projectClassPath = "uploaded-code/class";
            // 项目源文件路径
            String sourceFilePath = "uploaded-code/java";

            Path estestFilePath = findtestFilePath(Paths.get("llm/java/"), className);
            System.out.println(estestFilePath);

            // 编译
            String java2Command = String.format(
                    "javac -cp \".;src/main/resources/static/junit/junit-4.13.2.jar;src/main/resources/static/evosuite/evosuite-standalone-runtime-1.2.0.jar;evosuite\\%s;uploaded-code\\class\"  %s",
                    className, estestFilePath);
            System.out.println(java2Command);
            Process process2 = new ProcessBuilder(java2Command.split(" ")).start();
            int exitCode2 = process2.waitFor();
            if (exitCode2 == 0) {
                System.out.println("Test成功");
            } else {
                System.err.println("Test失败，退出码: " + exitCode2);
                response.setStatus(500);
                result.put("message", "Test 编译失败，退出码: " + exitCode2);
                return result;
            }

            // 构建运行测试用例并收集覆盖率数据的命令

            String runTestCommand = String.format(
                    "java -javaagent:%s=destfile=%s -cp %s;%s;src/main/resources/static/junit/junit-4.13.2.jar;src/main/resources/static/evosuite/evosuite-standalone-runtime-1.2.0.jar;src/main/resources/static/hamcrest/hamcrest-core-1.3.jar org.junit.runner.JUnitCore %s_test",
                    jacocoAgentPath, jacocoExecFilePath, projectClassPath, generatedTestDir, className);
            System.out.println("执行的运行测试用例命令: " + runTestCommand);

            // 使用 ProcessBuilder 执行命令
            ProcessBuilder runTestProcessBuilder = new ProcessBuilder(runTestCommand.split(" "));
            Process runTestProcess = runTestProcessBuilder.start();

            // 异步处理标准输出
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> runTestOutputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runTestProcess.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> runTestErrorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runTestProcess.getErrorStream()))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> runTestExitCodeFuture = executor.submit(() -> {
                try {
                    return runTestProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });
            int runTestExitCode;
            try {
                runTestExitCode = runTestExitCodeFuture.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                runTestProcess.destroyForcibly();
                response.setStatus(500);
                result.put("message", "运行测试用例失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String runTestOutput = runTestOutputFuture.get();
            String runTestErrorOutput = runTestErrorFuture.get();

//            if (runTestExitCode != 0) {
//                response.setStatus(500);
//                result.put("message", "运行测试用例失败，返回错误代码：" + runTestExitCode + "\n输出信息：\n" + runTestOutput + "\n错误信息：\n" + runTestErrorOutput);
//                System.out.println(runTestOutput);
//                System.out.println(runTestErrorOutput);
//                executor.shutdown();
//                return result;
//            }
            if (runTestExitCode != 0) {
                // 只记录错误信息，不提前返回
                result.put("testExecutionError", "运行测试用例失败，返回错误代码：" + runTestExitCode + "\n输出信息：\n" + runTestOutput + "\n错误信息：\n" + runTestErrorOutput);
                System.out.println(runTestOutput);
                System.out.println(runTestErrorOutput);
            }


            // 构建 JaCoCo 报告生成命令，添加 --includes 参数
            String jacocoReportCommand = String.format(
                    "java -jar %s report %s --classfiles %s --sourcefiles %s --html %s",
                    jacocoCliJarPath, jacocoExecFilePath, projectClassPath, sourceFilePath, jacocoOutputDir);
            System.out.println("执行的 JaCoCo 报告生成命令: " + jacocoReportCommand);

            // 使用 ProcessBuilder 执行命令
            ProcessBuilder jacocoReportProcessBuilder = new ProcessBuilder(jacocoReportCommand.split(" "));
            Process jacocoReportProcess = jacocoReportProcessBuilder.start();

            // 异步处理标准输出
            Future<String> jacocoReportOutputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(jacocoReportProcess.getInputStream(), "UTF-8"))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> jacocoReportErrorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(jacocoReportProcess.getErrorStream(), "UTF-8"))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> jacocoReportExitCodeFuture = executor.submit(() -> {
                try {
                    return jacocoReportProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });
            int jacocoReportExitCode;
            try {
                jacocoReportExitCode = jacocoReportExitCodeFuture.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                jacocoReportProcess.destroyForcibly();
                response.setStatus(500);
                result.put("message", "生成 JaCoCo 报告失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String jacocoReportOutput = jacocoReportOutputFuture.get();
            String jacocoReportErrorOutput = jacocoReportErrorFuture.get();

            executor.shutdown();
            Path htmlFilePath = findHtmlFile(jacocoOutputDir, className + ".html");
            System.out.println(htmlFilePath);

            if (jacocoReportExitCode == 0) {
                // 创建线程池（建议使用单例或Spring管理）
                ExecutorService executor2 = Executors.newFixedThreadPool(2);

                // 提交第一个任务
                CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                    try {
                        String codePath=filePath;
                        Path examplepath = Paths.get(System.getProperty("user.dir"), String.valueOf(estestFilePath));

                        Code c=codeService.findCodeByPath(codePath);
                        // 获取测试结果列表
                        List<TestResult> results = testResultService.findByCodeIdandMethodandUserId(c.getCodeId(),"javaLLM",userID);
                        TestResult r = results.get(0);
                        r.setCodeId(c.getCodeId());

                        LocalDateTime currentDateTime = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String time = currentDateTime.format(formatter);
                        r.setTestTime(time);
                        r.setJudgement(htmlFilePath.toString());
                        testResultService.update(r);
                    } catch (Exception e) {
                        System.err.println("Error in task 1: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, executor2);

                // 提交第二个任务
                CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
                    try {
                        result.put("message", "JaCoCo 测评成功！");
                        result.put("jacocoReportPath", htmlFilePath.toString());
                        result.put("sessionToken", sessionToken);
                    } catch (Exception e) {
                        System.err.println("Error in task 2: " + e.getMessage());
                    }
                }, executor2);

                // 等待所有任务完成
                CompletableFuture.allOf(task1, task2).join();
                executor2.shutdown();
                try {
                    if (!executor2.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor2.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor2.shutdownNow();
                }
            } else {
                response.setStatus(500);
                result.put("message", "生成 JaCoCo 报告失败，返回错误代码：" + jacocoReportExitCode + "\n输出信息：\n" + jacocoReportOutput + "\n错误信息：\n" + jacocoReportErrorOutput);
            }
        } catch (IOException e) {
            response.setStatus(500);
            result.put("message", "测评失败：IO 异常 - " + e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            response.setStatus(500);
            result.put("message", "测评失败：线程执行异常 - " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/api/apiperformPythonCoverageEvaluation")
    @ResponseBody
    public Map<String, String> apiperformPythonCoverageEvaluation(@RequestParam("sessionToken") String sessionToken,HttpSession session, HttpServletResponse response) {
        Map<String, Object> scopedSession = SessionScopedUtil.getScopedSession(session, sessionToken);
        Map<String, String> result = new HashMap<>();
        String filePath = (String) scopedSession.get("uploadedFilePath");
        String language = (String) scopedSession.get("language");

        if (filePath == null || !"python".equals(language)) {
            response.setStatus(400);
            result.put("message", "请上传 Python 文件并成功生成测试用例后再进行测评！");
            return result;
        }

        try {
            // 提取文件名（不包含扩展名）
            String moduleName = Paths.get(filePath).getFileName().toString().replace(".py", "");

            // 构建输出目录路径
            Path coverageOutputPath = Paths.get("coverageapi", moduleName);
            // 确保输出目录存在
            if (!Files.exists(coverageOutputPath)) {
                Files.createDirectories(coverageOutputPath);
            }

            // 设置 PYTHONPATH 环境变量
            String pythonPath = "uploaded-code/python";
            String envPath = System.getenv("PYTHONPATH");
            if (envPath != null && !envPath.isEmpty()) {
                pythonPath = envPath + File.pathSeparator + pythonPath;
            }

            // 构建 coverage run 命令
            String coverageRunCommand = String.format(
                    "cmd.exe /c \"C:\\Users\\Wangfei\\anaconda3\\Scripts\\activate py310_env && set PYTHONPATH=%s && coverage run -m pytest llm/python/%s_test.py\"",
                    pythonPath, moduleName, moduleName);
            System.out.println(coverageRunCommand);

            // 使用 ProcessBuilder 执行 coverage run 命令
            ProcessBuilder coverageRunProcessBuilder = new ProcessBuilder("cmd.exe", "/c", coverageRunCommand);
            Process coverageRunProcess = coverageRunProcessBuilder.start();

            // 异步处理标准输出
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> coverageRunOutputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(coverageRunProcess.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> coverageRunErrorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(coverageRunProcess.getErrorStream()))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> coverageRunExitCodeFuture = executor.submit(() -> {
                try {
                    return coverageRunProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });

            int coverageRunExitCode;
            try {
                coverageRunExitCode = coverageRunExitCodeFuture.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                coverageRunProcess.destroyForcibly();
                response.setStatus(500);
                result.put("message", "运行 coverage run 失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String coverageRunOutput = coverageRunOutputFuture.get();
            String coverageRunErrorOutput = coverageRunErrorFuture.get();

            if (coverageRunExitCode != 0) {
                response.setStatus(500);
                result.put("message", "运行 coverage run 失败，返回错误代码：" + coverageRunExitCode + "\n输出信息：\n" + coverageRunOutput + "\n错误信息：\n" + coverageRunErrorOutput);
                executor.shutdown();
                return result;
            }

            // 构建 coverage html 命令
            String coverageHtmlCommand = String.format(
                    "cmd.exe /c \"C:\\Users\\Wangfei\\anaconda3\\Scripts\\activate py310_env && coverage html --directory=%s\"",
                    coverageOutputPath.toString());

            // 使用 ProcessBuilder 执行 coverage html 命令
            ProcessBuilder coverageHtmlProcessBuilder = new ProcessBuilder("cmd.exe", "/c", coverageHtmlCommand);
            Process coverageHtmlProcess = coverageHtmlProcessBuilder.start();

            // 异步处理标准输出
            Future<String> coverageHtmlOutputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(coverageHtmlProcess.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            // 异步处理错误输出
            Future<String> coverageHtmlErrorFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(coverageHtmlProcess.getErrorStream()))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    return errorOutput.toString();
                }
            });

            // 等待进程执行完成，设置超时时间
            Future<Integer> coverageHtmlExitCodeFuture = executor.submit(() -> {
                try {
                    return coverageHtmlProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            });

            int coverageHtmlExitCode;
            try {
                coverageHtmlExitCode = coverageHtmlExitCodeFuture.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                coverageHtmlProcess.destroyForcibly();
                response.setStatus(500);
                result.put("message", "运行 coverage html 失败：命令执行超时，已强制终止。");
                executor.shutdownNow();
                return result;
            }

            // 获取输出和错误信息
            String coverageHtmlOutput = coverageHtmlOutputFuture.get();
            String coverageHtmlErrorOutput = coverageHtmlErrorFuture.get();

            executor.shutdown();

            Path htmlPath = coverageOutputPath.resolve("index.html");
            Path htmlFilePath = Paths.get(System.getProperty("user.dir"), String.valueOf(htmlPath));

            if (coverageHtmlExitCode == 0) {
                // 创建线程池（建议使用单例或Spring管理）
                ExecutorService executor2 = Executors.newFixedThreadPool(2);

                // 提交第一个任务
                CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                    try {
                        String codePath=filePath;
                        Path outputPath = Paths.get(System.getProperty("user.dir"), "llm/python");

                        Code c=codeService.findCodeByPath(codePath);
                        // 获取测试结果列表
                        List<TestResult> results = testResultService.findByCodeIdandMethodandUserId(c.getCodeId(),"pythonLLM",userID);
                        TestResult r = results.get(0);
                        r.setCodeId(c.getCodeId());

                        LocalDateTime currentDateTime = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String time = currentDateTime.format(formatter);
                        r.setTestTime(time);
                        r.setJudgement(htmlFilePath.toString());
                        testResultService.update(r);
                    } catch (Exception e) {
                        System.err.println("Error in task 1: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, executor2);

                // 提交第二个任务
                CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
                    try {
                        result.put("message", "Python 覆盖率测评成功！");
                        result.put("coverageReportPath", htmlFilePath.toString());
                        result.put("sessionToken", sessionToken);
                    } catch (Exception e) {
                        System.err.println("Error in task 2: " + e.getMessage());
                    }
                }, executor2);

                // 等待所有任务完成
                CompletableFuture.allOf(task1, task2).join();
                executor2.shutdown();
                try {
                    if (!executor2.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor2.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor2.shutdownNow();
                }
            } else {
                response.setStatus(500);
                result.put("message", "运行 coverage html 失败，返回错误代码：" + coverageHtmlExitCode + "\n输出信息：\n" + coverageHtmlOutput + "\n错误信息：\n" + coverageHtmlErrorOutput);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            response.setStatus(500);
            result.put("message", "测评失败：" + e.getMessage());
        }
        return result;
    }

    private String readTestCaseFile(Path directory, String fileName) {
        StringBuilder content = new StringBuilder();
        Path filePath = directory.resolve(fileName);
        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException e) {
                System.err.println("读取测试用例文件时出错: " + e.getMessage());
            }
        }
        return content.toString();
    }
    private String readTestCaseFile(Path filePath) {
        StringBuilder content = new StringBuilder();
        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException e) {
                System.err.println("读取测试用例文件时出错: " + e.getMessage());
            }
        }
        return content.toString();
    }
    private Path findHtmlFile(Path directory, String fileName) throws IOException {
        try (Stream<Path> walk = Files.walk(directory)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst()
                    .orElse(null);
        }
    }


}
