package com.picobase.persistence.resolver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.picobase.PbUtil;
import com.picobase.fun.PbCollFetchFun;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.model.CollectionModel;
import com.picobase.model.RequestInfo;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.util.PbConstants;

import java.util.*;

import static com.picobase.persistence.dbx.DbxUtil.splitModifier;
import static com.picobase.util.PbConstants.IssetModifier;


public class RecordFieldResolver implements FieldResolver {
    private CollectionModel baseCollection;
    private RequestInfo requestInfo;
    private Map<String, Object> staticRequestInfo;
    private List<String> allowedFields;
    private List<CollectionModel> loadedCollections;
    private List<Join> joins;
    private boolean allowHiddenFields;

    private final CollectionMapper collectionMapper = PbUtil.findMapper(CollectionModel.class);

    public RecordFieldResolver(
                               CollectionModel baseCollection,
                               RequestInfo requestInfo,
                               boolean allowHiddenFields) {
        this.baseCollection = baseCollection;
        this.requestInfo = requestInfo;
        this.allowHiddenFields = allowHiddenFields;
        this.joins = new ArrayList<>();
        this.loadedCollections = new ArrayList<>();
        this.loadedCollections.add(baseCollection);
        this.allowedFields = Arrays.asList(
                "^\\w+[\\w\\.\\:]*$",
                "^\\@request\\.context$",
                "^\\@request\\.method$",
                "^\\@request\\.auth\\.[\\w\\.\\:]*\\w+$",
                "^\\@request\\.data\\.[\\w\\.\\:]*\\w+$",
                "^\\@request\\.query\\.[\\w\\.\\:]*\\w+$",
                "^\\@request\\.headers\\.\\w+$",
                "^\\@collection\\.\\w+(\\:\\w+)?\\.[\\w\\.\\:]*\\w+$"
        );

        this.staticRequestInfo = new HashMap<>();
        if (requestInfo != null) {
            staticRequestInfo.put("context", requestInfo.getContext());
            staticRequestInfo.put("method", requestInfo.getMethod());
            staticRequestInfo.put("query", requestInfo.getQuery());
            staticRequestInfo.put("headers", requestInfo.getHeaders());
            staticRequestInfo.put("data", requestInfo.getData());
            if (requestInfo.getAuthRecord() != null) {
                var authData = requestInfo.getAuthRecord().publicExport();
                // always add the record email no matter of the emailVisibility field
                authData.put(PbConstants.FieldName.Email, requestInfo.getAuthRecord().email());
                staticRequestInfo.put("auth", authData);
            }
        }
    }

    /**
     * Conditionally updates the provided search query based on the
     * resolved fields (eg. dynamically joining relations).
     */
    @Override
    public void updateQuery(SelectQuery query) {
        if (!this.joins.isEmpty()) {
            query.distinct(true);

            for (Join join : this.joins) {
                query.leftJoin(join.getTableName() + " " + join.getTableAlias(), join.getOn());
            }
        }
    }

    /**
     * Resolve implements `search.FieldResolver` interface.
     * <p>
     * Example of some resolvable fieldName formats:
     * <p>
     * id
     * someSelect.each
     * project.screen.status
     * screen.project_via_prototype.name
     *
     * @request.context
     * @request.method
     * @request.query.filter
     * @request.headers.x_token
     * @request.auth.someRelation.name
     * @request.data.someRelation.name
     * @request.data.someField
     * @request.data.someSelect:each
     * @request.data.someField:isset
     * @collection.product.name
     */
    @Override
    public ResolverResult resolve(String fieldName) {
        return parseAndRun(fieldName, this);
    }

    private ResolverResult parseAndRun(String fieldName, RecordFieldResolver recordFieldResolver) {
        return new ResolverRunner(fieldName, recordFieldResolver).run();
    }

    public CollectionModel loadCollection(String collectionNameOrId) {
        //return already loaded
        CollectionModel collectionModel = this.loadedCollections.stream()
                .filter(co -> co.getId().equals(collectionNameOrId) || co.getId().equalsIgnoreCase(collectionNameOrId))
                .findFirst().orElse(null);

        if (collectionModel != null) {
            return collectionModel;
        }

        CollectionModel collection = collectionMapper.findCollectionByNameOrId(collectionNameOrId);//
        if (collection==null) {
            return null;
        }
        this.loadedCollections.add(collection);
        return collection;
    }

