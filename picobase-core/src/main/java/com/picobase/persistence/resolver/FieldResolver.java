package com.picobase.persistence.resolver;


import com.picobase.persistence.dbx.SelectQuery;

import java.util.List;

public interface FieldResolver {

    default void updateQuery(SelectQuery query) {

    }

    ResolverResult resolve(String field);


    static FieldResolver newSimpleFieldResolver(String... field) {
        return new SimpleFieldResolver(List.of(field));
    }
}
