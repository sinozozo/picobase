package com.picobase.persistence.mapper;

import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.repository.PbRowMapper;

import java.util.List;

import static com.picobase.persistence.mapper.PbMapperManager.DEFAULT_DATA_SOURCE;

/**
 * The parent class of the all mappers.
 *
 **/

public interface PbMapper {

     PbRowMapper getPbRowMapper();
    /**
     * The select method contains columns and where params.
     * @param columns The columns
     * @param where The where params
     * @return The sql of select
     */
    String select(List<String> columns, List<String> where);
    
    /**
     * The insert method contains columns.
     * @param columns The columns
     * @return The sql of insert
     */
    String insert(List<String> columns);
    
    /**
     * The update method contains columns and where params.
     * @param columns The columns
     * @param where The where params
     * @return The sql of update
     */
    String update(List<String> columns, List<String> where);
    
    /**
     * The delete method contains.
     * @param params The params
     * @return The sql of delete
     */
    String delete(List<String> params);
    
    /**
     * The count method contains where params.
     *
     * @param where The where params
     * @return The sql of count
     */
    String count(List<String> where);
    
    /**
     * Get the name of table.
     * @return The name of table.
     */
    String getTableName();
    
    /**
     * Get the datasource name.
     * @return The name of datasource.
     */
   default String getDataSource(){
       return DEFAULT_DATA_SOURCE;
   }
    
    /**
     * Get config_info table primary keys name.
     * The old default value: Statement.RETURN_GENERATED_KEYS
     * The new default value: new String[]{"id"}
     * @return an array of column names indicating the columns
     */
    String[] getPrimaryKeyGeneratedKeys();


    <T> Class<T> getModelClass();

    SelectQuery modelQuery();
}