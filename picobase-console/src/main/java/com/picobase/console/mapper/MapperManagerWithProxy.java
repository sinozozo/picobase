package com.picobase.console.mapper;

import com.picobase.console.PbConsoleManager;
import com.picobase.persistence.mapper.PbMapper;
import com.picobase.persistence.mapper.PbMapperManager;

public class MapperManagerWithProxy extends PbMapperManager {

    /**
     * 重写 findMapper 方法 ， 提供代理能力，主要用于sql日志的输入
     * @param dataSource the datasource.
     * @param clazz  model clazz.
     * @return
     * @param <R>
     * @param <T>
     */
    public <R extends PbMapper,T> R findMapper(String dataSource,Class<T> clazz){
        if (PbConsoleManager.getConfig().isSqlLogEnable()) {
            return ProxyMapper.createSingleProxy(super.findMapper(dataSource, clazz));
        }
        return super.findMapper(dataSource, clazz);
    }
}
