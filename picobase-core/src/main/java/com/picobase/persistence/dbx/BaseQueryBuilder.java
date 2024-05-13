package com.picobase.persistence.dbx;

import cn.hutool.core.util.StrUtil;
import com.picobase.persistence.dbx.expression.Expression;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.picobase.persistence.dbx.DbxUtil.*;


public class BaseQueryBuilder implements QueryBuilder {
    private static final Pattern selectRegex = Pattern.compile("(?i:\\s+as\\s+|\\s+)([\\w\\-_\\.]+)$"); //该字符串必须以空格符开头，然后是至少一个空格符，然后是 "ASC" 或 "DESC"（不区分大小写）


    /**
     * BuildSelect generates a SELECT clause from the given selected column names.
     *
     * @param cols
     * @param distinct
     * @param option
     * @return
     */
    @Override
    public String buildSelect(List<String> cols, boolean distinct, String option) {
        StringBuilder builder = new StringBuilder("SELECT ");
        if (distinct) {
            builder.append("DISTINCT ");
        }
        if (StrUtil.isNotEmpty(option)) {
            builder.append(option).append(" ");
        }
        if (cols.size() == 0) {
            builder.append("*");
            return builder.toString();
        }

        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            Matcher matcher = selectRegex.matcher(cols.get(i));
            if (!matcher.find()) {
                builder.append(quoteColumnName(cols.get(i)));
            } else {
                String col = cols.get(i).substring(0, matcher.start());
                String alias = matcher.group(1);
                builder.append(quoteColumnName(col))
                        .append(" AS ")
                        .append(quoteSimpleColumnName(alias));
            }
        }

        return builder.toString();
    }


    @Override
    public String buildFrom(List<String> tables) {
        if (tables.size() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder("FROM ");
        for (int i = 0; i < tables.size(); i++) {
            String table = quoteTableNameAndAlias(tables.get(i));
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(table);
        }

        return builder.toString();
    }

    private String quoteTableNameAndAlias(String table) {
        Matcher matcher = selectRegex.matcher(table);
        if (!matcher.find()) {
            return quoteTableName(table);
        }

        String tableName = table.substring(0, matcher.start());
        String alias = matcher.group(1);
        return quoteTableName(tableName) + " " + quoteSimpleTableName(alias);
    }

    @Override
    public String buildJoin(List<JoinInfo> joins, Map<String, Object> params) {
        if (joins.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < joins.size(); i++) {
            JoinInfo join = joins.get(i);
            builder.append(join.getJoin()).append(" ");
            builder.append(quoteTableNameAndAlias(join.getTable()));

            String on = "";
            if (join.getOn() != null) {
                on = join.getOn().build(params);
            }

            if (!on.isEmpty()) {
                builder.append(" ON ").append(on);
            }

            if (i < joins.size() - 1) {
                builder.append(" "); // Space between joins
            }
        }

        return builder.toString();
    }

    @Override
    public String buildWhere(Expression where, Map<String, Object> params) {
        if (where != null) {
            String condition = where.build(params);
            if (!condition.isEmpty()) {
                return "WHERE " + condition;
            }
        }
        return "";
    }

    @Override
    public String buildGroupBy(List<String> cols) {
        if (cols.isEmpty()) {
            return "";
        }

        return "GROUP BY " + cols.stream()
                .map(DbxUtil::quoteColumnName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String buildHaving(Expression having, Map<String, Object> params) {
        if (having != null) {
            String condition = having.build(params);
            if (!condition.isEmpty()) {
                return "HAVING " + condition;
            }
        }
        return "";
    }

    @Override
    public String buildOrderByAndLimit(String sql, List<String> cols, long limit, long offset) {
        StringBuilder builder = new StringBuilder(sql);

        // Append ORDER BY clause
        String orderBy = buildOrderBy(cols);
        if (!orderBy.isEmpty()) {
            builder.append(" ").append(orderBy);
        }

        // Append LIMIT clause
        String limitClause = buildLimit(limit, offset);
        if (!limitClause.isEmpty()) {
            builder.append(" ").append(limitClause);
        }

        return builder.toString();
    }

    private static final Pattern ORDER_REGEX = Pattern.compile("\\s+((?i)ASC|DESC)$");

    public String buildOrderBy(List<String> cols) {
        if (cols.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder("ORDER BY ");
        for (int i = 0; i < cols.size(); i++) {
            String col = cols.get(i);
            if (i > 0) {
                builder.append(", ");
            }

            Matcher matcher = ORDER_REGEX.matcher(col);
            if (!matcher.find()) {
                builder.append(quoteColumnName(col));
            } else {
                String columnName = col.substring(0, matcher.start());
                String direction = matcher.group(1).toUpperCase();
                builder.append(quoteColumnName(columnName)).append(" ").append(direction);
            }
        }

        return builder.toString();
    }

    public String buildLimit(long limit, long offset) {
        if (limit < 0 && offset > 0) {
            // Most DBMS require LIMIT when OFFSET is present
            limit = Long.MAX_VALUE; // Use Long.MAX_VALUE for maximum value
        }

        StringBuilder builder = new StringBuilder();
        if (limit >= 0) {
            builder.append("LIMIT ").append(limit);
        }
        if (offset > 0) {
            builder.append(" OFFSET ").append(offset);
        }
        return builder.toString();
    }

    @Override
    public String buildUnion(List<UnionInfo> unions, Map<String, Object> params) {
        if (unions.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < unions.size(); i++) {
            if (i > 0) {
                builder.append(" ");
            }

            UnionInfo union = unions.get(i);

            // Merge query parameters into the main params object
            for (String key : union.getQuery().getParams().keySet()) {
                params.put(key, union.getQuery().getParams().get(key));
            }

            String unionType = "UNION";
            if (union.isAll()) {
                unionType = "UNION ALL";
            }

            builder.append(String.format("%s (%s)", unionType, union.getQuery().getSql()));
        }

        return builder.toString();
    }
}
