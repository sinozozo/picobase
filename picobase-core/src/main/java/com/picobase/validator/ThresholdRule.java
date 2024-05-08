package com.picobase.validator;

import java.util.Date;
import java.util.Map;

import static com.picobase.validator.Err.newError;

/**
 * ThresholdRule is a validation rule that checks if a value satisfies the specified threshold requirement.
 */
public class ThresholdRule implements Rule {
    /**
     * ErrMinGreaterEqualThanRequired is the error that returns when a value is less than a specified threshold.
     */
    private static final Err ErrMinGreaterEqualThanRequired = newError("validation_min_greater_equal_than_required", "must be no less than {{threshold}}");
    /**
     * ErrMaxLessEqualThanRequired is the error that returns when a value is greater than a specified threshold.
     */
    private static final Err ErrMaxLessEqualThanRequired = newError("validation_max_less_equal_than_required", "must be no greater than {{threshold}}");
    /**
     * ErrMinGreaterThanRequired is the error that returns when a value is less than or equal to a specified threshold.
     */
    private static final Err ErrMinGreaterThanRequired = newError("validation_min_greater_than_required", "must be greater than {{threshold}}");
    /**
     * ErrMaxLessThanRequired is the error that returns when a value is greater than or equal to a specified threshold.
     */
    private static final Err ErrMaxLessThanRequired = newError("validation_max_less_than_required", "must be less than {{threshold}}");

    private Object threshold;
    private int operator;
    private Err err;

    public static final int greaterThan = 0;
    public static final int greaterEqualThan = 1;
    public static final int lessThan = 2;
    public static final int lessEqualThan = 3;

    public ThresholdRule(Object threshold, int operator, Err err) {
        this.threshold = threshold;
        this.operator = operator;
        this.err = err;
    }

    public static ThresholdRule min(Object min) {
        return new ThresholdRule(min, greaterEqualThan, ErrMinGreaterEqualThanRequired);
    }

    public static ThresholdRule max(Object max) {
        return new ThresholdRule(max, lessEqualThan, ErrMaxLessEqualThanRequired);
    }

    public ThresholdRule exclusive() {
        if (operator == greaterEqualThan) {
            operator = greaterThan;
            err = ErrMinGreaterThanRequired;
        } else if (operator == lessEqualThan) {
            operator = lessThan;
            err = ErrMaxLessThanRequired;
        }
        return this;
    }


    @Override
    public Err validate(Object value) {
        Object checkedValue = Util.indirect(value);
        if (null == checkedValue || Util.isEmpty(checkedValue)) {
            return null;
        }

        switch (threshold.getClass().getSimpleName()) {
            case "Integer" -> {
                int intValue = Integer.parseInt(checkedValue.toString());
                if (compareInt((int) threshold, intValue)) {
                    return null;
                }
            }
            case "Long" -> {
                long longValue = Long.parseLong(checkedValue.toString());
                if (compareLong((long) threshold, longValue)) {
                    return null;
                }
            }
            case "Float", "Double" -> {
                float floatValue = Float.parseFloat(checkedValue.toString());
                if (compareFloat((float) threshold, floatValue)) {
                    return null;
                }
            }
            case "Date" -> {
                Date dateValue = (Date) checkedValue;
                Date thresholdDate = (Date) threshold;
                if (compareTime(thresholdDate, dateValue)) {
                    return null;
                }
            }
            default -> {
                return newError("", "Type not supported: " + threshold.getClass().getSimpleName());
            }
        }

        return err.setParams(Map.of("threshold", threshold));
    }

    public ThresholdRule error(String message) {
        err.setMessage(message);
        return this;
    }

    public ThresholdRule errorObject(Err err) {
        this.err = err;
        return this;
    }

    private boolean compareInt(int threshold, int value) {
        return switch (operator) {
            case greaterThan -> value > threshold;
            case greaterEqualThan -> value >= threshold;
            case lessThan -> value < threshold;
            default -> value <= threshold;
        };
    }

    private boolean compareLong(long threshold, long value) {
        return switch (operator) {
            case greaterThan -> value > threshold;
            case greaterEqualThan -> value >= threshold;
            case lessThan -> value < threshold;
            default -> value <= threshold;
        };
    }

    private boolean compareFloat(float threshold, float value) {
        return switch (operator) {
            case greaterThan -> value > threshold;
            case greaterEqualThan -> value >= threshold;
            case lessThan -> value < threshold;
            default -> value <= threshold;
        };
    }

    private boolean compareTime(Date threshold, Date value) {
        return switch (operator) {
            case greaterThan -> value.after(threshold);
            case greaterEqualThan -> value.after(threshold) || value.equals(threshold);
            case lessThan -> value.before(threshold);
            default -> value.before(threshold) || value.equals(threshold);
        };
    }

}
