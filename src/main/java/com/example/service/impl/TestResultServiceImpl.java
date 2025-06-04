package com.example.service.impl;

import com.example.pojo.TestResult;
import com.example.service.TestResultService;
import com.example.dao.TestResultDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TestResultServiceImpl implements TestResultService {

    @Autowired
    private TestResultDao testResultDao;

    @Override
    public List<TestResult> findByUserId(Integer userId) {
        return testResultDao.findByUserId(userId);
    }

    @Override
    public TestResult findByTestId(Integer testId) {
        return testResultDao.findByTestId(testId);
    }

    @Override
    public List<TestResult> findByCodeId(Integer codeId) {
        return testResultDao.findByCodeId(codeId);
    }

    @Override
    public List<TestResult> findByTestTime(String testTime) {
        return testResultDao.findByTestTime(testTime);
    }

    @Override
    public List<TestResult> findByTestMethod(String testMethod) {
        return testResultDao.findByTestMethod(testMethod);
    }

    @Override
    public List<TestResult> findByCodeIdandMethod(Integer codeId, String testMethod) {
        return testResultDao.findByCodeIdandMethod(codeId, testMethod);
    }

    @Override
    public int insert(TestResult testResult) {
        return testResultDao.insert(testResult);
    }

    @Override
    public void delete(Integer testId) {
        testResultDao.delete(testId);
    }

    @Override
    public int update(TestResult testResult) {
        return testResultDao.update(testResult);
    }

    @Override
    public List<TestResult> findTestByKey(String testKey) {
        return testResultDao.findTestByKey(testKey);
    }

    @Override
    public TestResult findByExamplePath(String path) {
        return testResultDao.findByExamplePath(path);
    }
}