
package com.picobase.persistence.mapper;

import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.exception.PbException;
import com.picobase.log.PbLog;
import com.picobase.spi.PbServiceLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.picobase.error.PbErrorCode.CODE_13000;

/**
 * DataSource Plugin PbMapper Management.
 *
 **/

public class PbMapperManager {

    private static final PbLog LOGGER = PbManager.getLog();
    
    public static final Map<String, Map<Class, PbMapper>> MAPPER_SPI_MAP = new HashMap<>();
    


    public static final String DEFAULT_DATA_SOURCE = "default";
    
    public PbMapperManager() {

    }
    

    /**
     * The init method.
     */
    public synchronized void loadInitial() {
        Collection<PbMapper> mappers = PbServiceLoader.load(PbMapper.class);
        for (PbMapper mapper : mappers) {
            putMapper(mapper);
            LOGGER.info("[PbMapperManager] Load PbMapper({}) datasource({}) modelClass({}) tableName({}) successfully.",
                    mapper.getClass(), mapper.getDataSource(), mapper.getModelClass(),mapper.getTableName());
        }
    }
    
    /**
     * To join mapper in MAPPER_SPI_MAP.
     * @param mapper The mapper you want join.
     */
    public static synchronized void join(PbMapper mapper) {
        if (Objects.isNull(mapper)) {
            return;
        }
        putMapper(mapper);
        LOGGER.info("[PbMapperManager] join successfully.");
    }
    
    private static void putMapper(PbMapper mapper) {
        Map<Class, PbMapper> mapperMap = MAPPER_SPI_MAP.computeIfAbsent(mapper.getDataSource(), key ->
                new HashMap<>(16));
        mapperMap.putIfAbsent(mapper.getModelClass(), mapper);
    }

    /**
     * Get the mapper by table name from default.
     *
     * @param clazz  model class.
     * @return mapper.
     */
    public <R extends PbMapper,T> R findMapper(Class<T> clazz){
        return findMapper(DEFAULT_DATA_SOURCE, clazz);
    }

    /**
     * Get the mapper by table name.
     *
     * @param clazz  model clazz.
     * @param dataSource the datasource.
     * @return mapper.
     */
    public <R extends PbMapper,T> R findMapper(String dataSource, Class<T> clazz) {
        LOGGER.debug("[PbMapperManager] findMapper dataSource: {}, ModelClass: {}", dataSource, clazz);
        if (StrUtil.isBlank(dataSource) || clazz==null) {
            throw new PbException("dataSource or ModelClass is null");
        }
        Map<Class, PbMapper> tableMapper = MAPPER_SPI_MAP.get(dataSource);
        if (Objects.isNull(tableMapper)) {
            throw new PbException(CODE_13000,
                    "[PbMapperManager] Failed to find the datasource,dataSource:" + dataSource);
        }
        PbMapper mapper = tableMapper.get(clazz);
        if (Objects.isNull(mapper)) {
            throw new PbException(CODE_13000,
                    "[PbMapperManager] Failed to find the table ,ModelClass:" + clazz);
        }

        return (R) mapper;
    }
}
