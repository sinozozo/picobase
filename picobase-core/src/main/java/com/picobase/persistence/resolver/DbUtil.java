package com.picobase.persistence.resolver;

import cn.hutool.core.util.StrUtil;
import com.picobase.persistence.model.Index;

import java.util.List;

import static com.picobase.persistence.dbx.DbxUtil.quoteColumnName;
import static com.picobase.persistence.model.Index.parseIndex;

public class DbUtil {
    public static String jsonArrayLength(String column) {
        return String.format("JSON_LENGTH(IF(JSON_VALID(%s),%s,IF(%s='',JSON_ARRAY(),JSON_ARRAY(%s))))",
                quoteColumnName(column), quoteColumnName(column), quoteColumnName(column), quoteColumnName(column));
    }


    public static String jsonEach(String column) {

        return String.format("JSON_TABLE( IF(JSON_VALID(%s), %s, JSON_ARRAY(%s)), '$[*]' COLUMNS ( `value` VARCHAR(255) PATH '$' ) )"
                , quoteColumnName(column), quoteColumnName(column), quoteColumnName(column));

    }

    /**
     * JsonExtract returns a JSON_EXTRACT SQL string expression with
     * some normalizations for non-json columns.
     */
    public static String jsonExtract(String column, String path) {
        //prefix the path with dot if it is not starting with array notation
        if (StrUtil.isNotEmpty(path) && !path.startsWith("[")) {
            path = "." + path;
        }
        return String.format("(IF(JSON_VALID(%s), JSON_EXTRACT(%s, '$%s'), JSON_EXTRACT(JSON_OBJECT('pb', %s), '$.pb%s')))",
                column,
                column,
                path,
                column,
                path);
    }

    /**
     * HasColumnUniqueIndex loosely checks whether the specified column has
     * a single column unique index (WHERE statements are ignored).
     */
    public static boolean hasSingleColumnUniqueIndex(String column, List<String> indexes) {
        for (int i = 0; i < indexes.size(); i++) {
            Index parsed = parseIndex(indexes.get(i));
            if (parsed.getUnique() && parsed.getColumns().size() == 1 && parsed.getColumns().get(0).getName().equals(column)) {
                return true;
            }
        }
        return false;
    }
}
