package com.picobase.logic;

import cn.hutool.core.util.BooleanUtil;
import com.picobase.exception.BadRequestException;
import com.picobase.util.HtmlUtil;

import java.util.List;

public interface FieldModifier {

    public final NilModifier Nil_Modifier = new NilModifier();

    Object modify(Object value);

    /**
     * newExcerptModifier validates the specified raw string arguments and
     * initializes a new excerptModifier.
     * <p>
     * This method is usually invoked in initModifer().
     */
    static ExcerptModifier newExcerptModifier(List<String> args) {
        var totalArgs = args.size();
        if (totalArgs == 0) {
            throw new BadRequestException("max argument is required - expected (max, withEllipsis?)");
        }

        if (totalArgs > 2) {
            throw new BadRequestException("too many arguments - expected (max, withEllipsis?)");
        }

        var max = Integer.parseInt(args.get(0));
        if (max == 0) {
            throw new BadRequestException("max argument must be > 0");
        }
        boolean withEllipsis = false;
        if (totalArgs > 1) {
            withEllipsis = BooleanUtil.toBoolean(args.get(1));
        }

        return new ExcerptModifier(max, withEllipsis);
    }

    /**
     * 仅占位用
     */
    record NilModifier() implements FieldModifier {
        @Override
        public Object modify(Object value) {
            return value;
        }
    }
}

class ExcerptModifier implements FieldModifier {
    private final int max;
    private final boolean withEllipsis;

    public ExcerptModifier(int max, boolean withEllipsis) {
        this.max = max;
        this.withEllipsis = withEllipsis;
    }

    @Override
    public Object modify(Object value) { // TODO 该实现是否合理
        if (!(value instanceof String)) {
            return value;
        }
        String cleanContent = HtmlUtil.cleanHtmlTag((String) value).trim();
        if (cleanContent.length() > max) {
            cleanContent = cleanContent.substring(0, max);
        }

        if (withEllipsis) {
            return cleanContent + "...";
        }
        return cleanContent;
    }


}
