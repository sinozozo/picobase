package com.picobase.logic;

import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.context.PbHolder;
import com.picobase.exception.BadRequestException;
import com.picobase.exception.ForbiddenException;
import com.picobase.log.PbLog;
import com.picobase.logic.mapper.RecordMapper;
import com.picobase.model.AdminModel;
import com.picobase.model.RecordModel;
import com.picobase.model.RequestInfo;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.resolver.RecordFieldResolver;
import com.picobase.persistence.resolver.ResultCouple;
import com.picobase.search.SearchFilter;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static com.picobase.persistence.dbx.DbxUtil.quoteSimpleColumnName;
import static com.picobase.persistence.dbx.DbxUtil.snakeCase;
import static com.picobase.util.PbConstants.JwtExtraFieldCollectionId;
import static com.picobase.util.PbConstants.QueryParam.*;
import static com.picobase.util.PbConstants.REQUEST_INFO_KEY;

public class RecordHelper {

    private static final PbLog log = PbManager.getLog();

    private static final RecordMapper recordMapper = PbUtil.findMapper(RecordModel.class);
    // Define the rule query parameters and the admin-only rule fields
    private static String[] ruleQueryParams = {FILTER, SORT};
    private static String[] adminOnlyRuleFields = {"@collection.", "@request."};

    /**
     * RequestInfo exports cached base request data fields
     * (query, body, logged auth state, etc.) from the provided context.
     *
     * @return
     */
    public static RequestInfo createRequestInfo() {
        // return cached to avoid copying the body multiple times
        RequestInfo requestInfo = (RequestInfo) PbHolder.getStorage().get(REQUEST_INFO_KEY);

        if (requestInfo != null) {
            // refresh auth state

            setAuthInfo(requestInfo);


            return requestInfo;
        }

        requestInfo = new RequestInfo();
        requestInfo.setContext(RequestInfo.REQUEST_INFO_CONTEXT_DEFAULT);
        requestInfo.setMethod(PbHolder.getRequest().getMethod());
        requestInfo.setQuery(new HashMap<>());
        requestInfo.setData(new HashMap<>());
        requestInfo.setHeaders(new HashMap<>());

        // extract the first value of all headers and normalizes the keys
        // ("X-Token" is converted to "x_token")
        Enumeration<String> headerNames = PbHolder.getRequest().getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String k = headerNames.nextElement();
            String v = PbHolder.getRequest().getHeader(k);
            if (StrUtil.isNotEmpty(v)) {
                requestInfo.getHeaders().put(snakeCase(k), v);
            }
        }

        setAuthInfo(requestInfo);

        UrlQuery urlQuery = UrlQuery.of(PbHolder.getRequest().getQueryString(), StandardCharsets.UTF_8);
        if (urlQuery.getQueryMap() != null) {
            requestInfo.getQuery().putAll(urlQuery.getQueryMap());
        }
        if (PbHolder.getRequest().getCachedContent() != null) {
            requestInfo.getData().putAll(PbUtil.createObjFromRequest(Map.class).get());// data -> key:String[]
        }

        PbHolder.getStorage().set(REQUEST_INFO_KEY, requestInfo);

