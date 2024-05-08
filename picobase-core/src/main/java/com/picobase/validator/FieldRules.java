package com.picobase.validator;


/**
 * FieldRules represents a rule set associated with a struct field.
 */
public class FieldRules<T, R> {

    /**
     * 待校验的对象，
     */
    private T checkObj;
    public String fieldName;
    private R fieldVal;
    public Rule[] rules;

    public FieldFns.FieldFn<T, R> fn;

    public FieldRules(String fieldName, R val, Rule... rules) {
        this.fieldName = fieldName;
        this.fieldVal = val;
        this.rules = rules;
    }


    public FieldRules(FieldFns.FieldFn<T, R> function, Rule... rules) {
        this.fn = function;
        this.fieldName = FieldFns.name(this.fn);
        this.rules = rules;
    }

    /**
     * 待校验的值
     *
     * @return
     */
    public Object checkValue() {
        if (fn != null) {
            return fn.apply(checkObj);
        }
        return fieldVal;
    }

    public FieldRules<T, R> setCheckObj(T checkObj) {
        this.checkObj = checkObj;
        return this;
    }
}
