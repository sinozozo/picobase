package com.picobase.validator;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.picobase.validator.Error.newError;

public class MatchRule implements Rule {
    private static final Error ErrMatchInvalid = newError("validation_match_invalid", "must be in a valid format");
    private final Pattern p;
    private Error err = ErrMatchInvalid;

    public MatchRule(Pattern p) {
        this.p = p;
    }

    @Override
    public Error validate(Object value) {
        if (value == null || "".equals(value)) {
            return null;
        }
        String strValue = String.valueOf(value);
        Matcher matcher = p.matcher(strValue);
        if (matcher.matches()) {
            return null;
        }
        return err;
    }

    public Error errorMessage(String msg) {
        this.err.setMessage(msg);
        return err;
    }

    public MatchRule setErr(Error err) {
        this.err = err;
        return this;
    }
}
