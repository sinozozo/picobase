package com.picobase.console.web;

import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.console.error.BadRequestException;
import com.picobase.console.error.ForbiddenException;
import com.picobase.console.error.NotFoundException;
import com.picobase.console.event.*;
import com.picobase.console.interceptor.InterceptorFunc;
import com.picobase.console.mapper.CollectionMapper;
import com.picobase.console.mapper.RecordMapper;
import com.picobase.console.model.RecordUpsert;
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
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.picobase.console.service.RecordHelper.*;
import static com.picobase.util.PbConstants.CollectionType.Auth;
import static com.picobase.util.PbConstants.CollectionType.Base;

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

        if (requestInfo.getAdmin() == null && collection.getListRule() == null) {
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
        if (requestInfo.getAdmin() == null && collection.getViewRule() == null) {
            // only admins can access if the rule is nil
            throw new ForbiddenException("Only admins can perform this action.");
        }

        Consumer<SelectQuery> ruleFunc = selectQuery -> {
            if (requestInfo.getAdmin() == null && StrUtil.isNotEmpty(collection.getViewRule())) {
                RecordFieldResolver recordFieldResolver = new RecordFieldResolver(collectionMapper.collFetchFun, collection, requestInfo, true);
                Expression expression = new SearchFilter(collection.getViewRule()).buildExpr(recordFieldResolver);
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


    @LoadCollection(optCollectionTypes = {Base, Auth})
    @PostMapping
    public Object create() {
        CollectionModel collection = PbUtil.getCurrentCollection();
        RequestInfo requestInfo = newRequestInfo();

        if (requestInfo.getAdmin() == null && collection.getListRule() == null) { //TODO 补充到文档中  Rule值 null 空字符 的不同是有差异的
            // only admins can access if the rule is nil
            throw new ForbiddenException("Only admins can perform this action.");
        }

        AtomicBoolean hasFullManageAccess = new AtomicBoolean(requestInfo.getAdmin() != null);

        // temporary save the record and check it against the creation rule
        if (requestInfo.getAdmin() == null && collection.getCreateRule() != null) {
            RecordModel testRecord = new RecordModel(collection);

            // replace modifiers fields so that the resolved value is always
            // available when accessing requestInfo.Data using just the field name
            // 将带有 modifier(+ -）的字段，替换成计算后的值，只对 number 、select、relation、file 类型的field 有效，
            // 查看具体的测试用例 testRecordReplaceModifiers 方法
            if (requestInfo.hasModifierDataKeys()) {
                requestInfo.setData(testRecord.replaceModifers(requestInfo.getData()));
            }

            RecordUpsert testForm = new RecordUpsert(testRecord);
            testForm.setManageAccess(true);

            testForm.loadRequest();

            Consumer<SelectQuery> createRuleFunc = selectQuery -> {
                if (StrUtil.isEmpty(collection.getCreateRule())) {
                    return;
                }
                RecordFieldResolver resolver = new RecordFieldResolver(collectionMapper.collFetchFun, collection, requestInfo, true);

                Expression expr = new SearchFilter(collection.getCreateRule()).buildExpr(resolver);
                resolver.updateQuery(selectQuery);
                selectQuery.andWhere(expr);
            };

            testForm.drySubmit(s -> {
                Optional<RecordModel> recordOptional = recordMapper.findRecordById(collection.getId(), testRecord.getId(), createRuleFunc);
                if (recordOptional.isEmpty()) {
                    throw new BadRequestException("DrySubmit create rule failure");
                }
                hasFullManageAccess.set(hasAuthManageAccess(recordOptional.get(), requestInfo));
            });

            throw new RuntimeException("not implemented"); // TODO

        }

        RecordModel record = new RecordModel(collection);
        RecordUpsert form = new RecordUpsert(record);
        form.setManageAccess(hasFullManageAccess.get());

        form.loadRequest();

        RecordCreateEvent event = new RecordCreateEvent(collection, record, form.getFilesToUpload(), TimePosition.BEFORE);
        InterceptorFunc<RecordModel, RecordModel> interceptorFun = next -> m -> {

            event.record = m;
            PbUtil.post(event);

            next.run(event.record);

            Error err = enrichRecord(event.record);
            if (err != null) {
                PbManager.getLog().warn("Failed to enrich create record, id: {}, collectionName: {}, err:{}",
                        event.record.getId(),
                        event.collection.getName(),
                        err);
            }
            event.timePosition = TimePosition.AFTER;
            PbUtil.post(event);
            return event.record;
        };

        return form.submit(interceptorFun);
    }

    @LoadCollection(optCollectionTypes = {Base, Auth})
    @PatchMapping("/{id}")
    public Object update(@PathVariable String id) {
        CollectionModel collection = PbUtil.getCurrentCollection();
        RequestInfo requestInfo = newRequestInfo();

        if (requestInfo.getAdmin() == null && collection.getUpdated() == null) {
            // only admins can access if the rule is nil
            throw new ForbiddenException("Only admins can perform this action.");
        }

        // eager fetch the record so that the modifier field values are replaced
        // and available when accessing requestInfo.Data using just the field name
        if (requestInfo.hasModifierDataKeys()) {
            Optional<RecordModel> recordOptional = recordMapper.findRecordById(collection.getId(), id);
            if (recordOptional.isEmpty()) {
                throw new NotFoundException();
            }
            requestInfo.setData(recordOptional.get().replaceModifers(requestInfo.getData()));

        }

        Consumer<SelectQuery> ruleFunc = selectQuery -> {
            if (requestInfo.getAdmin() == null && StrUtil.isNotEmpty(collection.getUpdateRule())) {
                RecordFieldResolver recordFieldResolver = new RecordFieldResolver(collectionMapper.collFetchFun, collection, requestInfo, true);
                Expression expression = new SearchFilter(collection.getUpdateRule()).buildExpr(recordFieldResolver);
                recordFieldResolver.updateQuery(selectQuery);
                selectQuery.andWhere(expression);
            }
        };

        // fetch record
        Optional<RecordModel> recordOptional = recordMapper.findRecordById(collection.getId(), id, ruleFunc);
        if (recordOptional.isEmpty()) {
            throw new NotFoundException();
        }

        RecordUpsert form = new RecordUpsert(recordOptional.get());
        form.setManageAccess(requestInfo.getAdmin() != null || hasAuthManageAccess(recordOptional.get(), requestInfo));

        // load request
        form.loadRequest();
        RecordUpdateEvent event = new RecordUpdateEvent(collection, form.getRecord(), form.getFilesToUpload(), TimePosition.BEFORE);

        InterceptorFunc<RecordModel, RecordModel> interceptorFun = next -> m -> {
            event.record = m;
            PbUtil.post(event);

            next.run(event.record);

            Error err = enrichRecord(event.record);
            if (err != null) {
                PbManager.getLog().warn("Failed to enrich update record, id: {}, collectionName: {}, err:{}",
                        event.record.getId(),
                        event.collection.getName(),
                        err);
            }
            event.timePosition = TimePosition.AFTER;
            PbUtil.post(event);
            return event.record;
        };

        return form.submit(interceptorFun);
    }

    @LoadCollection
    @DeleteMapping(value = "/{recordId}")
    public void delete(@PathVariable String recordId) {
        CollectionModel collection = PbUtil.getCurrentCollection();
        RequestInfo requestInfo = newRequestInfo();

        if (requestInfo.getAdmin() == null && collection.getDeleteRule() == null) {
            // only admins can access if the rule is nil
            throw new ForbiddenException("Only admins can perform this action.");
        }

        Consumer<SelectQuery> ruleFunc = selectQuery -> {
            if (requestInfo.getAdmin() == null && StrUtil.isNotEmpty(collection.getDeleteRule())) {
                RecordFieldResolver recordFieldResolver = new RecordFieldResolver(collectionMapper.collFetchFun, collection, requestInfo, true);
                Expression expression = new SearchFilter(collection.getDeleteRule()).buildExpr(recordFieldResolver);
                recordFieldResolver.updateQuery(selectQuery);
                selectQuery.andWhere(expression);
            }
        };

        Optional<RecordModel> recordOptional = recordMapper.findRecordById(collection.getId(), recordId, ruleFunc);
        if (recordOptional.isEmpty()) {
            throw new NotFoundException();
        }
        RecordModel record = recordOptional.get();

        PbUtil.post(new RecordDeleteEvent(collection, record, TimePosition.BEFORE));

        //执行删除
        recordMapper.deleteRecord(record);

        PbUtil.post(new RecordDeleteEvent(collection, record, TimePosition.AFTER));
    }
}
