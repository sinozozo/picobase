package com.picobase.search;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.model.Store;
import com.picobase.persistence.dbx.ConcatExpr;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.fexpr.*;
import com.picobase.persistence.resolver.FieldResolver;
import com.picobase.persistence.resolver.ResolverResult;
import com.picobase.util.StringEscapeUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static com.picobase.persistence.dbx.expression.Expression.and;
import static com.picobase.persistence.fexpr.SignOp.SignEq;

public class SearchFilter {

    private final String filterData;

    public SearchFilter(String filterData) {
        this.filterData = filterData;
    }


    // parsedFilterData holds a cache with previously parsed filter data expressions
    // (initialized with some preallocated empty data map)
    private static final Store<List<ExprGroup>> parsedFilterData = new Store<>();

    // DefaultLikeEscape specifies the default special character escaping for LIKE expressions
    // The strings at 2i positions are the special characters to be escaped while those at 2i+1 positions
    // are the corresponding escaped versions.
    public static final String[] DEFAULT_LIKE_ESCAPE = {"\\", "\\\\", "%", "\\%", "_", "\\_"};

    /**
     * // BuildExpr parses the current filter data and returns a new db WHERE expression.
     * //
     * // The filter string can also contain dbx placeholder parameters (eg. "title = :name"),
     * // that will be safely replaced and properly quoted inplace with the placeholderReplacements values.
     */
    public Expression buildExpr(FieldResolver fieldResolver, Map<String, Object>... placeholderReplacements) {
        String raw = filterData;

        if (StrUtil.isEmpty(raw)) {
            return null;
        }

        // replace the placeholder params in the raw string filter
        for (var p : placeholderReplacements) {
            for (Map.Entry<String, Object> entry : p.entrySet()) {
                String replacement;
                var value = entry.getValue();
                if (value == null
                        || value instanceof Number
                        || value instanceof Boolean
                ) {
                    replacement = String.valueOf(entry.getValue()); //value 为 null 时，valueOf 方法返回字符串 "null".
                } else if (value instanceof String
                        || value instanceof Date
                        || value instanceof LocalDate) {
                    if (value.toString().indexOf("'") != -1) {
                        replacement = "'" + value.toString().replaceAll("\\'", "\\\\\\\\\'") + "'"; // TODO 心情无法描述
                    } else {

                        replacement = "'" + StringEscapeUtils.escapeJson(value.toString()) + "'";
                    }

                } else { // 处理 list map 类型
                    //replacement = objectMapper.writeValueAsString(entry.getValue());
                    replacement = StringEscapeUtils.escapeJson(PbManager.getPbJsonTemplate().toJsonString(entry.getValue()));
                    replacement = "'" + replacement + "'";
                }

                // 使用正则表达式匹配整个键名
                String regex = ":" + entry.getKey() + "\\b";
                raw = raw.replaceAll(regex, StringEscapeUtils.escapeJson(replacement));
            }

        }


        if (parsedFilterData.has(raw)) {
            return buildParsedFilterExpr(parsedFilterData.get(raw), fieldResolver);
        }

        List<ExprGroup> data;
        try {
            data = FexprParser.parse(raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // store in cache
        // (the limit size is arbitrary and it is there to prevent the cache growing too big)
        parsedFilterData.setIfLessThanLimit(raw, data, 500);
        return buildParsedFilterExpr(data, fieldResolver);
    }


    private Expression buildParsedFilterExpr(List<ExprGroup> data, FieldResolver fieldResolver) {
        if (data.isEmpty()) {
            throw new RuntimeException("empty filter expression");
        }
        ConcatExpr result = new ConcatExpr();
        result.setSeparator(" ");

        for (ExprGroup group : data) {
            Expression expr;

            Object item = group.getItem();

            if (item instanceof Expr) {
                expr = resolveTokenizedExpr((Expr) item, fieldResolver);
            } else if (item instanceof ExprGroup) {
                expr = buildParsedFilterExpr(Collections.singletonList((ExprGroup) item), fieldResolver);
            } else if (item instanceof List) {
                expr = buildParsedFilterExpr((List<ExprGroup>) item, fieldResolver);
            } else {
                throw new RuntimeException("unsupported expression item");
            }

            if (!result.getParts().isEmpty()) {
                String op;
                if (group.getJoin() == JoinOp.OR) {
                    op = "OR";
                } else {
                    op = "AND";
                }
                result.addPart(Expression.newOp(op));
            }

            result.addPart(expr);
        }


        return result;
    }

    private Expression resolveTokenizedExpr(Expr expr, FieldResolver fieldResolver) {
        ResolverResult lResult = resolveToken(expr.getLeft(), fieldResolver);
        if (lResult == null || lResult.getIdentifier() == null || lResult.getIdentifier().isEmpty()) {
            throw new RuntimeException(String.format("invalid left operand \"%s\"", expr.getLeft().getLiteral()));
        }

        ResolverResult rResult = resolveToken(expr.getRight(), fieldResolver);
        if (rResult == null || rResult.getIdentifier() == null || rResult.getIdentifier().isEmpty()) {
            throw new RuntimeException(String.format("invalid right operand %s", expr.getRight().getLiteral()));
        }

        return buildResolversExpr(lResult, expr.getOp(), rResult);
    }

    private ResolverResult resolveToken(Token token, FieldResolver fieldResolver) {
        switch (token.getType()) {
            case Identifier -> {
                // check for macros
                IdentifierMacros.Macro macro = IdentifierMacros.getMacro(token.getLiteral());
                if (macro != null) {
                    var placeholder = "t" + RandomUtil.randomString(5);
                    Object value = macro.getValue();
                    return ResolverResult.builder().identifier(":" + placeholder).params(Map.of(placeholder, value)).build();
                }

                ResolverResult result = null;
                // custom resolver
                try {
                    result = fieldResolver.resolve(token.getLiteral());
                } catch (Exception e) {
                    // field 为 null 值情况会抛出异常，这里不做处理
                }

                if (result == null) {
                    // if `null` field is missing, treat `null` identifier as NULL token
                    var m = Map.of("null", "NULL",
                            // if `true` field is missing, treat `true` identifier as TRUE token
                            "true", "1",
                            // if `false` field is missing, treat `false` identifier as FALSE token
                            "false", "0");
                    String v = m.get(token.getLiteral().toLowerCase());
                    if (v == null) {
                        return null;
                    }
                    return ResolverResult.builder().identifier(v).build();
                }
                return result;

            }
            case Text -> {
                var placeholder = "t" + RandomUtil.randomString(5);
                return ResolverResult.builder()
                        .identifier(":" + placeholder)
                        .params(Map.of(placeholder, token.getLiteral()))
                        .build();
            }
            case Number -> {
                var placeholder = "t" + RandomUtil.randomString(5);
                return ResolverResult.builder()
                        .identifier(":" + placeholder)
                        .params(Map.of(placeholder, Double.valueOf(token.getLiteral())))
                        .build();
            }
        }
        throw new RuntimeException("unresolvable token type");
    }

    private Expression buildResolversExpr(ResolverResult left, SignOp op, ResolverResult right) {
        Expression expr = switch (op) {
            case SignEq, SignAnyEq -> resolveEqualExpr(true, left, right);
            case SignNeq, SignAnyNeq -> resolveEqualExpr(false, left, right);
            case SignLike, SignAnyLike -> {
                // the right side is a column and therefor wrap it with "%" for not-contains like behavior
                if (right.getParams().isEmpty()) {
                    yield Expression.newExpr(String.format("%s LIKE CONCAT('%%',%s,'%%')", left.getIdentifier(), right.getIdentifier()),
                            left.getParams());
                } else {
                    yield Expression.newExpr(String.format("%s LIKE %s", left.getIdentifier(), right.getIdentifier()),
                            mergeParams(left.getParams(), wrapLikeParams(right.getParams())));
                }
            }
            case SignNlike, SignAnyNlike -> {
                // the right side is a column and therefor wrap it with "%" for not-contains like behavior
                if (right.getParams().isEmpty()) {
                    yield Expression.newExpr(String.format("%s NOT LIKE CONCAT('%%',%s,'%%')", left.getIdentifier(), right.getIdentifier()),
                            left.getParams());
                } else {
                    yield Expression.newExpr(String.format("%s NOT LIKE %s", left.getIdentifier(), right.getIdentifier()),
                            mergeParams(left.getParams(), wrapLikeParams(right.getParams())));
                }
            }
            case SignLt, SignAnyLt ->
                    Expression.newExpr(String.format("%s < %s", left.getIdentifier(), right.getIdentifier()),
                            mergeParams(left.getParams(), right.getParams()));
            case SignLte, SignAnyLte ->
                    Expression.newExpr(String.format("%s <= %s", left.getIdentifier(), right.getIdentifier()),
                            mergeParams(left.getParams(), right.getParams()));

            case SignGt, SignAnyGt ->
                    Expression.newExpr(String.format("%s > %s", left.getIdentifier(), right.getIdentifier()),
                            mergeParams(left.getParams(), right.getParams()));
            case SignGte, SignAnyGte ->
                    Expression.newExpr(String.format("%s >= %s", left.getIdentifier(), right.getIdentifier()),
                            mergeParams(left.getParams(), right.getParams()));
        };


        // multi-match expressions
        if (!isAnyMatchOp(op)) {
            if (left.getMultiMatchSubQuery() != null && right.getMultiMatchSubQuery() != null) {
                var mm = new ManyVsManyExpr(left, right, op);
                expr = Expression.enclose(and(expr, mm));
            } else if (left.getMultiMatchSubQuery() != null) {

                var mm = new ManyVsOneExpr(left.isNoCoalesce(), left.getMultiMatchSubQuery(), op, right);
                expr = Expression.enclose(and(expr, mm));
            } else if (right.getMultiMatchSubQuery() != null) {
                var mm = new ManyVsOneExpr(right.isNoCoalesce(), right.getMultiMatchSubQuery(), op, left, true);
                expr = Expression.enclose(and(expr, mm));
            }

        }

        if (left.getAfterBuild() != null) {
            expr = left.getAfterBuild().apply(expr);
        }

        if (right.getAfterBuild() != null) {
            expr = right.getAfterBuild().apply(expr);
        }

        return expr;
    }

    private boolean isAnyMatchOp(SignOp op) {
        return switch (op) {
            case SignAnyEq, SignAnyNeq, SignAnyLike, SignAnyNlike, SignAnyLt, SignAnyLte, SignAnyGt, SignAnyGte -> true;
            default -> false;
        };

    }

    private Map<String, Object> mergeParams(Map<String, Object>... params) {
        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> param : params) {
            if (param == null) {
                continue;
            }
            result.putAll(param);
        }
        return result;
    }


    private Map<String, Object> wrapLikeParams(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            String valueStr = String.valueOf(entry.getValue());

            if (!containsUnescapedChar(valueStr, '%')) {
                // note: this is done to minimize the breaking changes and to preserve the original autoescape behavior
                valueStr = escapeUnescapedChars(valueStr, '\\', '%', '_');
                valueStr = "%" + valueStr + "%";
            }

            result.put(key, valueStr);
        }

        return result;
    }

