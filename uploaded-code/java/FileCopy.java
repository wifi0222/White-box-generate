//文件FileCopy.java
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;

//这是实验唯一的类，没有其他辅助类：
public class FileCopy {
    //该方法判断抽象文件/夹f1是否是抽象文件/夹f2的自身或祖先（自身也是祖先的一种）：
    private static boolean formerIsAncestor(File f1, File f2) {
        //在这里添加你的代码。本方法是辅助方法，你可以用别的命名，也可以用别的方式实现该方法。
        try {
            // 尝试获取f1的规范文件路径
            File canonicalF1 = f1.getCanonicalFile();
            // 尝试获取f2的规范文件路径
            File canonicalF2 = f2.getCanonicalFile();
            // 如果f1和f2的规范文件路径相同，则f1是f2的祖先
            if (canonicalF1.equals(canonicalF2))
            {
                return true;
            }
            else
            {
                // 否则，获取f2的父文件夹
                File parentF2 = canonicalF2.getParentFile();
                // 循环检查父文件夹是否为f1
                while (parentF2 != null)
                {
                    // 如果找到一个与f1相同的父文件夹，则返回true
                    if (parentF2.equals(canonicalF1))
                    {
                        return true;
                    }
                    // 继续检查下一个父文件夹
                    parentF2 = parentF2.getParentFile();
                }
            }
            // 如果所有父文件夹都不等于f1，则返回false
            return false;
        }
        catch (IOException e)
        {
            // 如果发生异常（例如文件访问问题），则抛出异常信息
            throw new RuntimeException("IOException was thrown when trying to get the canonical paths of the files.", e);
        }
    }

    //这是实现文件拷贝的主要方法。一般来说，需要递归方能实现。
    //当出现异常时，拷贝任务不能完全实现，或完全不能实现，此时要返回false。
    //如果拷贝成功，则返回true。
private static boolean copyTo(File source, File destDir) {
        //在这里添加你的代码，注意各种可能情况的判断。
    if (source.isDirectory()) {// 如果源是目录，则遍历其所有子文件/夹
        File destSubDir = new File(destDir, source.getName());//获取目标子目录
        // 确保destSubDir是一个目录
        if (!destSubDir.isDirectory()) {
            throw new RuntimeException("目标位置不是一个目录: " + destSubDir.getAbsolutePath());
        }
//        if (destSubDir.exists()) {	// 如果目的文件已存在，则输出错误信息并返回 false
//            System.out.println("Error: 目的文件已存在，拷贝操作被取消。");
//            return false;
//        }
        // 递归拷贝子文件和子文件夹
        for (File child : source.listFiles()) {
            if (!copyTo(child, destSubDir)) {
                return false;
            }
        }
    }
    else {
        // 如果源是文件，则创建对应的目的文件对象
        File destFile = new File(destDir, source.getName());
        if (destFile.exists()) {	// 如果目的文件已存在，则输出错误信息并返回 false
            System.out.println("Error: 目的文件已存在，拷贝操作被取消。");
            return false;
        }
        // 使用 Files.copy 方法拷贝文件
        try {
            Files.copy(source.toPath(),destDir.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        try {
//            Files.copy(source.toPath(), destDir.toPath().resolve(source.getName()));
//        }
//        catch (IOException e) {
//            return false; // 如果拷贝过程中出现异常，则返回 false
//        }
    }
    // 如果所有文件/夹都拷贝成功，则返回 true
    return true;
    }


    //这是类的主方法，里面有重要的健壮性判断功能要实现：
    //（1）参数个数必须大于或等于3个。前面若干个参数构成源文件/夹路径；后面若干个参数表示目的目录路径；中间一个参数 --# 表示拷贝的方向为从左边的路径到右面的路径。
    //（2）如果没有方向参数 --# 则为错误，程序退出。
    //（3）如果方向参数在最左侧或最右侧都是错误的，程序退出。
    //（4）方向参数左侧的各个参数拼接出源文件/夹的整体路径，它们之间用空格分隔；这说明：该程序可以对带有空格的文件名/文件夹名进行操作。
    //（5）同上，参数右侧的各个参数拼接出目的文件夹整体路径，它们之间用空格分隔。
    //（6）判断源和目的是否存在，不存在则终止操作。
    //（7）判断目的文件夹是否是文件夹，如果不是，则终止操作。
    //（8）判断源文件夹是否是目的文件夹的祖先（或自身），如果是，则会出现无限拷贝的情况，需要立即终止操作。
    //（9）如果所有情况正常，则可以用文件流拷贝所有内容，调用方法copyTo()方法。
    //（10）在上述各种操作判断出现问题时，一定要反馈用户错误原因和类型。
    //（11）如果拷贝成功，则反馈给用户；如果不成功或部分成功，则也要反馈给用户。
 public static void main(String[] args) {
    // 参数个数必须大于或等于 3 个。
     if (args.length < 3) {
        System.out.println("错误： 输入的参数不够，至少要有3个参数：源路径、--#、目的目录路径。");
        System.exit(1);
    }

    // 查找 --# 标记的位置
    int hashIndex = -1;
    for (int i = 0; i < args.length; i++) {
        if (args[i].equals("--#")) {
            hashIndex = i;
            break;
        }
    }

// 如果没有方向参数 --# 则为错误，程序退出。
    if (hashIndex == -1) {
        System.out.println("错误:未出现拷贝方向符 --# 作为拷贝标记的独立参数。");
        System.exit(1);
    }
 // 如果方向参数在最左侧或最右侧都是错误的，程序退出。
    if(hashIndex == 0|| hashIndex == args.length-1){
        System.out.println("错误:拷贝方向符只能出现在参数列表中间，不能出现在两端。");
        System.exit(1);
    }
    // 解析源路径
    String sourcePath = args[0];
    for (int i = 1; i < hashIndex; i++) {
        sourcePath += " " + args[i];
    }
    File source = new File(sourcePath);

    // 解析目标路径
    String destPath = args[hashIndex + 1];
    for (int i = hashIndex + 2; i < args.length; i++) {
        destPath += " " + args[i];
    }
    File destDir = new File(destPath);

    // 判断源和目的是否存在，不存在则终止操作
    if (!source.exists()) {
        System.out.println("错误: 源文件夹不存在。");
        System.exit(1);
    }
    if (!destDir.exists()) {
        System.out.println("错误: 目的文件夹不存在。");
        System.exit(1);
    }

// 判断目的文件夹是否是文件夹，如果不是，则终止操作。
    if (!destDir.isDirectory()) {
        System.out.println("错误: 目的文件夹不是文件夹");
        System.exit(1);
    }

    // 检查源是否是目的的祖先（或自身）
    if (formerIsAncestor(source, destDir) ) {
        System.out.println("错误: 源文件夹 是目的文件夹 本身或其祖先，会出现无限循环拷贝");
        System.exit(1);
    }

// 如果所有情况正常，则可以用文件流拷贝所有内容，调用方法 copyTo() 方法。
    // 执行拷贝
    if (!copyTo(source, destDir)) {
         System.out.println("错误: 把源文件拷贝至目的文件时出现重名。");
        System.out.println("文件拷贝过程中出现异常，拷贝不成功或不完全成功。");
        System.exit(1);
    }
    else {
    // 如果拷贝成功，则反馈给用户；
        System.out.println("文件拷贝成功");
        }
    }
}

