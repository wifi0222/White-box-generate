package com.example.service.impl;

import com.example.pojo.DataSource;
import com.example.service.DataSourceService;
import com.example.dao.DataSourceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceDao dataSourceDao;

    @Override
    public List<DataSource> findByUserId(Integer userId) {
        return dataSourceDao.findByUserId(userId);
    }

    @Override
    public DataSource findByDataSourceId(Integer dataSourceId) {
        return dataSourceDao.findByDataSourceId(dataSourceId);
    }

    @Override
    public List<DataSource> findByDataSourceName(String dataSourceName) {
        return dataSourceDao.findByDataSourceName(dataSourceName);
    }

    @Override
    public List<DataSource> findByDataSourceType(String dataSourceType) {
        return dataSourceDao.findByDataSourceType(dataSourceType);
    }

    @Override
    public int delete(Integer dataSourceId) {
        return dataSourceDao.delete(dataSourceId);
    }

    @Override
    public int update(DataSource dataSource) {
        return dataSourceDao.update(dataSource);
    }

    @Override
    public int insert(DataSource dataSource) {
        return dataSourceDao.insert(dataSource);
    }

    @Override
    public List<DataSource> findDataByKey(String dataKey) {
        return dataSourceDao.findDataByKey(dataKey);
    }
}
