package com.picobase.model;

import java.util.Map;

/**
 * ColumnValueMapper defines an interface for custom db model data serialization.
 */
public interface ColumnValueMapper {

    /**
     * return the data to be used when persisting the model
     */
    Map<String, Object> columnValueMap();
}
