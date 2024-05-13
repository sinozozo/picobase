package com.picobase.persistence.dbx;

import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.repository.PbDatabaseOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.picobase.persistence.dbx.DbxUtil.quoteColumnName;
import static com.picobase.persistence.dbx.DbxUtil.quoteTableName;


public abstract class BaseBuilder {

    private final PbDatabaseOperate dbOperate;

    protected BaseBuilder(PbDatabaseOperate dbOperate) {
        this.dbOperate = dbOperate;
    }


    public Query newQuery(String sql) {
        return new Query(dbOperate, sql);
    }


    public Query insert(String table, Map<String, Object> cols) {
        List<String> names = cols.keySet().stream().sorted().collect(Collectors.toList());

        var params = new HashMap<String, Object>();
        var columns = new ArrayList<String>(names.size());
        var values = new ArrayList<String>(names.size());
        names.forEach(name -> {
            columns.add(quoteColumnName(name));
            var value = cols.get(name);
            if (value instanceof Expression e) {
                values.add(e.build(params));
            } else {
                values.add(String.format(":p%s", params.size()));
                params.put(String.format("p%s", params.size()), value);
            }
        });

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", quoteTableName(table), String.join(",", columns), String.join(",", values));

        return this.newQuery(sql).bind(params);
    }

    public Query update(String table, Map<String, Object> cols, Expression where) {
        List<String> names = cols.keySet().stream().sorted().collect(Collectors.toList());

        var params = new HashMap<String, Object>();
        var lines = new ArrayList<String>(names.size());
        names.forEach(name -> {
            var value = cols.get(name);
            name = quoteColumnName(name);
            if (value instanceof Expression e) {
                lines.add(name + "=" + e.build(params));
            } else {
                lines.add(String.format("%s=:p%s", name, params.size()));
                params.put(String.format("p%s", params.size()), value);
            }
        });

        String sql = String.format("UPDATE %s SET %s", quoteTableName(table), String.join(", ", lines));
        if (where != null) {
            String w = where.build(params);
            if (w != null) {
                sql += " WHERE " + w;
            }
        }

        return this.newQuery(sql).bind(params);
    }

    public Query delete(String table, Expression where) {
        String sql = String.format("DELETE FROM %s", quoteTableName(table));
        var params = new HashMap<String, Object>();
        if (where != null) {
            String w = where.build(params);
            if (w != null) {
                sql += " WHERE " + w;
            }
        }

        return this.newQuery(sql).bind(params);
    }

}
