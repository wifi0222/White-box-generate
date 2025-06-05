package com.example.service.impl;

import com.example.pojo.Code;
import com.example.service.CodeService;
import com.example.dao.CodeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeServiceImpl implements CodeService {

    @Autowired
    private CodeDao codeDao;

    @Override
    public Code findCodeByCodeId(Integer codeId) {
        return codeDao.findCodeByCodeId(codeId);
    }

    @Override
    public Code findByName(String codeName) {
        return codeDao.findfByName(codeName);
    }

    @Override
    public Code findCodeByPath(String codePath) {
        return codeDao.findCodeByPath(codePath);
    }

    @Override
    public Code findCodeByPathandUserId(String codePath,Integer userId) {
        return codeDao.findCodeByPathandUserId(codePath,userId);
    }

    @Override
    public List<Code> findCodesByUserId(Integer userId) {
        return codeDao.findCodesByUserId(userId);
    }

    @Override
    public int insertCode(Code code) {
        return codeDao.insertCode(code);
    }

    @Override
    public int deleteCodeByCodeId(Integer codeId) {
        return codeDao.deleteCodeByCodeId(codeId);
    }

    @Override
    public int updateCode(Code code) {
        return codeDao.updateCode(code);
    }

    @Override
    public List<Code> findCodeByKey(String codeKey) {
        return codeDao.findByKey(codeKey);
    }
}