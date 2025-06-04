
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>请登录</title>
    <style>
        /* 全局样式，使用渐变背景色 */
        body {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            font-family: Arial, sans-serif;
            background: linear-gradient(to top, #fff1eb 0%, #ace0f9 100%);
            overflow: hidden;
        }

        /* 登录容器样式 */
        .login__container {
            position: relative;
            width: 320px;
            background: rgba(255, 255, 255, 0.85); /* 半透明背景 */
            border-radius: 15px;
            padding: 40px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
            text-align: center;
            color: #333;
        }

        /* 标题样式 */
        .login__title {
            font-size: 24px;
            color: #333;
            font-weight: bold;
            margin-bottom: 20px;
        }

        /* 输入框和图标样式 */
        .login__field {
            position: relative;
            padding: 15px 0;
        }

        .login__icon {
            position: absolute;
            top: 45%; /* 将图标垂直居中于输入框 */
            color: #ace0f9;
            font-size: 18px;
            transform: translateY(-50%);
        }

        .login__input {
            width: 100%;
            padding: 10px 10px 10px 30px;
            background: none;
            border: none;
            border-bottom: 2px solid #ace0f9;
            color: #333;
            font-size: 16px;
            transition: border-color 0.2s;
        }

        .login__input:focus {
            outline: none;
            border-bottom-color: #80b8e6; /* 聚焦时边框颜色 */
        }

        /* 登录按钮样式 */
        .login__submit {
            background: linear-gradient(to right, #80b8e6, #ace0f9);
            width:120px;
            color: #333;
            font-size: 16px;
            padding: 12px;
            border-radius: 26px;
            border: none;
            cursor: pointer;
            transition: background 0.3s;
            font-weight: bold;
            margin-top:15px;
        }

        .login__submit:hover {
            background: linear-gradient(to right, #ace0f9, #80b8e6);
            color: #333;
        }

        /* 注册按钮样式，基于登录按钮样式 */
        .register__submit {
            background: linear-gradient(to right, #80b8e6, #ace0f9);
            width:220px;
            color: #333;
            font-size: 16px;
            padding: 12px;
            border-radius: 26px;
            border: none;
            cursor: pointer;
            text-decoration: none;
            transition: background 0.3s;
            font-weight: bold;
            display: inline-block; /* 使得按钮可以与其他内容在同一行显示 */
            text-align: center; /* 文字居中 */
            margin-top: 15px; /* 与登录按钮保持一致的外边距 */
        }

        .register__submit:hover {
            background: linear-gradient(to right, #ace0f9, #80b8e6);
            text-decoration: none;
            color: #333;
        }

        /* 背景形状 */
        .screen__background {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: -1;
            overflow: hidden;
        }

        .screen__background__shape {
            position: absolute;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.3);
            animation: floatShapes 20s infinite ease-in-out;
        }

        .screen__background__shape1 {
            width: 200px;
            height: 200px;
            top: -50px;
            right: -50px;
        }

        .screen__background__shape2 {
            width: 150px;
            height: 150px;
            bottom: 50px;
            left: -50px;
        }

        .screen__background__shape3 {
            width: 250px;
            height: 250px;
            bottom: -50px;
            right: -50px;
        }

        .screen__background__shape4 {
            width: 100px;
            height: 100px;
            top: 50px;
            left: -50px;
        }

        @keyframes floatShapes {
            0% {
                transform: translateY(0) translateX(0);
            }
            50% {
                transform: translateY(-10px) translateX(10px);
            }
            100% {
                transform: translateY(0) translateX(0);
            }
        }
    </style>
</head>
<body>
<div class="login__container">
    <h2 class="login__title">请登录</h2>
    <form method="post" action="GetLogin">
        <div class="login__field">
            <i class="login__icon">&#128100;</i>
            <input type="text" name="userName" class="login__input" placeholder="请输入用户名" required>
        </div>
        <div class="login__field">
            <i class="login__icon">&#128222;</i>
            <input type="text" name="userTel" class="login__input" placeholder="请输入联系电话" required>
        </div>
        <div class="login__field">
            <i class="login__icon">&#128274;</i>
            <input type="password" name="password" class="login__input" placeholder="请输入密码" required>
        </div>
        <button type="submit" class="login__submit">登录</button>
        <button type="reset" class="login__submit">重置</button>
        <a href="register.jsp" class="register__submit">还未注册？去注册</a>
    </form>
</div>

<div class="screen__background">
    <span class="screen__background__shape screen__background__shape1"></span>
    <span class="screen__background__shape screen__background__shape2"></span>
    <span class="screen__background__shape screen__background__shape3"></span>
    <span class="screen__background__shape screen__background__shape4"></span>
</div>
</body>
</html>
