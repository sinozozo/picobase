package com.picobase.search;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.context.PbHolder;
import com.picobase.exception.PbException;
import com.picobase.log.PbLog;
import com.picobase.logic.mapper.RecordRowMapper;
import com.picobase.model.CollectionModel;
import com.picobase.model.RecordModel;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.FieldResolver;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.picobase.util.PbConstants.DEFAULT_PER_PAGE;
import static com.picobase.util.PbConstants.MAX_PER_PAGE;
import static com.picobase.util.PbConstants.QueryParam.*;


public class PbProvider {

    // Execute the 2 queries concurrently

    private static final PbLog log = PbManager.getLog();


    private final FieldResolver fieldResolver;
    private final List<SortField> sort = new ArrayList<>();
    private final List<SearchFilter> filter = new ArrayList<>();
    private SelectQuery query;
    private boolean skipTotal;
    private boolean skipData;
    private int page = 1;
    private int perPage = DEFAULT_PER_PAGE;
    private String countCol = "id";
    private CollectionModel collection;

    public PbProvider(FieldResolver fieldResolver) {
        this.fieldResolver = fieldResolver;
    }


    public PbProvider query(SelectQuery query) {
        this.query = query;
        return this;
    }

    /**
     * 搜索 普通 Model（静态）
     */
    public <T> Page<T> parseAndExec(String urlQuery, Class<T> clz) {
        if (urlQuery == null) {
            urlQuery = "";
        }
        parse(urlQuery);
        return exec(clz);
    }

    /**
     * 搜索 普通 Model（静态）
     */
    public <T> Page<T> parseAndExec(Class<T> clz) {
        String urlQuery = PbHolder.getRequest().getQueryString();
        if (urlQuery == null) {
            urlQuery = "";
        }
        return parseAndExec(urlQuery, clz);
    }

    /**
     * 搜索 普通 Model（静态）
     */
    public <T> Page<T> parseAndExec(String urlQuery, CollectionModel collection) {
        this.collection = collection;
        return (Page<T>) parseAndExec(urlQuery, RecordModel.class);
    }

    /**
     * 搜索recorder （动态）
     */
    public <T> Page<T> parseAndExec(CollectionModel collection) {
        this.collection = collection;
        return (Page<T>) parseAndExec(RecordModel.class);
    }

    public PbProvider countCol(String countCol) {
        this.countCol = countCol;
        return this;
    }

    /**
     * // Exec executes the search provider and fills/scans
     * // the provided `items` slice with the found models.
     */
    private <T> Page<T> exec(Class<T> clz) {
        if (this.query == null) {
            throw new RuntimeException("query is not set");
        }


        try {
            // shallow clone the provider's query
            SelectQuery modelsQuery = (SelectQuery) this.query.clone();

            // build filters
            this.filter.forEach(filter -> {
                var expr = filter.buildExpr(this.fieldResolver);
                if (expr != null) {
                    modelsQuery.andWhere(expr);
                }
            });

            // apply sorting
            sort.forEach(sortField -> {
                String expr = sortField.buildExpr(this.fieldResolver);
                if (expr != null) {
                    modelsQuery.andOrderBy(expr);
                }
            });

            // apply field resolver query modifications (if any)
            this.fieldResolver.updateQuery(modelsQuery);

            // normalize page
            if (this.page <= 0) {
                this.page = 1;
            }

            // normalize perPage
            if (this.perPage <= 0) {
                this.perPage = DEFAULT_PER_PAGE;
            } else if (this.perPage > MAX_PER_PAGE) {
                this.perPage = MAX_PER_PAGE;
            }

            // negative value to differentiate from the zero default
            long totalCount = -1;
            long totalPages = -1;
            List<T> items;

            // prepare a count query from the base one
            SelectQuery countQuery = (SelectQuery) modelsQuery.clone(); // shallow clone

            Callable<Map<String, Object>> countExec = () -> {
                String countCol = this.countCol;
                if (!countQuery.getFrom().isEmpty()) {
                    countCol = "`" + countQuery.getFrom().get(0) + "`.`" + countCol + "`";
                } else {
                    countCol = "`" + countCol + "`";
                }
                // note: countQuery is shallow cloned and slice/map in-place modifications should be avoided
                return countQuery.distinct(false)
                        .select("COUNT(DISTINCT " + countCol + ") as count")
                        .orderBy().row();
            };

            Callable<List<T>> modelsExec = () -> {
                modelsQuery.limit(this.perPage);
                modelsQuery.offset((long) (this.page - 1) * this.perPage);
                if (clz == RecordModel.class) {
                    return (List<T>) modelsQuery.all(new RecordRowMapper(this.collection));
                }
                return modelsQuery.all(clz);
            };

            if (skipData && !skipTotal) {
                Map<String, Object> row = countExec.call();
                totalCount = (long) row.get("count");
                items = Collections.EMPTY_LIST;
            } else if (!skipTotal) {
                ExecutorService executor = Executors.newFixedThreadPool(2);
                Future<Map<String, Object>> countFuture = executor.submit(countExec);
                Future<List<T>> modelsFuture = executor.submit(modelsExec);
                try {
                    Map<String, Object> row = countFuture.get();
                    totalCount = (long) row.get("count");
                    totalPages = (int) Math.ceil((double) totalCount / (double) this.perPage);

                    items = modelsFuture.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } finally {
                    executor.shutdown();
                }
            } else {
                items = modelsExec.call();
            }
            return new Page<>(this.page, this.perPage, (int) totalCount, (int) totalPages, items);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new PbException("Something went wrong while processing your request. Invalid filter.");
        }


    }

    public PbProvider skipTotal(boolean skipTotal) {
        this.skipTotal = skipTotal;
        return this;
    }

    public PbProvider skipData(boolean skipData) {
        this.skipData = skipData;
        return this;
    }

    public PbProvider page(int page) {
        this.page = page;
        return this;
    }


    private PbProvider perPage(int perPage) {
        this.perPage = perPage;
        return this;
    }

    public PbProvider addSort(SortField sortField) {
        this.sort.add(sortField);
        return this;
    }

    public PbProvider addFilter(SearchFilter filter) {
        this.filter.add(filter);
        return this;
    }

    private void parse(String urlQuery) {
        var params = UrlQuery.of(urlQuery, StandardCharsets.UTF_8);

        var skipTotal = params.get(SKIP_TOTAL);
        if (StrUtil.isNotEmpty(skipTotal)) {
            this.skipTotal(Convert.toBool(skipTotal));
        }

        var skipData = params.get(SKIP_DATA);
        if (StrUtil.isNotEmpty(skipData)) {
            this.skipData(Convert.toBool(skipData));
        }

        var pageQueryParam = params.get(PAGE);
        if (StrUtil.isNotEmpty(pageQueryParam)) {
            this.page(Convert.toInt(pageQueryParam));
        }

        var perPageQueryParam = params.get(PER_PAGE);
        if (StrUtil.isNotEmpty(perPageQueryParam)) {
            this.perPage(Convert.toInt(perPageQueryParam));
        }

        var sortQueryParam = params.get(SORT);
        if (StrUtil.isNotEmpty(sortQueryParam)) {
            SortField.parseSortFromString(sortQueryParam.toString())
                    .forEach(this::addSort);
        }
        var filterQueryParam = params.get(FILTER);
        if (StrUtil.isNotEmpty(filterQueryParam)) {
            this.addFilter(new SearchFilter(filterQueryParam.toString()));
        }
    }


}
