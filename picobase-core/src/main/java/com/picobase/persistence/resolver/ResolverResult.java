package com.picobase.persistence.resolver;


import com.picobase.persistence.dbx.expression.Expression;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ResolverResult {


    private String identifier;
    private boolean noCoalesce;
    private Map<String, Object> params = new HashMap<>();


    private Expression multiMatchSubQuery;

    private Function<Expression, Expression> afterBuild;

    public ResolverResult(String identifier, boolean noCoalesce, Map<String, Object> params, Expression multiMatchSubQuery, Function<Expression, Expression> afterBuild) {
        this.identifier = identifier;
        this.noCoalesce = noCoalesce;
        this.params = params;
        this.multiMatchSubQuery = multiMatchSubQuery;
        this.afterBuild = afterBuild;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isNoCoalesce() {
        return noCoalesce;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Expression getMultiMatchSubQuery() {
        return multiMatchSubQuery;
    }

    public Function<Expression, Expression> getAfterBuild() {
        return afterBuild;
    }

    public static ResolverResultBuilder builder() {
        return new ResolverResultBuilder();
    }

    public static class ResolverResultBuilder {
        private String identifier;
        private boolean noCoalesce;
        private Map<String, Object> params = new HashMap<>();


        private Expression multiMatchSubQuery;

        private Function<Expression, Expression> afterBuild;


        public ResolverResultBuilder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }


        public ResolverResultBuilder noCoalesce(boolean noCoalesce) {
            this.noCoalesce = noCoalesce;
            return this;
        }


        public ResolverResultBuilder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }


        public ResolverResultBuilder multiMatchSubQuery(Expression multiMatchSubQuery) {
            this.multiMatchSubQuery = multiMatchSubQuery;
            return this;
        }


        public ResolverResultBuilder afterBuild(Function<Expression, Expression> afterBuild) {
            this.afterBuild = afterBuild;
            return this;
        }

        public ResolverResult build() {
            return new ResolverResult(identifier, noCoalesce, params, multiMatchSubQuery, afterBuild);
        }
    }
}
