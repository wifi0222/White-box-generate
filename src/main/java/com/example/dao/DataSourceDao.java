package com.example.dao;

import com.example.pojo.DataSource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface DataSourceDao {
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
