package com.picobase.model.schema.fieldoptions;


import cn.hutool.core.util.StrUtil;
import com.picobase.validator.Err;
import com.picobase.validator.Errors;
import com.picobase.validator.RuleFunc;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.picobase.validator.Validation.*;

public class TextOptions implements FieldOptions {
    private Integer min;
    private Integer max;
    private String pattern;


    @Override
    public Errors validate() {
        int minVal = 0;
        if (min != null) {
            minVal = min;
        }

        return validateObject(this,
                field(TextOptions::getMin, min(0)),
                field(TextOptions::getMax, min(minVal)),
                field(TextOptions::getPattern, by(checkRegex()))
        );
    }

    private RuleFunc checkRegex() {
        return (Object value) -> {
            String v = (String) value;
            if (StrUtil.isBlank(v)) {
                return null; // nothing to check
            }

            try {
                Pattern.compile(v);
            } catch (PatternSyntaxException e) {
                return Err.newError("validation_invalid_regex", e.getMessage());
            }
            return null;
        };
    }

    public Integer getMin() {
        return min;
    }

    public TextOptions setMin(Integer min) {
        this.min = min;
        return this;
    }

    public Integer getMax() {
        return max;
    }

    public TextOptions setMax(Integer max) {
        this.max = max;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public TextOptions setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }
}

