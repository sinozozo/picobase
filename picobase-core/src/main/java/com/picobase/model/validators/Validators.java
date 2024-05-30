package com.picobase.model.validators;


import com.picobase.PbUtil;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.validator.RuleFunc;

import java.util.Map;

import static com.picobase.validator.Err.newError;


public class Validators {

    /**
     * checks whether the provided model id already exists.
     */
    public static RuleFunc uniqueId(String tableName) {
        return uniqueCol(tableName, "id");
    }

    public static RuleFunc uniqueCol(String tableName, String column) {
        return (value) -> {
            if (value == null) {
                return null;
            }
            Map<String, Object> row = PbUtil.getPbDbxBuilder().select("id").from(tableName).where(Expression.newHashExpr(Map.of(column, value))).limit(1).row();
            if (row != null && !row.isEmpty()) {
                return newError("validation_invalid_" + column, "The model " + column + " is invalid or already exists.");
            }
            return null;
        };
    }
}
