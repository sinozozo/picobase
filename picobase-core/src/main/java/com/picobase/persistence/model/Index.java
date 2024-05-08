package com.picobase.persistence.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.util.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.picobase.persistence.dbx.DbxUtil.quoteSimpleColumnName;
import static com.picobase.persistence.dbx.DbxUtil.quoteSimpleTableName;

/**
 * Index represents a single parsed SQL CREATE INDEX expression.
 */
public class Index {

    private Boolean unique;

    private Boolean optional;

    private String schemaName;

    private String indexName;

    private String tableName;

    private List<IndexColumn> columns;

    private String where;


    public final static Pattern indexRegex = Pattern.compile("(?im)create\\s+(unique\\s+)?\\s*index\\s*(if\\s+not\\s+exists\\s+)?(\\S*)\\s+on\\s+(\\S*)\\s*\\(([\\s\\S]*)\\)(?:\\s*where\\s+([\\s\\S]*))?");
    public final static Pattern indexColumnRegex = Pattern.compile("(?im)^([\\s\\S]+?)(?:\\s+collate\\s+([\\w]+))?(?:\\s+(asc|desc))?$");


    // IsValid checks if the current Index contains the minimum required fields to be considered valid.
    public boolean isValid() {
        return !this.indexName.equals("") && !this.tableName.equals("") && !this.columns.isEmpty();
    }


    // Build returns a "CREATE INDEX" SQL string from the current index parts.
    //
    // Returns empty string if idx.IsValid() is false.
    public String build() {
        if (!this.isValid()) {
            return "";
        }

        StringBuilder str = new StringBuilder("CREATE ");
        if (this.getUnique()) {
            str.append("UNIQUE ");
        }

        str.append("INDEX ");

        if (this.getOptional()) {
            str.append("IF NOT EXISTS ");
        }

        if (StrUtil.isNotBlank(this.getSchemaName())) {
            // str.append("`");
            str.append(quoteSimpleColumnName(this.getSchemaName()));
            // str.append("`.");
        }

        // str.append("`");
        str.append(quoteSimpleColumnName(this.getIndexName()));
        // str.append("` ");

        str.append(" ON ");
        str.append(quoteSimpleTableName(this.getTableName()));
        str.append(" (");

        if (CollUtil.isNotEmpty(this.getColumns())) {
            str.append("\n  ");
        }

        boolean hasCol = false;
        for (IndexColumn col : this.getColumns()) {
            String trimmedColName = StrUtil.trim(col.getName());
            if (StrUtil.isBlank(trimmedColName)) {
                continue;
            }

            if (hasCol) {
                str.append(",\n  ");
            }

            if (StrUtil.contains(col.getName(), "(") || StrUtil.contains(col.getName(), " ")) {
                // most likely an expression
                str.append(trimmedColName);
            } else {
                // regular identifier
                // str.append("`");
                str.append(quoteSimpleColumnName(trimmedColName));
                // str.append("`");
            }

            if (StrUtil.isNotBlank(col.getCollate())) {
                str.append(" COLLATE ");
                str.append(col.getCollate());
            }

            if (StrUtil.isNotBlank(col.getSort())) {
                str.append(" ");
                str.append(col.getSort().toUpperCase());
            }

            hasCol = true;
        }

        if (hasCol && CollUtil.isNotEmpty(this.getColumns())) {
            str.append("\n");
        }

        str.append(")");

        if (StrUtil.isNotBlank(this.getWhere())) {
            str.append(" WHERE ");
            str.append(this.getWhere());
        }

        return str.toString().replace("\n", "");
    }


    // ParseIndex parses the provided "CREATE INDEX" SQL string into Index struct.
    public static Index parseIndex(String createIndexExpr) {
        Index result = new Index();

        Matcher matches = indexRegex.matcher(createIndexExpr);
        if (!matches.find() || matches.groupCount() != 6) {
            return result;
        }

        String trimChars = "`\"'\\[\\]\\n\\t\\f\\v ";

        // Unique
        // ---
        result.setUnique(null != matches.group(1) && !"".equals(matches.group(1).trim()));

        // Optional (aka. "IF NOT EXISTS")
        // ---
        result.setOptional(null != matches.group(2) && !"".equals(matches.group(2).trim()));

        // SchemaName and IndexName
        // ---
        Tokenizer nameTk = Tokenizer.newFromString(matches.group(3));
        nameTk.setSeparators('.');
        List<String> nameParts = nameTk.scanAll();
        nameTk.close();

        if (nameParts.size() == 2) {
            result.setSchemaName(nameParts.get(0).trim().replaceAll("[" + trimChars + "]", ""));
            result.setIndexName(nameParts.get(1).trim().replaceAll("[" + trimChars + "]", ""));
        } else {
            result.setIndexName(nameParts.get(0).trim().replaceAll("[" + trimChars + "]", ""));
        }

        // TableName
        // ---
        result.setTableName(matches.group(4).trim().replaceAll("[" + trimChars + "]", ""));

        // Columns
        // ---
        Tokenizer columnsTk = Tokenizer.newFromString(matches.group(5));
        columnsTk.setSeparators(',');
        List<String> rawColumns = columnsTk.scanAll();
        columnsTk.close();

        result.setColumns(new ArrayList<>(rawColumns.size()));

        for (String col : rawColumns) {
            Matcher colMatches = indexColumnRegex.matcher(col);
            if (!colMatches.find()) {
                continue;
            }

            String trimmedName = colMatches.group(1).trim().replaceAll("[" + trimChars + "]", "");
            if ("".equals(trimmedName)) {
                continue;
            }

            result.getColumns().add(new IndexColumn(
                    trimmedName,
                    null != colMatches.group(2) ? colMatches.group(2).trim() : "",
                    null != colMatches.group(3) ? colMatches.group(3).trim().toUpperCase() : ""
            ));
        }

        // WHERE expression
        // ---
        result.setWhere(null != matches.group(6) ? matches.group(6).trim() : "");

        return result;
    }


    public Boolean getUnique() {
        return unique;
    }

    public Index setUnique(Boolean unique) {
        this.unique = unique;
        return this;
    }

    public Boolean getOptional() {
        return optional;
    }

    public Index setOptional(Boolean optional) {
        this.optional = optional;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public Index setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getIndexName() {
        return indexName;
    }

    public Index setIndexName(String indexName) {
        this.indexName = indexName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public Index setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public List<IndexColumn> getColumns() {
        return columns;
    }

    public Index setColumns(List<IndexColumn> columns) {
        this.columns = columns;
        return this;
    }

    public String getWhere() {
        return where;
    }

    public Index setWhere(String where) {
        this.where = where;
        return this;
    }
}
