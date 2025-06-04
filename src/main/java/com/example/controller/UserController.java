package com.example.controller;

import com.example.pojo.Code;
import com.example.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.pojo.User;
import com.example.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private CodeService codeService;

    @RequestMapping(value = "operation")
    public String toOperation() {
        return "operation";
    }
    @RequestMapping(value = "register")
    public String toRegister() {
        return "register";
    }
    @RequestMapping(value = "login")
    public String toLogin() {
        return "login";
    }


    @PostMapping("/registerCheck")
    public String register(@RequestParam("userName") String userName, @RequestParam("userTel") String userTel, @RequestParam("password") String password) {
        // 检查数据库中是否已存在该用户
        User existingUser = userService.findUserByUserTel(userTel);
        if (existingUser != null) {
            // 用户已存在，返回错误页面
            return "registerError";
        }

        // 用户不存在，插入新用户
        User newUser = new User();
        newUser.setUserName(userName);
        newUser.setUserTel(userTel);
        newUser.setPassword(password);
        userService.insertUser(newUser);

        // 注册成功，返回登录页面
        return "login";
    }

    @PostMapping("/loginCheck")
    public String login(@RequestParam("userTel") String userTel, @RequestParam("password") String password, Model model,HttpSession session) {
        // 根据 userTel 查找用户
        User user = userService.findUserByUserTel(userTel);

        if (user == null ||!user.getPassword().equals(password)) {
            // 用户不存在或密码错误，返回登录错误页面
            return "loginerror";
        }
        // 登录成功，返回代码管理页面
        model.addAttribute("user",user);
        // 将用户信息存入 session
        session.setAttribute("user", user);  // 保存用户信息到 sessions
        List<Code> codeList = new ArrayList<>();
        codeList = codeService.findCodesByUserId(user.getUserId());
        model.addAttribute("codeList",codeList);
        return "codeManagement";
    }
    @RequestMapping("/modifyPersonalInfo")
    public String modify(@RequestParam("userName")String userName,@RequestParam("userTel")String userTel,@RequestParam("userInfo")String userInfo,
                         @RequestParam("userId")Integer userId,Model model,HttpSession session){
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }
        User u =userService.findUserByUserId(userId);
        u.setUserName(userName);
        u.setUserTel(userTel);
        u.setUserInfo(userInfo);
        userService.updateUser(u);
        User user=userService.findUserByUserId(userId);
        model.addAttribute("user",user);
        return "personInfo";
    }
    @RequestMapping("/personalInfo")
    public String toPersonalInfo(@RequestParam("userId")Integer userId,Model model,HttpSession session){
        // 检查 session 中是否存有用户信息
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !sessionUser.getUserId().equals(userId)) {
            // 如果 session 中没有用户信息，表示用户未登录，重定向到登录页面
            return "redirect:/login";
        }

        User user=userService.findUserByUserId(userId);
        model.addAttribute("user",user);
        return "personInfo";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // 销毁 session
        session.invalidate();
        return "redirect:/login";
    }

}    