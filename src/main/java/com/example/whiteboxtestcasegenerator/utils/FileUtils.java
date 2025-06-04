package com.example.whiteboxtestcasegenerator.utils;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    // 保存上传的文件到指定路径
    public static String saveFileToDisk(String fileName, byte[] content, String destinationDirectory) throws IOException {
        File dir = new File(destinationDirectory);
        if (!dir.exists()) {
            dir.mkdirs();  // 如果目标目录不存在，创建该目录
        }

        File destinationFile = new File(destinationDirectory + "/" + fileName);
        org.apache.commons.io.FileUtils.writeByteArrayToFile(destinationFile, content);
        return destinationFile.getAbsolutePath();
    }

    // 删除指定路径的文件
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();  // 删除文件
        }
        return false;
    }

    // 读取文件内容
    public static String readFileContent(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            return org.apache.commons.io.FileUtils.readFileToString(file, "UTF-8");
        }
        return null;
    }

    // 获取文件扩展名
    public static String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return "";
    }

    // 将文件移动到指定目标目录
    public static boolean moveFile(String sourcePath, String destinationPath) {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destinationPath);

        if (sourceFile.exists() && sourceFile.isFile()) {
            try {
                org.apache.commons.io.FileUtils.moveFile(sourceFile, destFile);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 复制文件到指定目录
    public static boolean copyFile(String sourcePath, String destinationPath) {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destinationPath);

        if (sourceFile.exists() && sourceFile.isFile()) {
            try {
                org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
