package com.picobase.validator;

import com.picobase.util.PbInnerUtil;

import java.util.Map;

import static com.picobase.validator.Error.newError;

/**
 * LengthRule is a validation rule that checks if a value's length is within the specified range.
 */
public class LengthRule implements Rule {
    private static final Error ErrLengthTooLong = newError("validation_length_too_long", "the length must be no more than {max}");
    private static final Error ErrLengthTooShort = newError("validation_length_too_short", "the length must be no less than {min}");
    private static final Error ErrLengthInvalid = newError("validation_length_invalid", "the length must be exactly {min}");
    private static final Error ErrLengthOutOfRange = newError("validation_length_out_of_range", "the length must be between {min} and {max}");
    private static final Error ErrLengthEmptyRequired = newError("validation_length_empty_required", "the value must be empty");
    private final int min;
    private final int max;
    private final Error err;

    public LengthRule(int min, int max) {
        this.min = min;
        this.max = max;
        this.err = buildLengthRuleError();
    }


    private Error buildLengthRuleError() {
        Error err;
        if (min == 0 && max > 0) {
            err = ErrLengthTooLong;
        } else if (min > 0 && max == 0) {
            err = ErrLengthTooShort;
        } else if (min > 0 && max > 0) {
            if (min == max) {
                err = ErrLengthInvalid;
            } else {
                err = ErrLengthOutOfRange;
            }
        } else {
            err = ErrLengthEmptyRequired;
        }

        return err.setParams(Map.of("min", min, "max", max));
    }


    @Override
    public Error validate(Object value) {
        if (value == null || PbInnerUtil.isEmpty(value)) {
            return null;
        }

        int l = Util.lengthOfValue(value);
        if (this.min > 0 && l < this.min || this.max > 0 && l > this.max || this.min == 0 && this.max == 0 && l > 0) {
            return this.err;
        }
        return null;
    }
}
