package com.example.controller;

import com.example.pojo.Code;
import com.example.pojo.TestResult;
import com.example.pojo.User;
import com.example.service.CodeService;
import com.example.service.TestResultService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CodeController {
    @Autowired
    private UserService userService;

    @Autowired
    private CodeService codeService;
    @Autowired
    private TestResultService testResultService;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    // 处理通过ID加载的代码内容上传
    @PostMapping("/test/uploadContent")
    @ResponseBody
    public Map<String, String> uploadCodeContent(
            @RequestParam("codeContent") String codeContent,
            @RequestParam("codeName") String codeName,
            @RequestParam("codeType") String codeType,
            @RequestParam Integer userId,
            HttpSession session, Model model) {
        Map<String, String> response = new HashMap<>();

        try {
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

            session.setAttribute("uploadedFilePath", dest.toString());
            session.setAttribute("language", codeType.equals(".java") ? "java" : "python");
            response.put("message", "上传成功：" + codeName);

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
    @RequestMapping(value = "/codeUpload")
    public String toCodeUpload(Model model, @RequestParam("userId") Integer userId, @RequestParam("codeFile") MultipartFile codeFile, @RequestParam("codeInfo") String codeInfo,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user", user);


        System.out.println("进来了");
        Code c = new Code();

        if (!codeFile.isEmpty()) {
            // 获取上传文件的原始文件名
            String fileName = codeFile.getOriginalFilename();

            String projectRoot = System.getProperty("user.dir");
            String filePath = projectRoot + "/uploadCodeFiles/";
            //String filePath = "D:\\Javaprac\\ppp\\Manage\\ManageSystem\\src\\main\\webapp\\uploads\\" + fileName; // 文件保存路径
            System.out.println("filepath" + filePath);
            // 创建目录（如果不存在的话）
            File uploadDir = new File(filePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs(); // 创建目录
            }
            try {
                // 将文件写入到指定路径
                codeFile.transferTo(new File(filePath + fileName));

                c.setCodeName(fileName);
                c.setCodePath(filePath + fileName);
                c.setVersion(1.0F);

                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String time = currentDateTime.format(formatter);
                c.setUpdateTime(time);

                c.setCodeInfo(codeInfo);
                c.setUserId(userId);


                String type = "";

                int lastIndex = fileName.lastIndexOf('.');
                if (lastIndex != -1 && lastIndex < fileName.length() - 1) {
                    type = fileName.substring(lastIndex);
                }

                c.setCodeType(type);
                Code code0=codeService.findCodeByPath(filePath + fileName);
                if(code0 == null) codeService.insertCode(c);
                else{
                    c.setCodeId(code0.getCodeId());
                    codeService.updateCode(c);
                }

                List<Code> codeList = new ArrayList<>();
                codeList = codeService.findCodesByUserId(user.getUserId());
                model.addAttribute("codeList", codeList);

                System.out.println(c.getCodeId());
                System.out.println(c.getUpdateTime());
                System.out.println("上传成功");

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "文件上传失败！");
                //return "error"; // 跳转到错误页面
            }

        }
        return "codeManagement";
    }


    @RequestMapping(value = "/uploadfloder")
    public String touploadfloder(Model model, @RequestParam Integer userId, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }
        User user = userService.findUserByUserId(userId);
        model.addAttribute("user", user);
        List<Code> codeList = new ArrayList<>();
        codeList = codeService.findCodesByUserId(user.getUserId());
        model.addAttribute("codeList", codeList);
        return "uploadfloder";
    }

    @RequestMapping(value = "/codeManagement")
    public String toCodeManagement(Model model, @RequestParam Integer userId, HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }
        User user = userService.findUserByUserId(userId);
        model.addAttribute("user", user);
        List<Code> codeList = new ArrayList<>();
        codeList = codeService.findCodesByUserId(user.getUserId());
        model.addAttribute("codeList", codeList);
        return "codeManagement";
    }

    @RequestMapping(value = "/viewCode")
    public String viewCode(Model model, @RequestParam Integer userId, @RequestParam Integer codeId, HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        if (user == null) {
            model.addAttribute("error", "用户未找到");
            return "error"; // 跳转到错误页面
        }
        model.addAttribute("user", user);

        Code code = codeService.findCodeByCodeId(codeId);
        if (code == null) {
            model.addAttribute("error", "代码未找到");
            return "error"; // 跳转到错误页面
        }
        model.addAttribute("code", code);

        // 读取代码文件内容
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

        return "viewCode";  // 源代码显示页面，在里面可以编辑并运行
    }

    @PostMapping(value = "/saveCode")
    public String saveCode(@RequestParam Integer codeId, @RequestParam Integer userId, @RequestParam String codeContent, @RequestParam String codeInfo, Model model,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }
        try {
            User user = userService.findUserByUserId(userId);
            Code code = codeService.findCodeByCodeId(codeId);
            if (code == null) {
                System.out.println("修改的：codeId" + codeId);
                model.addAttribute("error", "代码未找到");
                System.out.println("修改的：codeId" + codeId);
                return "error"; // 跳转到错误页面
            }
            System.out.println("修改的：codeId  " + codeId);
            System.out.println("修改的：userId  " + userId);

            code.setVersion((float) (code.getVersion()+1.0));
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String time = currentDateTime.format(formatter);
            code.setUpdateTime(time);
            code.setCodeInfo(codeInfo);
            codeService.updateCode(code);

            // 保存代码内容到文件
            Path path = Paths.get(code.getCodePath());
            Files.write(path, codeContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 在返回的 model 中，添加最新的代码内容
            model.addAttribute("user", user);
            model.addAttribute("code", code);
            model.addAttribute("codeContent", codeContent); // 传递修改后的代码内容

            return "viewCode"; // 返回到 viewCode 页面
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "保存代码时发生错误");
            return "error"; // 跳转到错误页面
        }
    }



    private String executePython(File file) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("python", file.getAbsolutePath());
        Process process = pb.start();
        System.out.println("\n执行Python输出:");
        return handleProcessOutput(process);
    }
    private  String compileAndRunJava(File file) throws IOException, InterruptedException {
        // 编译Java文件
        Process compileProcess = new ProcessBuilder(
                "javac",
                "-encoding", "UTF-8",
                "-source", "1.8",
                "-target", "1.8",
                file.getAbsolutePath()
        ).start();

        int compileExitCode = compileProcess.waitFor();
        if (compileExitCode != 0) {
            System.out.println("\nJava编译错误:");
            printStream(compileProcess.getErrorStream());
        }

        // 运行Java程序
        String className = file.getName().replace(".java", "");
        String classpath = file.getParent();
        Process runProcess = new ProcessBuilder(
                "java",
                "-classpath",
                classpath,
                className
        ).start();

        System.out.println("\nJava程序输出:");
        return handleProcessOutput(runProcess);
    }

    private static String handleProcessOutput(Process process) {
        StringBuilder output = new StringBuilder();

        // 并行处理输出流和错误流
        Thread outputThread = new Thread(() -> {
            output.append(getStreamAsString(process.getInputStream()));
        });
        Thread errorThread = new Thread(() -> {
            output.append(getStreamAsString(process.getErrorStream()));
        });

        outputThread.start();
        errorThread.start();

        try {
            int exitCode = process.waitFor();
            outputThread.join();
            errorThread.join();
            output.append("\n进程退出码: ").append(exitCode);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return output.toString();  // 返回所有输出内容
    }
    private static String getStreamAsString(InputStream inputStream) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "GBK"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();  // 返回流的内容
    }

