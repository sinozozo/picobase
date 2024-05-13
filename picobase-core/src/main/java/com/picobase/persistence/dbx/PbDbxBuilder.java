package com.picobase.persistence.dbx;


import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.repository.PbDatabaseOperate;

import java.util.Map;

public interface PbDbxBuilder {

    SelectQuery select(String... field);

    QueryBuilder QueryBuilder();

    static PbDbxBuilder newMysqlBuilder(PbDatabaseOperate operate) {
        return new MysqlPbDbxBuilder(operate);
    }

    Query newQuery(String sql);


    /**
     * Insert creates a Query that represents an INSERT SQL statement.
     * The keys of cols are the column names, while the values of cols are the corresponding column
     * values to be inserted.
     */
    Query insert(String table, Map<String, Object> dataMap);

    /**
     * Update creates a Query that represents an UPDATE SQL statement.
     * The keys of cols are the column names, while the values of cols are the corresponding new column
     * values. If the "where" expression is nil, the UPDATE SQL statement will have no WHERE clause
     * (be careful in this case as the SQL statement will update ALL rows in the table).
     */
    Query update(String table, Map<String, Object> dataMap, Expression where);

    /**
     * Delete creates a Query that represents a DELETE SQL statement.
     * If the "where" expression is nil, the DELETE SQL statement will have no WHERE clause
     * (be careful in this case as the SQL statement will delete ALL rows in the table).
     */
    Query delete(String table, Expression where);

}
