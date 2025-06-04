package com.example.dao;

import com.example.pojo.Code;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface CodeDao {
    // 通过codeId查询代码
    Code findCodeByCodeId(Integer codeId);

    // 通过userId查询代码列表
    List<Code> findCodesByUserId(Integer userId);
    Code findCodeByPath(String codePath);
    Code findfByName(String codeName);

    // 插入代码
    int insertCode(Code code);

    // 通过codeId删除代码
    int deleteCodeByCodeId(Integer codeId);

    // 更新代码信息
    int updateCode(Code code);

    List<Code> findByKey(String codeKey);



}