//    private void handleProcessOutput(Process process) {
//        // 并行处理输出流和错误流
//        Thread outputThread = new Thread(() -> printStream(process.getInputStream()));
//        Thread errorThread = new Thread(() -> printStream(process.getErrorStream()));
//
//        outputThread.start();
//        errorThread.start();
//
//        try {
//            int exitCode = process.waitFor();
//            outputThread.join();
//            errorThread.join();
//            System.out.println("\n进程退出码: " + exitCode);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    private void printStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "GBK"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/runCode")
    public String toRunCode(Model model, @RequestParam Integer userId, @RequestParam Integer codeId,HttpSession session) {
        System.out.println("111111111111111111111111111111111111111111");

        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }
        User user = userService.findUserByUserId(userId);
        model.addAttribute("user", user);
        List<Code> codeList = new ArrayList<>();
        codeList = codeService.findCodesByUserId(user.getUserId());
        model.addAttribute("codeList", codeList);
        Code code = codeService.findCodeByCodeId(codeId);
        model.addAttribute("code", code);


        if (user == null) {
            model.addAttribute("error", "用户未找到");
            return "error"; // 跳转到错误页面
        }
        model.addAttribute("user", user);


        if (code == null) {
            model.addAttribute("error", "代码未找到");
            return "error"; // 跳转到错误页面
        }
        model.addAttribute("code", code);

        // 读取代码文件内容
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

        String filePath = code.getCodePath();
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("文件不存在或不是有效文件！");
        }

        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            System.out.println("文件没有扩展名，不支持执行！");
        }

        String fileExtension = fileName.substring(dotIndex + 1).toLowerCase();
        String result="";
        try {
            switch (fileExtension) {
                case "py":
                    result=executePython(file);
                    break;
                case "java":
                    result=compileAndRunJava(file);
                    break;
                default:
                    System.out.println("不支持的文件类型: " + fileExtension);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("执行过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        model.addAttribute("result",result);
        return "viewCode";
    }




    @RequestMapping(value = "/deleteCode")
    public String deleteCode(Model model, @RequestParam Integer userId, @RequestParam Integer codeId,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user",user);

        List<TestResult> results= (List<TestResult>) testResultService.findByCodeId(codeId);
        if (results != null) {
            for(int i=0;i<results.size();i++){
                testResultService.delete(results.get(i).getTestId());
            }
        }
        if(codeService.findCodeByCodeId(codeId)!=null) codeService.deleteCodeByCodeId(codeId);

        List<Code> codeList = new ArrayList<>();
        codeList = codeService.findCodesByUserId(user.getUserId());
        model.addAttribute("codeList",codeList);

        return "redirect:/codeManagement?userId=" + userId;
    }


    @RequestMapping(value = "/codeSelect")
    public String codeSelect(Model model, @RequestParam Integer userId, @RequestParam String codeKey,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user",user);

        List<Code> codeList = new ArrayList<>();
        codeList = codeService.findCodeByKey(codeKey);
        model.addAttribute("codeList",codeList);

        return "codeManagement";
    }

}
