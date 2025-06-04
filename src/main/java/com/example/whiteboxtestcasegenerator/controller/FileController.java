//package com.example.whiteboxtestcasegenerator.controller;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.net.URLDecoder;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//@RestController
//public class FileController {
//
//
//    @GetMapping("/api/getFiles")
//    public List<FileItem> getFiles(@RequestParam String path) {
//        try {
//            // 对路径进行解码
//            path = URLDecoder.decode(path, String.valueOf(StandardCharsets.UTF_8));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        List<FileItem> fileItems = new ArrayList<>();
//        Path dirPath = Paths.get(path);
//        try {
//            Files.list(dirPath).forEach(file -> {
//                FileItem item = new FileItem();
//                item.setName(file.getFileName().toString());
//                item.setType(file.toFile().isDirectory()? "folder" : "file");
//                if (item.getType().equals("folder")) {
//                    item.setChildren(getFiles(file.toString()));
//                }
//                fileItems.add(item);
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return fileItems;
//    }
//
//    @GetMapping(value = "/api/getFileContent", produces = "text/plain;charset=UTF-8")
//    public String getFileContent(@RequestParam String path) {
//        try {
//            // 对路径进行解码
//            path = URLDecoder.decode(path, String.valueOf(StandardCharsets.UTF_8));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        StringBuilder content = new StringBuilder();
//        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path),StandardCharsets.UTF_8)) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                content.append(line).append("\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return content.toString();
//    }
//}
//
//class FileItem {
//    private String name;
//    private String type;
//    private List<FileItem> children = new ArrayList<>();
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public List<FileItem> getChildren() {
//        return children;
//    }
//
//    public void setChildren(List<FileItem> children) {
//        this.children = children;
//    }
//}
package com.example.whiteboxtestcasegenerator.controller;

import com.example.util.FileReaderUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FileController {

    @GetMapping("/api/getFiles")
    public List<FileItem> getFiles(@RequestParam String path) {
        try {
            path = URLDecoder.decode(path, String.valueOf(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<FileItem> fileItems = new ArrayList<>();
        Path dirPath = Paths.get(path);
        try {
            Files.list(dirPath).forEach(file -> {
                FileItem item = new FileItem();
                item.setName(file.getFileName().toString());
                item.setType(file.toFile().isDirectory()? "folder" : "file");
                if (item.getType().equals("folder")) {
                    item.setChildren(getFiles(file.toString()));
                }
                fileItems.add(item);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileItems;
    }

    @GetMapping(value = "/api/getFileContent", produces = "text/plain;charset=UTF-8")
    public String getFileContent(@RequestParam String path) {
        try {
            // 解码 URL 参数
            path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());

            // 使用工具类读取文件，自动处理多种格式
            return FileReaderUtil.readFile(path);
        } catch (Exception e) {
            // 记录异常信息（实际项目中建议使用日志框架）
            e.printStackTrace();

            // 返回错误信息（根据业务需求调整返回格式）
            return "读取文件失败: " + e.getMessage();
        }
    }

    @GetMapping(value = "/api/runJavaCode", produces = "text/plain;charset=UTF-8")
    public String runJavaCode(@RequestParam String path) {
        try {
            path = URLDecoder.decode(path, String.valueOf(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "Error decoding path: " + e.getMessage();
        }

        StringBuilder output = new StringBuilder();
        Path filePath = Paths.get(path);

        // 验证文件存在
        if (!Files.exists(filePath)) {
            return "Error: File not found - " + path;
        }

        // 验证是Java文件
        if (!path.endsWith(".java")) {
            return "Error: Not a Java source file - " + path;
        }

        String className = filePath.getFileName().toString().replace(".java", "");
        Path parentDir = filePath.getParent();

        try {
            // 创建临时目录用于编译
            Path tempDir = Files.createTempDirectory("java-compile-");

            // 编译 Java 代码
            ProcessBuilder compileBuilder = new ProcessBuilder(
                    "javac",
                    "-encoding", "UTF-8",
                    "-d", tempDir.toString(),
                    path
            );

            // 设置工作目录
            compileBuilder.directory(parentDir.toFile());

            Process compileProcess = compileBuilder.start();

            // 获取编译错误输出
            BufferedReader compileErrorReader = new BufferedReader(
                    new InputStreamReader(compileProcess.getErrorStream())
            );

            String line;
            while ((line = compileErrorReader.readLine()) != null) {
                output.append("Compile Error: ").append(line).append("\n");
            }

            int compileExitCode = compileProcess.waitFor();
            if (compileExitCode != 0) {
                output.append("Compilation failed with exit code: ").append(compileExitCode).append("\n");
                return output.toString();
            }

            // 运行 Java 代码
            ProcessBuilder runBuilder = new ProcessBuilder(
                    "java",
                    "-Dfile.encoding=UTF-8",
                    "-cp", tempDir.toString(),
                    className
            );

            // 设置工作目录
            runBuilder.directory(parentDir.toFile());

            Process runProcess = runBuilder.start();

            // 获取标准输出
            BufferedReader runOutputReader = new BufferedReader(
                    new InputStreamReader(runProcess.getInputStream())
            );

            while ((line = runOutputReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 获取错误输出
            BufferedReader runErrorReader = new BufferedReader(
                    new InputStreamReader(runProcess.getErrorStream())
            );

            while ((line = runErrorReader.readLine()) != null) {
                output.append("Error: ").append(line).append("\n");
            }

            int runExitCode = runProcess.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            output.append("Error: ").append(e.getMessage()).append("\n");
        }

        return output.toString();
    }

    @GetMapping(value = "/api/runPythonCode", produces = "text/plain;charset=UTF-8")
    public String runPythonCode(@RequestParam String path) {
        try {
            path = URLDecoder.decode(path, String.valueOf(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "Error decoding path: " + e.getMessage();
        }

        StringBuilder output = new StringBuilder();
        Path filePath = Paths.get(path);

        // 验证文件存在
        if (!Files.exists(filePath)) {
            return "Error: File not found - " + path;
        }

        // 验证是Python文件
        if (!path.endsWith(".py")) {
            return "Error: Not a Python source file - " + path;
        }

        Path parentDir = filePath.getParent();

        try {

            // 尝试使用python3命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    path
            );

            // 设置工作目录
            processBuilder.directory(parentDir.toFile());

            Process process = processBuilder.start();

            // 获取标准输出
            BufferedReader outputReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = outputReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 获取错误输出
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream())
            );

            while ((line = errorReader.readLine()) != null) {
                output.append("Error: ").append(line).append("\n");
            }

            int exitCode = process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            output.append("Error: ").append(e.getMessage()).append("\n");

            // 如果python3失败，尝试使用python命令
            try {
                output.append("\nTrying 'python' command instead...\n");

                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python",
                        path
                );

                processBuilder.directory(parentDir.toFile());
                Process process = processBuilder.start();

                BufferedReader outputReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

                String line;
                while ((line = outputReader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream())
                );

                while ((line = errorReader.readLine()) != null) {
                    output.append("Error: ").append(line).append("\n");
                }

                int exitCode = process.waitFor();

            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
                output.append("Error: ").append(ex.getMessage()).append("\n");
                output.append("Python command not found. Make sure Python is installed and added to PATH.\n");
            }
        }

        return output.toString();
    }
}

class FileItem {
    private String name;
    private String type;
    private List<FileItem> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FileItem> getChildren() {
        return children;
    }

    public void setChildren(List<FileItem> children) {
        this.children = children;
    }
}