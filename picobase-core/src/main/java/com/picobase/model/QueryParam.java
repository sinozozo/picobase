package com.picobase.model;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.StrUtil;
import com.picobase.util.PbConstants;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.picobase.util.PbConstants.QueryParam.*;


/**
 * 用户的查询请求参数
 */
public class QueryParam {

    public static final String CollectionParamKey = "collection";
    private Integer page;
    private Integer perPage;
    private String sort;
    private String filter;
    private String expand;
    private Boolean skipTotal;


    public static QueryParam create() {
        return new QueryParam();
    }

    public static QueryParam of(String queryParams) {

        QueryParam queryParam = new QueryParam();
        if (StrUtil.isEmpty(queryParams)) {
            return queryParam;
        }
        UrlQuery urlQuery = UrlQuery.of(queryParams, StandardCharsets.UTF_8);
        queryParam.setPage(Convert.toInt(urlQuery.get(PAGE)));
        queryParam.setPerPage(Convert.toInt(urlQuery.get(PER_PAGE)));
        queryParam.setSort(Convert.toStr(urlQuery.get(SORT)));
        queryParam.setFilter(Convert.toStr(urlQuery.get(FILTER)));
        queryParam.setExpand(Convert.toStr(urlQuery.get(EXPAND)));
        queryParam.setSkipTotal(Convert.toBool(urlQuery.get(SKIP_TOTAL)));
        return queryParam;
    }

    public boolean isEmpty() {
        return StrUtil.isAllEmpty(sort, filter, expand) && page == null && perPage == null && skipTotal == null;
    }

    public Integer getPage() {
        return page;
    }

    public QueryParam setPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public QueryParam setPerPage(Integer perPage) {
        this.perPage = perPage;
        return this;
    }

    public String getSort() {
        return sort;
    }

    public QueryParam setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public String getFilter() {
        return filter;
    }

    public QueryParam setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public String getExpand() {
        return expand;
    }

    public QueryParam setExpand(String expand) {
        this.expand = expand;
        return this;
    }

    public Boolean getSkipTotal() {
        return skipTotal;
    }

    public QueryParam setSkipTotal(Boolean skipTotal) {
        this.skipTotal = skipTotal;
        return this;
    }


    public String toQueryStr() {

        Map<String, Object> params = new HashMap<>();

        if (this.page != null) {
            params.put(PAGE, this.page);
        }
        if (this.perPage != null) {
            params.put(PER_PAGE, this.perPage);
        }
        if (this.sort != null) {
            params.put(SORT, this.sort);
        }
        if (this.filter != null) {
            params.put(PbConstants.QueryParam.FILTER, this.filter);
        }
        if (this.expand != null) {
            params.put(PbConstants.QueryParam.EXPAND, this.expand);
        }
        if (this.skipTotal != null) {
            params.put(PbConstants.QueryParam.SKIP_TOTAL, this.skipTotal);
        }
        if (!params.isEmpty()) {
            return UrlQuery.of(params).build(StandardCharsets.UTF_8);
        }
        return "";
    }

}
