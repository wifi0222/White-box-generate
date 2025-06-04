import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入代码文件路径：");
        String filePath = scanner.nextLine().trim();
        scanner.close();

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("文件不存在或不是有效文件！");
            return;
        }

        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            System.out.println("文件没有扩展名，不支持执行！");
            return;
        }

        String fileExtension = fileName.substring(dotIndex + 1).toLowerCase();
        try {
            switch (fileExtension) {
                case "py":
                    executePython(file);
                    break;
                case "java":
                    compileAndRunJava(file);
                    break;
                case "c":
                case "cpp":
                    compileAndRunC(file, fileExtension);
                    break;
                default:
                    System.out.println("不支持的文件类型: " + fileExtension);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("执行过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executePython(File file) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("python", file.getAbsolutePath());
        Process process = pb.start();
        System.out.println("\n执行Python输出:");
        handleProcessOutput(process);
    }

    private static void compileAndRunJava(File file) throws IOException, InterruptedException {
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
            return;
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
        handleProcessOutput(runProcess);
    }

    private static void compileAndRunC(File file, String ext) throws IOException, InterruptedException {
        // 编译C/C++文件
        String compiler = ext.equals("cpp") ? "g++" : "gcc";
        String exePath = file.getAbsolutePath().replace("." + ext, ".exe");

        Process compileProcess = new ProcessBuilder(
                compiler,
                "-o",
                exePath,
                file.getAbsolutePath()
        ).start();

        int compileExitCode = compileProcess.waitFor();
        if (compileExitCode != 0) {
            System.out.println("\nC/C++编译错误:");
            printStream(compileProcess.getErrorStream());
            return;
        }

        // 运行可执行文件
        Process runProcess = new ProcessBuilder(exePath).start();
        System.out.println("\n程序执行输出:");
        handleProcessOutput(runProcess);
    }

    private static void handleProcessOutput(Process process) {
        // 并行处理输出流和错误流
        Thread outputThread = new Thread(() -> printStream(process.getInputStream()));
        Thread errorThread = new Thread(() -> printStream(process.getErrorStream()));

        outputThread.start();
        errorThread.start();

        try {
            int exitCode = process.waitFor();
            outputThread.join();
            errorThread.join();
            System.out.println("\n进程退出码: " + exitCode);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "GBK"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}