import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main_Project {

    public static void main(String[] args) {
        // 获取用户输入的文件路径8
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("请输入文件路径：");
        try {
            String filePath = reader.readLine();

            // 获取文件信息
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("文件不存在！");
                return;
            }

            String fileName = file.getName();
            String fileExtension = ""; // 获取文件后缀
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileExtension = fileName.substring(lastDotIndex + 1);
                System.out.println("文件扩展名: " + fileExtension);
            } else {
                System.out.println("此文件没有扩展名！");
                return;
            }

            // 获取父文件夹路径
            String parentFolderPath = file.getParent();
            System.out.println("父文件夹路径: " + parentFolderPath);

            // 根据文件扩展名决定如何运行代码
            String command = "";
            if (fileExtension.equalsIgnoreCase("py")) {
                // Python文件
                command = "python " + filePath;
            } else if (fileExtension.equalsIgnoreCase("java")) {
                // Java文件
                String classpath = findRootDirectory(filePath, "src");
                // 编译Java文件
                CompileResult res = compileJavaFile(filePath, classpath);
                if (res.isSuccess()) {
                    String classFilePath = filePath.replace(".java", "");
                    // 计算 runPath
                    String runPath = classFilePath.substring(classpath.length() + 1).replace("\\", ".");
                    System.out.println("运行路径: " + runPath);
                    command = "java " + runPath;
                } else {
                    System.out.println("编译失败:\n" + res.getErrors());
                    return;
                }
            } else if (fileExtension.equalsIgnoreCase("cpp") || fileExtension.equalsIgnoreCase("c")) {
                // C 或 C++ 文件
                String compiler = fileExtension.equalsIgnoreCase("cpp") ? "g++" : "gcc";
                Process compileProcess = Runtime.getRuntime().exec(compiler + " -o " + filePath.substring(0, filePath.lastIndexOf(".")) + ".exe " + filePath);
                compileProcess.waitFor();
                if (compileProcess.exitValue() != 0) {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream(), "GBK"));
                    StringBuilder errorMessage = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorMessage.append(line).append("\n");
                    }
                    System.out.println("编译失败:\n" + errorMessage.toString());
                    return;
                }
                command = filePath.substring(0, filePath.lastIndexOf(".")) + ".exe";
            } else {
                System.out.println("不支持的文件类型！");
                return;
            }

            // 执行命令并获取运行结果
            System.out.println("执行命令: " + command);
            Process process = Runtime.getRuntime().exec(command);
            try (BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = processReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                process.waitFor(); // 等待运行完成
                int exitValue = process.exitValue();
                if (exitValue != 0) {
                    System.out.println("运行失败！");
                    return;
                }
                // 输出运行结果
                System.out.println("运行结果:\n" + result);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("发生错误: " + e.getMessage());
        }
    }

    // 模拟查找根目录的函数（您可以根据实际需要改进）
    private static String findRootDirectory(String filePath, String src) {
        // 简单地模拟返回文件路径中的 src 目录部分
        return filePath.substring(0, filePath.indexOf(src) + src.length());
    }

    // 模拟 Java 编译函数（您可以根据实际情况改进）
    private static CompileResult compileJavaFile(String filePath, String classpath) {
        try {
            // 模拟 Java 编译过程
            String command = "javac -cp " + classpath + " " + filePath;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() == 0) {
                return new CompileResult(true, "");
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "GBK"));
                StringBuilder errorMessage = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorMessage.append(line).append("\n");
                }
                return new CompileResult(false, errorMessage.toString());
            }
        } catch (IOException | InterruptedException e) {
            return new CompileResult(false, e.getMessage());
        }
    }

    // 用于保存编译结果的类
    static class CompileResult {
        private boolean success;
        private String errors;

        public CompileResult(boolean success, String errors) {
            this.success = success;
            this.errors = errors;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrors() {
            return errors;
        }
    }
}
