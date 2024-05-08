package com.picobase.validator;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.picobase.validator.Err.newError;

/**
 * DateRule is a validation rule that validates date/time string values.
 */
public class DateRule implements Rule {

    /**
     * ErrDateInvalid is the error that returns in case of an invalid date.
     */
    private static final Err ErrDateInvalid = newError("validation_date_invalid", "must be a valid date");

    /**
     * ErrDateOutOfRange is the error that returns in case of an invalid date.
     */
    private static final Err ErrDateOutOfRange = newError("validation_date_out_of_range", "the date is out of range");

    private String layout;
    private LocalTime min, max;
    private Err err, rangeErr;


    public DateRule(String layout) {
        this.layout = layout;
        this.err = ErrDateInvalid;
        this.rangeErr = ErrDateOutOfRange;
    }

    public DateRule min(LocalTime min) {
        this.min = min;
        return this;
    }

    public DateRule max(LocalTime max) {
        this.max = max;
        return this;
    }

    @Override
    public Err validate(Object value) {
        Object checkedValue = Util.indirect(value);
        if (null == checkedValue || Util.isEmpty(checkedValue)) {
            return null;
        }
        String str;
        try {
            str = Util.ensureString(value);
        } catch (Exception e) {
            return err;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(layout);
        LocalTime date;
        try {
            date = LocalTime.parse(str, formatter);
        } catch (Exception e) {
            return err;
        }

        if ((min != null && min.isAfter(date)) ||
                (max != null && max.isBefore(date))) {
            return rangeErr;
        }
        return null;
    }
}
