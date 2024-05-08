package com.picobase.spring.repository;

import com.picobase.persistence.model.MapperResult;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.repository.PbPaginationHelper;
import com.picobase.persistence.repository.PbRowMapper;
import com.picobase.persistence.repository.StorageContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * External Storage Pagination utils.
 */

public class PaginationHelperImpl<E> implements PbPaginationHelper {

    private final JdbcTemplate jdbcTemplate;

    public PaginationHelperImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Take paging.
     *
     * @param sqlCountRows query total SQL
     * @param sqlFetchRows query data sql
     * @param args         query parameters
     * @param pageNo       page number
     * @param pageSize     page size
     * @param pbRowMapper  {@link PbRowMapper}
     * @return Paginated data {@code <E>}
     */
    @Override
    public Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, final Object[] args,
                             final int pageNo, final int pageSize, final PbRowMapper pbRowMapper) {
        return fetchPage(sqlCountRows, sqlFetchRows, args, pageNo, pageSize, null, pbRowMapper);
    }

    @Override
    public Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, Object[] args, final int pageNo,
                             final int pageSize, final Long lastMaxId, final PbRowMapper pbRowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }

        // Query the total number of current records.
        Integer rowCountInt = jdbcTemplate.queryForObject(sqlCountRows, args, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }

        // Compute pages count
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }

        // Create Page object
        final Page<E> page = new Page<>();
        page.setPage(pageNo);
        page.setTotalPages(pageCount);
        page.setTotalItems(rowCountInt);
        page.setPerPage(pageSize);

        if (pageNo > pageCount) {
            return page;
        }

        List<E> result = jdbcTemplate.query(sqlFetchRows, args, (rs, rowNum) -> (E) pbRowMapper.mapRow(rs, rowNum));
        for (E item : result) {
            page.getItems().add(item);
        }
        return page;
    }

    @Override
    public Page<E> fetchPageLimit(final String sqlCountRows, final String sqlFetchRows, final Object[] args,
                                  final int pageNo, final int pageSize, final PbRowMapper pbRowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // Query the total number of current records
        Integer rowCountInt = jdbcTemplate.queryForObject(sqlCountRows, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }

        // Compute pages count
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }

        // Create Page object
        final Page<E> page = new Page<>();
        page.setPage(pageNo);
        page.setTotalPages(pageCount);
        page.setTotalItems(rowCountInt);
        page.setPerPage(pageSize);

        if (pageNo > pageCount) {
            return page;
        }

        List<E> result = jdbcTemplate.query(sqlFetchRows, args, (rs, rowNum) -> (E) pbRowMapper.mapRow(rs, rowNum));
        for (E item : result) {
            page.getItems().add(item);
        }
        return page;
    }

    @Override
    public Page fetchPageLimit(MapperResult countMapperResult, MapperResult mapperResult, int pageNo, int pageSize,
                               PbRowMapper pbRowMapper) {
        return fetchPageLimit(countMapperResult.getSql(), countMapperResult.getParamList().toArray(),
                mapperResult.getSql(), mapperResult.getParamList().toArray(), pageNo, pageSize, pbRowMapper);
    }

    @Override
    public Page<E> fetchPageLimit(final String sqlCountRows, final Object[] args1, final String sqlFetchRows,
                                  final Object[] args2, final int pageNo, final int pageSize, final PbRowMapper pbRowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // Query the total number of current records
        Integer rowCountInt = jdbcTemplate.queryForObject(sqlCountRows, args1, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }

        // Compute pages count
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }

        // Create Page object
        final Page<E> page = new Page<>();
        page.setPage(pageNo);
        page.setTotalPages(pageCount);
        page.setTotalItems(rowCountInt);
        page.setPerPage(pageSize);

        if (pageNo > pageCount) {
            return page;
        }
        List<E> result = jdbcTemplate.query(sqlFetchRows, args2, (rs, rowNum) -> (E) pbRowMapper.mapRow(rs, rowNum));
        for (E item : result) {
            page.getItems().add(item);
        }
        return page;
    }

    @Override
    public Page<E> fetchPageLimit(final String sqlFetchRows, final Object[] args, final int pageNo, final int pageSize,
                                  final PbRowMapper pbRowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // Create Page object
        final Page<E> page = new Page<>();
        List<E> result = jdbcTemplate.query(sqlFetchRows, args, (rs, rowNum) -> (E) pbRowMapper.mapRow(rs, rowNum));
        for (E item : result) {
            page.getItems().add(item);
        }
        return page;
    }

    @Override
    public void updateLimit(final String sql, final Object[] args) {
        try {
            jdbcTemplate.update(sql, args);
        } finally {
            StorageContextHolder.cleanAllContext();
        }
    }

    /**
     * Update limit with response.
     *
     * @param sql  sql
     * @param args args
     * @return update row count
     */
    public int updateLimitWithResponse(final String sql, final Object[] args) {
        String sqlUpdate = sql;

        try {
            return jdbcTemplate.update(sqlUpdate, args);
        } finally {
            StorageContextHolder.cleanAllContext();
        }
    }

}