    public void registerJoin(String tableName, String tableAlias, Expression on) {
        var join = new Join(tableName, tableAlias, on);

        // replace existing join
        for (int i = 0; i < this.joins.size(); i++) {
            if (joins.get(i).getTableAlias().equals(join.getTableAlias())) {
                this.joins.set(i, join);
                return;
            }
        }

        this.joins.add(join);
    }

    public ResolverResult resolveStaticRequestField(List<String> path) {
        if (CollUtil.isEmpty(path)) {
            throw new RuntimeException("at least one path key should be provided");
        }
        var arr = splitModifier(path.get(path.size() - 1));
        var lastProp = arr[0];
        var modifier = arr[1];

        path.set(path.size() - 1, lastProp);

        // extract value
        ResultCouple<Object> couple = extractNestedMapVal(this.staticRequestInfo, path);
        var resultVal = couple.getResult();

        if (IssetModifier.equals(modifier)) {
            if (couple.getError() != null) {
                return ResolverResult.builder().identifier("FALSE").build();
            }
            return ResolverResult.builder().identifier("TRUE").build();
        }

        // note: we are ignoring the error because requestInfo is dynamic
        // and some of the lookup keys may not be defined for the request

        if (resultVal == null) {
            return ResolverResult.builder().identifier("NULL").build();
        } else if (resultVal instanceof String) {
            // check if it is a number field and explicitly try to cast to
            // float in case of a numeric string value was used
            // (this usually the case when the data is from a multipart/form-data request)
            var field = this.baseCollection.getSchema().getFieldByName(path.get(path.size() - 1));
            if (field != null && field.getType().equals(PbConstants.FieldType.Number)) {
                resultVal = NumberUtil.parseDouble(String.valueOf(resultVal));

            }

        } else if (NumberUtil.isNumber(String.valueOf(resultVal)) || resultVal instanceof Boolean) {
            // no further processing is needed...
        } else {
            // TODO 这里缺少json转换逻辑 ， 但何时需要转换成json还需要观察测试用例
            resultVal = resultVal.toString();
            //throw new RuntimeException("not implemented");
        }
        var placeholder = "t" + RandomUtil.randomString(5);
        return ResolverResult.builder().identifier(":" + placeholder)
                .params(Map.of(placeholder, resultVal)).build();
    }

    private ResultCouple<Object> extractNestedMapVal(Map<String, Object> m, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new ResultCouple<>(null, new Error("at least one key should be provided"));
        }
        if (!m.containsKey(keys.get(0))) {
            return new ResultCouple<>(null, new Error("invalid key path - missing key " + keys.get(0)));
        }
        var result = m.get(keys.get(0));
        // end key reached
        if (keys.size() == 1) {
            return new ResultCouple<>(result);
        }
        if (result instanceof Map) {
            return extractNestedMapVal((Map<String, Object>) result, keys.subList(1, keys.size()));
        } else {
            return new ResultCouple<>(null, new Error("expected map, got " + result));
        }
    }


    public CollectionModel getBaseCollection() {
        return baseCollection;
    }

    public RecordFieldResolver setBaseCollection(CollectionModel baseCollection) {
        this.baseCollection = baseCollection;
        return this;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public RecordFieldResolver setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        return this;
    }

    public Map<String, Object> getStaticRequestInfo() {
        return staticRequestInfo;
    }

    public RecordFieldResolver setStaticRequestInfo(Map<String, Object> staticRequestInfo) {
        this.staticRequestInfo = staticRequestInfo;
        return this;
    }

    public List<String> getAllowedFields() {
        return allowedFields;
    }

    public RecordFieldResolver setAllowedFields(List<String> allowedFields) {
        this.allowedFields = allowedFields;
        return this;
    }

    public List<CollectionModel> getLoadedCollections() {
        return loadedCollections;
    }

    public RecordFieldResolver setLoadedCollections(List<CollectionModel> loadedCollections) {
        this.loadedCollections = loadedCollections;
        return this;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public RecordFieldResolver setJoins(List<Join> joins) {
        this.joins = joins;
        return this;
    }

    public boolean isAllowHiddenFields() {
        return allowHiddenFields;
    }

    public RecordFieldResolver setAllowHiddenFields(boolean allowHiddenFields) {
        this.allowHiddenFields = allowHiddenFields;
        return this;
    }
}

