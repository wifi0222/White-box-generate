package com.example.service;

import com.example.pojo.DataSource;

import java.util.List;

public interface DataSourceService {
    // 通过userId查询数据源列表
    List<DataSource> findByUserId(Integer userId);

    // 通过dataSourceId查询数据源
    DataSource findByDataSourceId(Integer dataSourceId);

    // 通过dataSourceName查询数据源列表
    List<DataSource> findByDataSourceName(String dataSourceName);

    // 通过dataSourceType查询数据源列表
    List<DataSource> findByDataSourceType(String dataSourceType);

    // 删除数据源
    int delete(Integer dataSourceId);

    // 更新数据源信息
    int update(DataSource dataSource);

    // 插入数据源
    int insert(DataSource dataSource);
    List<DataSource> findDataByKey(String dataKey);
}