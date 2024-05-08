
package com.picobase.persistence.repository;


import com.picobase.persistence.model.MapperResult;

/**
 * Pagination Utils interface.
 *
 */
@SuppressWarnings("PMD.AbstractMethodOrInterfaceMethodMustUseJavadocRule")
public interface PbPaginationHelper<E> {
    
    Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, final Object[] args, final int pageNo,
            final int pageSize, final PbRowMapper<E> pbRowMapper);
    
    Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, final Object[] args, final int pageNo,
            final int pageSize, final Long lastMaxId, final PbRowMapper<E> pbRowMapper);
    
    Page<E> fetchPageLimit(final String sqlCountRows, final String sqlFetchRows, final Object[] args, final int pageNo,
            final int pageSize, final PbRowMapper<E> pbRowMapper);
    
    Page<E> fetchPageLimit(final String sqlCountRows, final Object[] args1, final String sqlFetchRows,
            final Object[] args2, final int pageNo, final int pageSize, final PbRowMapper<E> pbRowMapper);
    
    Page<E> fetchPageLimit(final String sqlFetchRows, final Object[] args, final int pageNo, final int pageSize,
            final PbRowMapper<E> pbRowMapper);
    
    Page<E> fetchPageLimit(final MapperResult countMapperResult, final MapperResult mapperResult, final int pageNo,
                           final int pageSize, final PbRowMapper<E> pbRowMapper);
    
    void updateLimit(final String sql, final Object[] args);
}
