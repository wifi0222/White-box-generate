package com.example.dao;

import com.example.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {
    // 通过userTel查找用户
    User findUserByUserTel(String userTel);

    // 更新用户信息
    int updateUser(User user);

    // 插入用户
    int insertUser(User user);

    // 删除用户
    int deleteUser(Integer userId);

    // 通过userId查找用户
    User findUserByUserId(Integer userId);
}