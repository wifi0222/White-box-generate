package com.example.dao;

import com.example.pojo.TestResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface TestResultDao {
    // 通过userId查询测试结果列表
    List<TestResult> findByUserId(Integer userId);

    // 通过testId查询测试结果
    TestResult findByTestId(Integer testId);

    // 通过codeId查询测试结果列表
    List<TestResult> findByCodeId(Integer codeId);

    // 通过testTime查询测试结果列表
    List<TestResult> findByTestTime(String testTime);

    // 通过testMethod查询测试结果列表
    List<TestResult> findByTestMethod(String testMethod);

    List<TestResult> findByCodeIdandMethodandUserId(Integer codeId, String testMethod, Integer userId);

    // 插入测试结果
    int insert(TestResult testResult);

    // 删除测试结果
    void delete(Integer testId);

    // 修改测试结果
    int update(TestResult testResult);

    List<TestResult> findTestByKey(String testKey);
    TestResult findByExamplePath(String path);

}