    /**
     * 包含转译的（%）字符
     * <p>
     * "abc%abc", '%' -> true
     * "abc\\%abc", '%' -> false
     * "abc\\\\%abc", '%' -> true
     */
    private boolean containsUnescapedChar(String str, char ch) {
        char prev = 0;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ch && prev != '\\') {
                return true;
            }

            if (c == '\\' && prev == '\\') {
                prev = 0; // reset escape sequence
            } else {
                prev = c;
            }
        }

        return false;
    }

    /**
     * 在字符串中转义指定的字符
     *
     * @param str         待处理的字符串
     * @param escapeChars 可变参数，指定需要转义的字符列表
     * @return
     */
    private String escapeUnescapedChars(String str, char... escapeChars) {
        char[] rs = str.toCharArray();
        int total = rs.length;
        StringBuilder result = new StringBuilder();

        boolean match = false;

        for (int i = total - 1; i >= 0; i--) {
            if (match) {
                if (rs[i] != '\\') {
                    result.append('\\');
                }
                match = false;
            } else {
                for (char ec : escapeChars) {
                    if (rs[i] == ec) {
                        match = true;
                        break;
                    }
                }
            }

            result.append(rs[i]);

            // in case the matching char is at the beginning
            if (i == 0 && match) {
                result.append('\\');
            }
        }

        // reverse
        return result.reverse().toString();
    }

    public static void main(String[] args) {
        System.out.println(new SearchFilter("").containsUnescapedChar("abc%abc", '%'));
        System.out.println(new SearchFilter("").containsUnescapedChar("abc\\%abc", '%'));
        System.out.println(new SearchFilter("").containsUnescapedChar("abc\\\\%abc", '%'));
        System.out.println(new SearchFilter("").escapeUnescapedChars("ab\\%c", '\\', '%', '_'));
        System.out.println(new SearchFilter("").escapeUnescapedChars("%ab\\%c", '\\', '%', '_'));


    }

    /**
     * // Resolves = and != expressions in an attempt to minimize the COALESCE
     * // usage and to gracefully handle null vs empty string normalizations.
     * //
     * // The expression `a = "" OR a is null` tends to perform better than
     * // `COALESCE(a, "") = ""` since the direct match can be accomplished
     * // with a seek while the COALESCE will induce a table scan.
     * <p>
     * ** mysql 不等于空 !='',等于空 a = '' or a is null **
     */
    private Expression resolveEqualExpr(boolean equal, ResolverResult left, ResolverResult right) {
        var isLeftEmpty = isEmptyIdentifier(left) || (left.getParams().size() == 1 && hasEmptyParamValue(left));
        var isRightEmpty = isEmptyIdentifier(right) || (right.getParams().size() == 1 && hasEmptyParamValue(right));
        var equalOp = "=";
        var nullEqualOp = "=";
        var concatOp = "OR";
        var nullExpr = "IS NULL";

        if (!equal) {
            equalOp = "!=";
            nullEqualOp = equalOp;
            concatOp = "AND";
            nullExpr = "IS NOT NULL";
        } else {
            equalOp = "=";
            nullEqualOp = equalOp;
            concatOp = "OR";
            nullExpr = "IS NULL";
        }

        // no coalesce (eg. compare to a json field)
        // a IS b
        // a IS NOT b
        if (left.isNoCoalesce() || right.isNoCoalesce()) {
            return Expression.newExpr(String.format("%s %s %s", left.getIdentifier(), nullEqualOp, right.getIdentifier())
                    , mergeParams(left.getParams(), right.getParams()));

        }
        // both operands are empty
        if (isLeftEmpty && isRightEmpty) {
            return Expression.newExpr(String.format("'' %s ''", equalOp)
                    , mergeParams(left.getParams(), right.getParams()));
        }

        // direct compare since at least one of the operands is known to be non-empty
        // eg. a = 'example'
        if (isKnownNonEmptyIdentifier(left) || isKnownNonEmptyIdentifier(right)) {
            var leftIdentifier = left.getIdentifier();
            if (isLeftEmpty) {
                leftIdentifier = "''";
            }
            var rightIdentifier = right.getIdentifier();
            if (isRightEmpty) {
                rightIdentifier = "''";
            }

            return Expression.newExpr(String.format("%s %s %s", leftIdentifier, equalOp, rightIdentifier)
                    , mergeParams(left.getParams(), right.getParams()));
        }
        // "" = b OR b IS NULL
        // "" = b
        if (isLeftEmpty) {
            if (equal) {
                return Expression.newExpr(String.format("('' %s %s %s %s %s)", equalOp, right.getIdentifier(), concatOp, right.getIdentifier(), nullExpr)
                        , mergeParams(left.getParams(), right.getParams()));
            } else {
                return Expression.newExpr(String.format("'' %s %s", equalOp, right.getIdentifier())
                        , mergeParams(left.getParams(), right.getParams()));
            }

        }

        // a = "" OR a IS NULL
        // a != ""
        if (isRightEmpty) {
            if (equal) {
                return Expression.newExpr(String.format("(%s %s '' %s %s %s)", left.getIdentifier(), equalOp, concatOp, left.getIdentifier(), nullExpr)
                        , mergeParams(left.getParams(), right.getParams()));
            }
            return Expression.newExpr(String.format("%s %s ''", left.getIdentifier(), equalOp)
                    , mergeParams(left.getParams(), right.getParams()));
        }

        // fallback to a COALESCE comparison
        return Expression.newExpr(String.format("COALESCE(%s, '') %s COALESCE(%s, '')", left.getIdentifier(), equalOp, right.getIdentifier())
                , mergeParams(left.getParams(), right.getParams()));
    }

    private boolean isKnownNonEmptyIdentifier(ResolverResult result) {
        return switch (result.getIdentifier().toLowerCase()) {
            case "1", "0", "false", "true" -> true;
            default -> !result.getParams().isEmpty()
                    && !hasEmptyParamValue(result)
                    && !isEmptyIdentifier(result);
        };
    }

    private static boolean hasEmptyParamValue(ResolverResult result) {
        for (Map.Entry<String, Object> entry : result.getParams().entrySet()) {
            if (entry.getValue() == null) {
                return true;
            } else if (entry.getValue() instanceof String) {
                if (((String) entry.getValue()).isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isEmptyIdentifier(ResolverResult result) {
        return switch (result.getIdentifier().toLowerCase()) {
            case "", "null", "''", "\"\"", "``" -> true;
            default -> false;
        };
    }


    /**
     * manyVsManyExpr constructs a multi-match many<->many db where expression.
     * <p>
     * Expects leftSubQuery and rightSubQuery to return a subquery with a
     * single "multiMatchValue" column.
     */

    private class ManyVsManyExpr implements Expression {

        private final ResolverResult left;
        private final ResolverResult right;
        private final SignOp op;

        public ManyVsManyExpr(ResolverResult left, ResolverResult right, SignOp op) {
            this.left = left;
            this.right = right;
            this.op = op;
        }

        /**
         * Build converts the expression into a SQL fragment.
         */
        @Override
        public String build(Map<String, Object> params) {
            if (this.left.getMultiMatchSubQuery() == null || this.right.getMultiMatchSubQuery() == null) {
                return "0=1";
            }

            var lAlias = "__ml" + RandomUtil.randomString(5);
            var rAlias = "__mr" + RandomUtil.randomString(5);

            Expression whereExpr;
            try {
                whereExpr = buildResolversExpr(
                        ResolverResult.builder().noCoalesce(this.left.isNoCoalesce()).identifier(lAlias + ".multiMatchValue").build()
                        , this.op
                        , ResolverResult.builder().noCoalesce(this.right.isNoCoalesce()).identifier(rAlias + ".multiMatchValue")
                                // note: the AfterBuild needs to be handled only once and it
                                // doesn't matter whether it is applied on the left or right subquery operand
                                .afterBuild(multiMatchAfterBuildFunc(this.op, lAlias, rAlias))
                                .build()
                );
            } catch (Exception ignore) {
                return "0=1";
            }

            return String.format("NOT EXISTS (SELECT 1 FROM (%s) %s LEFT JOIN (%s) %s ON 1=1 WHERE %s)", // 这里的 1=1 在左右为表达式情况下需要 如下条件：self_rel_many.title = self_rel_many.title
                    this.left.getMultiMatchSubQuery().build(params),
                    lAlias,
                    this.right.getMultiMatchSubQuery().build(params),
                    rAlias,
                    whereExpr.build(params)
            );
        }
    }

    private Function<Expression, Expression> multiMatchAfterBuildFunc(SignOp op, String... multiMatchAliases) { //multiMatchAliases 不为 null
        return expr -> {
            expr = Expression.not(expr); // inverse for the not-exist expression

            if (op == SignEq) {
                return expr;
            }


            var orExprs = new Expression[multiMatchAliases.length + 1];
            orExprs[0] = expr;

            // Add an optional "IS NULL" condition(s) to handle the empty rows result.
            //
            // For example, let's assume that some "rel" field is [nonemptyRel1, nonemptyRel2, emptyRel3],
            // The filter "rel.total > 0" ensures that the above will return true only if all relations
            // are existing and match the condition.
            //
            // The "=" operator is excluded because it will never equal directly with NULL anyway
            // and also because we want in case "rel.id = ''" is specified to allow
            // matching the empty relations (they will match due to the applied COALESCE).
            for (int i = 0; i < multiMatchAliases.length; i++) {
                var mAlias = multiMatchAliases[i];
                orExprs[i + 1] = Expression.newExpr(mAlias + ".multiMatchValue IS NULL");
            }

            return Expression.enclose(Expression.or(orExprs));
        };
    }

    private class ManyVsOneExpr implements Expression {
        private final boolean noCoalesce;
        private final Expression subQuery;
        private final SignOp op;
        private final ResolverResult otherOperand;
        private boolean inverse;

        public ManyVsOneExpr(boolean noCoalesce, Expression subQuery, SignOp op, ResolverResult otherOperand) {
            this.noCoalesce = noCoalesce;
            this.subQuery = subQuery;
            this.op = op;
            this.otherOperand = otherOperand;
        }

        public ManyVsOneExpr(boolean noCoalesce, Expression subQuery, SignOp op, ResolverResult otherOperand, boolean inverse) {
            this.noCoalesce = noCoalesce;
            this.subQuery = subQuery;
            this.op = op;
            this.otherOperand = otherOperand;
            this.inverse = inverse;
        }

        @Override
        public String build(Map<String, Object> params) {
            if (this.subQuery == null) {
                return "0=1";
            }
            var alias = "__sm" + RandomUtil.randomString(5);

            var r1 = ResolverResult.builder().noCoalesce(this.noCoalesce).identifier(alias + ".multiMatchValue").afterBuild(multiMatchAfterBuildFunc(this.op, alias)).build();

            var r2 = ResolverResult.builder().identifier(this.otherOperand.getIdentifier()).params(this.otherOperand.getParams()).build();

            Expression whereExpr = null;
            Exception err = null;
            try {
                if (this.inverse) {
                    whereExpr = buildResolversExpr(r2, this.op, r1);
                } else {
                    whereExpr = buildResolversExpr(r1, this.op, r2);
                }
            } catch (Exception e) {
                err = e;
            }

            if (err != null) {
                return "0=1";
            }

            return String.format(
                    "NOT EXISTS (SELECT 1 FROM (%s) %s WHERE %s)",
                    this.subQuery.build(params),
                    alias,
                    whereExpr.build(params)
            );
        }
    }
}