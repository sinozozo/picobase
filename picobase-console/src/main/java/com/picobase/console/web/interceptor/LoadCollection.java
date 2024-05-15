package com.picobase.console.web.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * LoadCollectionContext middleware finds the collection with related
 * path identifier and loads it into the request context.
 * <p>
 * Set optCollectionTypes to further filter the found collection by its type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoadCollection {
    String[] optCollectionTypes() default {};
}