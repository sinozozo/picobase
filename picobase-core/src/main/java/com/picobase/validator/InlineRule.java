package com.picobase.validator;

class InlineRule implements Rule {
    private final RuleFunc ruleFunc;

    public InlineRule(RuleFunc ruleFunc) {
        this.ruleFunc = ruleFunc;
    }

    @Override
    public Err validate(Object value) {
        return this.ruleFunc.apply(value);
    }
}
