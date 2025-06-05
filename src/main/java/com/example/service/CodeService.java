package com.example.service;


import com.example.pojo.Code;

import java.util.List;

public interface CodeService {
    // 通过codeId查询代码
    Code findCodeByCodeId(Integer codeId);
    Code findByName(String codeName);


    Code findCodeByPath(String codePath);
    Code findCodeByPathandUserId(String codePath,Integer userId);

    // 通过userId查询代码列表
    List<Code> findCodesByUserId(Integer userId);

    // 插入代码
    int insertCode(Code code);

    // 通过codeId删除代码
    int deleteCodeByCodeId(Integer codeId);

    // 更新代码信息
    int updateCode(Code code);
    List<Code> findCodeByKey(String codeKey);
}