package com.picobase.validator;

import java.util.List;
import java.util.Map;

import static com.picobase.validator.Err.newErrors;

/**
 * Each returns a validation rule that loops through an iterable (map, slice or array)
 * and validates each value inside with the provided rules.
 * An empty iterable is considered valid. Use the Required rule to make sure the iterable is not empty.
 */
public class EachRule implements Rule {

    private Rule[] rules;


    public EachRule(Rule... rules) {
        this.rules = rules;
    }

    @Override
    public Err validate(Object value) {
        Errors errors = newErrors();

        if (value instanceof Map<?, ?> map) {
            int index = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object val = entry.getValue();
                Err err = Validation.validate(val, rules);
                if (err != null) {
                    errors.put(Integer.toString(index), err);
                }
                index++;
            }
        } else if (value instanceof List<?> v) {
            for (int i = 0; i < v.size(); i++) {
                Object val = v.get(i);
                Err err = Validation.validate(val, rules);
                if (err != null) {
                    errors.put(Integer.toString(i), err);
                }
            }
        } else {
            return Err.newError("", "must be an iterable (map, slice or array)");
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        return null;
    }
}
