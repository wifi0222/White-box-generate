
import java.io.*;

public class FileCopy {
    private static boolean formerIsAncestor(File f1, File f2) {
        File parent = f2.getParentFile();
        while (parent != null) {
            if (parent.equals(f1)) {
                return true;
            }
            parent = parent.getParentFile();
        }
        return false;
    }

    private static boolean copyFile(File source, File destDir) {
    File destFile = new File(destDir, source.getName());
    if (destFile.exists()) {
        System.out.println("把" + source.getAbsolutePath() + "拷贝至" + destDir.getAbsolutePath() + "时出现重名。");
        return false;
    }
    try (InputStream input = new FileInputStream(source);
         OutputStream output = new FileOutputStream(destFile)) {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }
    return true;
}


    private static boolean copyDirectory(File source, File destDir) {
        if (!destDir.exists()) {
            return false;
        }
        for (File file : source.listFiles()) {
            if (file.isDirectory()) {
                if (!copyDirectory(file, new File(destDir, file.getName()))) {
                    return false;
                }
            } else {
                if (!copyFile(file, destDir)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean copyTo(File source, File destDir) {
        if (source.isDirectory()) {
            return copyDirectory(source, destDir);
        } else {
            return copyFile(source, destDir);
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("您输入的参数不够。至少要有3个参数:源路径、--#、目的目录路径");
            return;
        }

        int hashIndex = -1;
        for (int i = 0; i < args.length; i++) {
            if ("--#".equals(args[i])) {
                hashIndex = i;
                break;
            }
        }

        if (hashIndex == -1) {
            System.out.println("未出现拷贝方向符号--#作为拷贝标记的独立参数。");
            return;
        }

        if (hashIndex == 0 || hashIndex == args.length - 1) {
            System.out.println("拷贝方向符号只能出现在参数列表的中间，不能出现在两端。");
            return;
        }

        StringBuilder sourcePathBuilder = new StringBuilder();
        for (int i = 0; i < hashIndex; i++) {
            sourcePathBuilder.append(args[i]).append(" ");
        }
        String sourcePath = sourcePathBuilder.toString().trim();

        StringBuilder destPathBuilder = new StringBuilder();
        for (int i = hashIndex + 1; i < args.length; i++) {
            destPathBuilder.append(args[i]).append(" ");
        }
        String destPath = destPathBuilder.toString().trim();

        File source = new File(sourcePath);
        File destDir = new File(destPath);

        if (!source.exists()) {
            System.out.println("源文件夹" + source.getAbsolutePath() + "不存在。");
            return;
        }
        if (!destDir.exists()) {
            System.out.println("目的文件夹" + destDir.getAbsolutePath() + "不存在。");
            return;
        }
        if (!destDir.isDirectory()) {
            System.out.println("目的路径" + destDir.getAbsolutePath() + "不是目录");
            return;
        }
        if (formerIsAncestor(source, destDir)) {
            System.out.println("源文件夹" + source.getAbsolutePath() + "是目的文件夹" + destDir.getAbsolutePath() + "本身或其祖先，会出现无限循环拷贝。");
            return;
        }

        boolean success = copyTo(source, destDir);
        if (success) {
            System.out.println("文件拷贝成功。");
        } else {
            System.out.println("文件拷贝过程中出现异常，拷贝不成功或不完全成功。");
        }
    }
}
