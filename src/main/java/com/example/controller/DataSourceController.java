package com.example.controller;

import com.example.pojo.Code;
import com.example.pojo.DataSource;
import com.example.pojo.User;
import com.example.service.DataSourceService;
import com.example.service.UserService;
import com.example.util.FileReaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DataSourceController {
    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/viewDataSource")
    public String viewDataSource(Model model, @RequestParam Integer userId, @RequestParam Integer dataSourceId,HttpSession session) {
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

        DataSource d=dataSourceService.findByDataSourceId(dataSourceId);


        if (d == null) {
            model.addAttribute("error", "代码未找到");
            return "error"; // 跳转到错误页面
        }
        model.addAttribute("dataSource", d);

        // 读取代码文件内容
        String dataContent = "";
        try {
            // 使用工具类读取文件内容
            dataContent = FileReaderUtil.readFile(d.getDataSourcePath());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "无法读取文件内容：" + e.getMessage());
        }
        model.addAttribute("dataContent", dataContent);

        return "viewDataSource";  // 源代码显示页面，在里面可以编辑并运行
    }



    @RequestMapping(value = "/dataUpload")
    public String toDataUpload(Model model, @RequestParam("userId") Integer userId, @RequestParam("dataFile") MultipartFile dataFile, @RequestParam("dataInfo")String dataInfo,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user", user);
        System.out.println(userId);
        List<DataSource> dataSourceList = new ArrayList<>();

        dataSourceList=dataSourceService.findByUserId(userId);

        model.addAttribute("dataSourceList",dataSourceList);
        DataSource d = new DataSource();

        if (!dataFile.isEmpty()) {
            // 获取上传文件的原始文件名
            String fileName = dataFile.getOriginalFilename();

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
                dataFile.transferTo(new File(filePath+fileName));

                d.setDataSourceInfo(dataInfo);
                d.setDataSourceName(fileName);
                d.setDataSourcePath(filePath+fileName);



                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String time = currentDateTime.format(formatter);
                d.setUpdateTime(time);

                d.setUserId(userId);


                String type = "";

                int lastIndex = fileName.lastIndexOf('.');
                if (lastIndex != -1 && lastIndex < fileName.length() - 1) {
                    type = fileName.substring(lastIndex);
                }

                d.setDataSourceType(type);
                dataSourceService.insert(d);

                System.out.println("上传成功");

            }catch(IOException e){
                e.printStackTrace();
                System.out.println("ssssssaaaaaaaaaaa");
                model.addAttribute("error", "文件上传失败！");
                //return "error"; // 跳转到错误页面
            }
            System.out.println("111");
        }
        System.out.println("222");

        return "redirect:/resourceManagement?userId=" + userId;
    }



    @RequestMapping(value = "/resourceManagement")
    public String toResourceManagement(Model model, @RequestParam Integer userId,HttpSession session) {

        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user",user);

        List<DataSource> dataSourceList = new ArrayList<>();

        dataSourceList=dataSourceService.findByUserId(userId);

        model.addAttribute("dataSourceList",dataSourceList);
        return "resourceManagement";
    }



    @RequestMapping(value = "/deleteDataSource")
    public String deleteDataSource(Model model, @RequestParam Integer userId, @RequestParam Integer dataSourceId,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user",user);
        if (dataSourceService.findByDataSourceId(dataSourceId) != null) {
            dataSourceService.delete(dataSourceId);
        }
        List<DataSource> dataSourceList = new ArrayList<>();

        dataSourceList=dataSourceService.findByUserId(userId);

        model.addAttribute("dataSourceList",dataSourceList);

        return "redirect:/resourceManagement?userId=" + userId;
    }

    @RequestMapping(value = "/dataSelect")
    public String dataSelect(Model model, @RequestParam Integer userId, @RequestParam String dataKey,HttpSession session) {
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user = userService.findUserByUserId(userId);
        model.addAttribute("user",user);

        List<DataSource> dataSourceList = new ArrayList<>();
        dataSourceList = dataSourceService.findDataByKey(dataKey);
        model.addAttribute("dataSourceList",dataSourceList);

        return "resourceManagement";
    }
}