        return requestInfo;
    }

    private static void setAuthInfo(RequestInfo requestInfo) {
        //从自定义session中获取对应session
        String authRecordId = (String) PbUtil.getLoginIdDefaultNull();
        if (StrUtil.isNotEmpty(authRecordId)) { // 增加authRecordId检查， 防止getExtra 时，loginType检查异常
            String collectionId = (String) PbUtil.getExtra(JwtExtraFieldCollectionId);
            if (StrUtil.isNotEmpty(authRecordId)) {
                RecordModel authRecord = recordMapper.findRecordById(collectionId, authRecordId).orElseThrow(() -> new BadRequestException("not found record ：" + authRecordId));
                requestInfo.setAuthRecord(authRecord);
            }
            return;
        }


        String adminId = (String) PbAdminUtil.getLoginIdDefaultNull();
        if (StrUtil.isNotEmpty(adminId)) {
            AdminModel admin = PbUtil.findById(AdminModel.class, adminId);
            if (admin == null) {
                //来自配置文件中定义的 admin,new 一个新的admin 做占位，无实际意义。
                admin = new AdminModel();
            }
            requestInfo.setAdmin(admin);
        }

    }

    /**
     * Checks and returns an error if the provided RequestInfo contains rule fields that only the admin can use.
     *
     * @param requestInfo the RequestInfo object
     * @return an error if the rule fields are restricted to admin, otherwise null
     * @todo consider moving the rules check to the RecordFieldResolver.
     */
    public static void checkForAdminOnlyRuleFields(RequestInfo requestInfo) {
        if (requestInfo.getAdmin() != null || requestInfo.getQuery().isEmpty()) {
            return; // admin or nothing to check
        }

        for (String param : ruleQueryParams) {
            String v = (String) requestInfo.getQuery().get(param);
            if (v == null || v.isEmpty()) {
                continue;
            }

            for (String field : adminOnlyRuleFields) {
                if (v.contains(field)) {
                    throw new ForbiddenException("Only admins can filter by " + field);
                }
            }
        }

    }

    /**
     * EnrichRecords parses the request context and enriches the provided records:
     * - expands relations (if defaultExpands and/or ?expand query param is set)
     * - ensures that the emails of the auth records and their expanded auth relations
     * are visible only for the current logged admin, record owner or record with manage access
     */
    public static Error enrichRecords(List<RecordModel> records, String... defaultExpands) {

        RequestInfo requestInfo = mustGetRequestInfo();

        try {
            autoIgnoreAuthRecordsEmailVisibility(records, requestInfo);
        } catch (Exception e) {
            //ignore exception
            log.error("Failed to resolve email visibility: ", e);
        }

        var expands = defaultExpands;

        var param = PbHolder.getRequest().getParameter(EXPAND);
        if (StrUtil.isNotEmpty(param)) {
            expands = ArrayUtil.append(expands, param.split(",", -1));
        }

        if (ArrayUtil.isEmpty(expands)) {
            return null;
        }

        Map<String, Error> errs = recordMapper.expandRecords(records, List.of(expands), expandFetch(requestInfo));
        if (!errs.isEmpty()) {
            return new Error("Failed to expand records: " + errs);
        }
        return null;
    }

    /**
     * expandFetch is the records fetch function that is used to expand related records.
     */
    private static RecordMapper.ExpandFetchFunc expandFetch(RequestInfo requestInfo) {
        return (relCollection, relIds) -> {

            try {
                var records = recordMapper.findRecordByIds(relCollection.getId(), relIds, (q) -> {
                    if (requestInfo.getAdmin() != null) {
                        return;
                    }

                    if (StrUtil.isEmpty(relCollection.getViewRule())) {
                        throw new IllegalStateException(String.format("Only admins can view collection %s records", relCollection.getName()));
                    }

                    var resolver = new RecordFieldResolver(relCollection, requestInfo, true);
                    var expr = new SearchFilter(relCollection.getViewRule()).buildExpr(resolver);
                    resolver.updateQuery(q);
                    q.andWhere(expr);
                });

                if (!records.isEmpty()) {
                    autoIgnoreAuthRecordsEmailVisibility(records, requestInfo);
                }
                return new ResultCouple<>(records);
            } catch (Exception e) {
                return new ResultCouple<>(null, new Error(e));
            }

        };
    }

    /**
     * autoIgnoreAuthRecordsEmailVisibility ignores the email visibility check for
     * the provided record if the current auth model is admin, owner or a "manager".
     * <p>
     * Note: Expects all records to be from the same auth collection!
     */
    private static void autoIgnoreAuthRecordsEmailVisibility(List<RecordModel> records, RequestInfo requestInfo) {
        if (records.isEmpty() || !records.get(0).getCollection().isAuth()) {
            return;
        }

        if (requestInfo.getAdmin() != null) {
            records.forEach(r -> r.setIgnoreEmailVisibility(true));
            return;
        }

        var collection = records.get(0).getCollection();

        Map<String, RecordModel> mappedRecords = new HashMap<>(records.size());
        var recordIds = new String[records.size()];

        for (int i = 0; i < records.size(); i++) {
            var rec = records.get(i);
            mappedRecords.put(rec.getId(), rec);
            recordIds[i] = rec.getId();
        }

        if (requestInfo.getAuthRecord() != null && mappedRecords.get(requestInfo.getAuthRecord().getId()) != null) {
            mappedRecords.get(requestInfo.getAuthRecord().getId()).setIgnoreEmailVisibility(true);
        }

        var authOptions = collection.authOptions();
        if (StrUtil.isEmpty(authOptions.getManageRule())) {
            return;
        }


        SelectQuery query = recordMapper.recordQuery(collection)
                .select(quoteSimpleColumnName(collection.getName()) + ".id")
                .andWhere(Expression.in(quoteSimpleColumnName(collection.getName()) + ".id", recordIds));
        var resolver = new RecordFieldResolver(collection, requestInfo, true);
        var expr = new SearchFilter(authOptions.getManageRule()).buildExpr(resolver);
        resolver.updateQuery(query);
        query.andWhere(expr);

        List<String> managedIds = query.column(String.class);

        managedIds.forEach(id -> {
            if (mappedRecords.containsKey(id)) {
                mappedRecords.get(id).setIgnoreEmailVisibility(true);
            }
        });
    }

    public static Error enrichRecord(RecordModel record, String... defaultExpands) {
        return enrichRecords(List.of(record), defaultExpands);
    }

    private static RequestInfo mustGetRequestInfo() {
        RequestInfo requestInfo = (RequestInfo) PbHolder.getStorage().get(REQUEST_INFO_KEY);
        if (requestInfo != null) {
            return requestInfo;
        }
        throw new IllegalStateException("cant get the RequestInfo obj");
    }


    // hasAuthManageAccess checks whether the client is allowed to have full
    // [forms.RecordUpsert] auth management permissions
    // (aka. allowing to change system auth fields without oldPassword).
    public static boolean hasAuthManageAccess(RecordModel record, RequestInfo requestInfo) {
        if (!record.getCollection().isAuth()) {
            return false;
        }

        String manageRule = record.getCollection().authOptions().getManageRule();
        if (StrUtil.isBlank(manageRule)) {
            return false; // only for admins (manageRule can't be empty)
        }

        if (Objects.isNull(requestInfo) || Objects.isNull(requestInfo.getAuthRecord())) {
            return false; // no auth record
        }

        Consumer<SelectQuery> ruleConsumer = selectQuery -> {
            RecordFieldResolver resolver = new RecordFieldResolver(record.getCollection(), requestInfo, true);
            Expression expression = new SearchFilter(manageRule).buildExpr(resolver);
            resolver.updateQuery(selectQuery);
            selectQuery.andWhere(expression);
        };

        recordMapper.findRecordById(record.getCollection().getId(), record.getId(), ruleConsumer);

        return true;
    }


}
