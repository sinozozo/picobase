package com.picobase.console.web;

import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.console.error.BadRequestException;
import com.picobase.console.error.ForbiddenException;
import com.picobase.console.error.NotFoundException;
import com.picobase.console.event.RecordViewEvent;
import com.picobase.console.event.RecordsListEvent;
import com.picobase.console.mapper.CollectionMapper;
import com.picobase.console.mapper.RecordMapper;
import com.picobase.console.web.interceptor.LoadCollection;
import com.picobase.console.web.interceptor.LoadCollectionInterceptor;
import com.picobase.log.PbLog;
import com.picobase.model.CollectionModel;
import com.picobase.model.RecordModel;
import com.picobase.model.RequestInfo;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.RecordFieldResolver;
import com.picobase.search.PbProvider;
import com.picobase.search.SearchFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.function.Consumer;

import static com.picobase.console.util.RecordHelper.*;

@RestController
@RequestMapping("/api/collections/{" + LoadCollectionInterceptor.VARIABLES_ATTRIBUTE_COLLECTION_NAME_OR_ID + "}/records")
public class RecordController {

    private CollectionMapper collectionMapper;
    private RecordMapper recordMapper;
    private static final PbLog log = PbManager.getLog();

    public RecordController(PbMapperManager manager) {
        this.collectionMapper = manager.findMapper(CollectionModel.class);
        this.recordMapper = manager.findMapper(RecordModel.class);
    }

    @LoadCollection
    @GetMapping
    public Page<RecordModel> list() {

        //获取当前请求中的Collection，只能在标注了@LoadCollection的方法中有（基于拦截器获取）
        CollectionModel collection = PbUtil.getCurrentCollection();

        RequestInfo requestInfo = newRequestInfo();

        // forbid users and guests to query special filter/sort fields
        checkForAdminOnlyRuleFields(requestInfo);

        if (requestInfo.getAdmin() == null && StrUtil.isEmpty(collection.getListRule())) {
            // only admins can access if the rule is nil
            throw new ForbiddenException("Only admins can perform this action.");
        }

        var fieldsResolver = new RecordFieldResolver(
                collectionMapper.collFetchFun,
                collection,
                requestInfo,
                // hidden fields are searchable only by admins
                requestInfo.getAdmin() != null
        );


        var searchProvider = new PbProvider(fieldsResolver).query(recordMapper.recordQuery(collection));

        if (requestInfo.getAdmin() == null && StrUtil.isNotEmpty(collection.getListRule())) {
            searchProvider.addFilter(new SearchFilter(collection.getListRule()));
        }
        Page<RecordModel> result = searchProvider.parseAndExec(collection);

        PbUtil.post(new RecordsListEvent(collection, result));

        Error error = enrichRecords(result.getItems());
        if (error != null) {
            PbManager.getLog().error("Failed to expand: {}", error.getMessage());
        }

        return result;
    }


    @LoadCollection
    @GetMapping("/{recordId}")
    public RecordModel view(@PathVariable String recordId) {
        //获取当前请求中的Collection，只能在标注了@LoadCollection的方法中有（基于拦截器获取）
        CollectionModel collection = PbUtil.getCurrentCollection();

        if (StrUtil.isEmpty(recordId)) {
            throw new BadRequestException("");
        }

        RequestInfo requestInfo = newRequestInfo();
        if (requestInfo.getAdmin() == null && StrUtil.isEmpty(collection.getListRule())) {
            // only admins can access if the rule is nil
            throw new ForbiddenException("Only admins can perform this action.");
        }

        Consumer<SelectQuery> ruleFunc = selectQuery -> {
            if (requestInfo.getAdmin() == null && collection.getViewRule() != null && !collection.getViewRule().isEmpty()) {
                RecordFieldResolver recordFieldResolver = new RecordFieldResolver(collectionMapper.collFetchFun, collection, requestInfo, true);
                Expression expression = new SearchFilter(collection.getCreateRule()).buildExpr(recordFieldResolver);
                recordFieldResolver.updateQuery(selectQuery);
                selectQuery.andWhere(expression);
            }
        };

        Optional<RecordModel> optionalRecordModel = recordMapper.findRecordById(collection.getId(), recordId, ruleFunc);
        if (optionalRecordModel.isEmpty()) {
            throw new NotFoundException();
        }

        PbUtil.post(new RecordViewEvent(collection, optionalRecordModel.get()));

        Error error = enrichRecord(optionalRecordModel.get());
        if (error != null) {
            log.error("Failed to enrichRecord ,collecion: {}, recordId: {},error: {}", collection, recordId, error.getMessage());
        }
        return optionalRecordModel.get();
    }
}
