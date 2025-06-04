package com.example.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

/**
 * 多功能文件读取工具类，支持 Excel、Word、文本、Class 和 PDF 文件
 */
public class FileReaderUtil {

    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws Exception 读取失败时抛出异常
     */
    public static String readFile(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();

        // 判断文件类型并选择对应读取方式
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return readExcelFile(filePath);
        } else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            return readWordFile(filePath);
        } else if (fileName.endsWith(".pdf")) {
            return readPdfFile(filePath);
        } else if (fileName.endsWith(".class")) {
            return readClassFile(filePath);
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".bmp")) {
            return readImageFile(filePath);
        } else if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
            return readCompressedFile(filePath);
        } else {
            // 默认为普通文本文件，尝试多种编码读取
            return readTextFile(filePath);
        }
    }

    /**
     * 读取 Excel 文件内容
     */
    private static String readExcelFile(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            StringBuilder content = new StringBuilder();
            int sheetCount = workbook.getNumberOfSheets();

            for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                content.append("===== 工作表 ").append(sheetIndex + 1).append(": ").append(sheet.getSheetName()).append(" =====\n");

                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        content.append(getCellValue(cell)).append("\t");
                    }
                    content.append("\n");
                }
                content.append("\n");
            }
            return content.toString();
        }
    }

    /**
     * 获取单元格值（处理不同类型）
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return NumberToTextConverter.toText(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getRichStringCellValue().getString();
                } catch (Exception e) {
                    return cell.getNumericCellValue() + "";
                }
            default:
                return "";
        }
    }

    /**
     * 读取 Word 文件内容（支持 .doc 和 .docx）
     */
    private static String readWordFile(String filePath) throws Exception {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(filePath);
            String fileName = new File(filePath).getName();

            StringBuilder content = new StringBuilder();

            if (fileName.endsWith(".docx")) {
                // 处理 .docx 文件
                try (XWPFDocument document = new XWPFDocument(fis)) {
                    for (XWPFParagraph paragraph : document.getParagraphs()) {
                        content.append(paragraph.getText()).append("\n");
                    }
                }
            } else if (fileName.endsWith(".doc")) {
                // 处理 .doc 文件
                try (HWPFDocument document = new HWPFDocument(fis);
                     WordExtractor extractor = new WordExtractor(document)) {

                    // 获取所有段落
                    String[] paragraphs = extractor.getParagraphText();
                    for (String paragraph : paragraphs) {
                        // 移除末尾的换行符（Word 段落末尾有额外的换行）
                        content.append(paragraph.replaceAll("\\u000D\\u000A$", "")).append("\n");
                    }
                }
            } else {
                throw new IllegalArgumentException("不支持的 Word 文件格式: " + filePath);
            }

            return content.toString();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * 读取普通文本文件（尝试 UTF-8 和 GBK 编码）
     */
    private static String readTextFile(String filePath) throws Exception {
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

        // 先尝试 UTF-8 编码
        try {
            return new String(fileContent, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // 再尝试 GBK 编码
            try {
                return new String(fileContent, "GBK");
            } catch (UnsupportedEncodingException ex) {
                // 其他编码尝试（可选）
                return "未知编码格式，原始字节长度: " + fileContent.length;
            }
        }
    }

    /**
     * 读取 PDF 文件内容（手动关闭资源）
     */
    private static String readPdfFile(String filePath) throws Exception {
        PdfReader reader = null;
        try {
            reader = new PdfReader(filePath);
            int pageCount = reader.getNumberOfPages();
            StringBuilder content = new StringBuilder();

            for (int i = 1; i <= pageCount; i++) {
                content.append("===== 第 ").append(i).append(" 页 =====\n");
                content.append(PdfTextExtractor.getTextFromPage(reader, i)).append("\n\n");
            }
            return content.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 读取 Class 文件内容（提取类结构信息）
     */
    private static String readClassFile(String filePath) throws Exception {
        byte[] classBytes = Files.readAllBytes(Paths.get(filePath));
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();

        // 解析 Class 文件
        cr.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);

        StringBuilder content = new StringBuilder();
        content.append("===== Class 文件信息 =====\n");
        content.append("类名: ").append(cn.name.replace('/', '.')).append("\n");
        content.append("父类: ").append(cn.superName.replace('/', '.')).append("\n");

        // 输出接口
        if (!cn.interfaces.isEmpty()) {
            content.append("实现的接口: ");
            for (String iface : cn.interfaces) {
                content.append(iface.replace('/', '.')).append(", ");
            }
            content.append("\n");
        }

        // 输出访问标志
        content.append("访问标志: ");
        if ((cn.access & Opcodes.ACC_PUBLIC) != 0) content.append("public ");
        if ((cn.access & Opcodes.ACC_FINAL) != 0) content.append("final ");
        if ((cn.access & Opcodes.ACC_ABSTRACT) != 0) content.append("abstract ");
        content.append("\n\n");

        // 输出字段（简化处理）
        content.append("===== 字段 =====\n");
        if (cn.fields != null) {
            for (org.objectweb.asm.tree.FieldNode field : cn.fields) {
                content.append("  ");
                if ((field.access & Opcodes.ACC_PUBLIC) != 0) content.append("public ");
                if ((field.access & Opcodes.ACC_PRIVATE) != 0) content.append("private ");
                if ((field.access & Opcodes.ACC_PROTECTED) != 0) content.append("protected ");
                if ((field.access & Opcodes.ACC_STATIC) != 0) content.append("static ");
                if ((field.access & Opcodes.ACC_FINAL) != 0) content.append("final ");

                content.append(field.desc).append(" ").append(field.name).append("\n");
            }
        } else {
            content.append("  无字段\n");
        }
        content.append("\n");

        // 输出方法
        content.append("===== 方法 =====\n");
        if (cn.methods != null) {
            for (MethodNode method : cn.methods) {
                content.append("  ");
                if ((method.access & Opcodes.ACC_PUBLIC) != 0) content.append("public ");
                if ((method.access & Opcodes.ACC_PRIVATE) != 0) content.append("private ");
                if ((method.access & Opcodes.ACC_PROTECTED) != 0) content.append("protected ");
                if ((method.access & Opcodes.ACC_STATIC) != 0) content.append("static ");
                if ((method.access & Opcodes.ACC_FINAL) != 0) content.append("final ");
                if ((method.access & Opcodes.ACC_ABSTRACT) != 0) content.append("abstract ");

                content.append(method.name).append(method.desc).append("\n");
            }
        } else {
            content.append("  无方法\n");
        }

        return content.toString();
    }

    /**
     * 读取图片文件内容（提取元数据和缩略图）
     */
    private static String readImageFile(String filePath) throws Exception {
        File file = new File(filePath);
        StringBuilder content = new StringBuilder();

        // 基本文件信息
        content.append("===== 图片文件信息 =====\n");
        content.append("文件名: ").append(file.getName()).append("\n");
        content.append("文件大小: ").append(file.length()).append(" 字节\n");
        content.append("修改时间: ").append(new Date(file.lastModified())).append("\n");

        try (FileInputStream fis = new FileInputStream(file)) {
            // 读取图片元数据
            BufferedImage image = ImageIO.read(fis);
            if (image != null) {
                content.append("图片宽度: ").append(image.getWidth()).append(" 像素\n");
                content.append("图片高度: ").append(image.getHeight()).append(" 像素\n");
                content.append("图片格式: ").append(getImageFormat(file.getName())).append("\n");

                // 生成缩略图的 Base64 编码（可选）
                content.append("\n===== 缩略图数据 =====\n");
                content.append("data:image/").append(getImageFormat(file.getName())).append(";base64,");

                // 调整图片大小为缩略图
                BufferedImage thumbnail = scaleImage(image, 200, 200);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(thumbnail, getImageFormat(file.getName()), baos);
                byte[] imageBytes = baos.toByteArray();

                content.append(Base64.getEncoder().encodeToString(imageBytes));
            } else {
                content.append("无法解析图片内容\n");
            }
        }

        return content.toString();
    }

    /**
     * 获取图片格式（从文件名推断）
     */
    private static String getImageFormat(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        if (lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg")) {
            return "jpg";
        } else if (lowerCaseName.endsWith(".png")) {
            return "png";
        } else if (lowerCaseName.endsWith(".gif")) {
            return "gif";
        } else if (lowerCaseName.endsWith(".bmp")) {
            return "bmp";
        } else {
            return "unknown";
        }
    }

    /**
     * 调整图片大小
     */
    private static BufferedImage scaleImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }


    /**
     * 读取压缩包文件内容（列出文件结构）
     */
    private static String readCompressedFile(String filePath) throws Exception {
        File file = new File(filePath);
        StringBuilder content = new StringBuilder();

        content.append("===== 压缩包内容 =====\n");
        content.append("文件名: ").append(file.getName()).append("\n");
        content.append("文件大小: ").append(file.length()).append(" 字节\n");

        if (filePath.toLowerCase().endsWith(".zip")) {
            // 处理 ZIP 文件
            try (ZipFile zipFile = new ZipFile(file)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    content.append(entry.isDirectory() ? "[目录] " : "[文件] ");
                    content.append(entry.getName()).append("\n");
                    if (!entry.isDirectory()) {
                        content.append("  大小: ").append(entry.getSize()).append(" 字节\n");
                        content.append("  压缩后: ").append(entry.getCompressedSize()).append(" 字节\n");
                    }
                }
            }
        } else if (filePath.toLowerCase().endsWith(".rar")) {
            // 处理 RAR 文件
            try (Archive archive = new Archive(new FileInputStream(file))) {
                if (archive.isEncrypted()) {
                    content.append("警告: RAR 文件已加密，无法列出内容\n");
                } else {
                    FileHeader fileHeader;
                    while ((fileHeader = archive.nextFileHeader()) != null) {
                        content.append(fileHeader.isDirectory() ? "[目录] " : "[文件] ");
                        content.append(fileHeader.getFileNameW().isEmpty()
                                ? fileHeader.getFileNameString()
                                : fileHeader.getFileNameW()).append("\n");

                        if (!fileHeader.isDirectory()) {
                            content.append("  大小: ").append(fileHeader.getUnpSize()).append(" 字节\n");
                        }
                    }
                }
            }
        } else {
            content.append("不支持的压缩格式\n");
        }

        return content.toString();
    }

}