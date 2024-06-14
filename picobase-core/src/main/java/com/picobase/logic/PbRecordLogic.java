package com.picobase.logic;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.annotation.PbCollection;
import com.picobase.context.PbHolder;
import com.picobase.exception.BadRequestException;
import com.picobase.exception.ForbiddenException;
import com.picobase.exception.NotFoundException;
import com.picobase.exception.PbException;
import com.picobase.file.PbFile;
import com.picobase.file.PbFileSystem;
import com.picobase.log.PbLog;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.logic.mapper.RecordMapper;
import com.picobase.model.CollectionModel;
import com.picobase.model.QueryParam;
import com.picobase.model.RecordModel;
import com.picobase.model.RequestInfo;
import com.picobase.model.event.RecordViewEvent;
import com.picobase.model.event.RecordsListEvent;
import com.picobase.model.schema.SchemaField;
import com.picobase.model.schema.fieldoptions.FileOptions;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.mapper.Editor;
import com.picobase.persistence.mapper.MappingOptions;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.ListUtil;
import com.picobase.persistence.resolver.RecordFieldResolver;
import com.picobase.search.PbProvider;
import com.picobase.search.SearchFilter;
import com.picobase.util.PbConstants;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.picobase.file.PbFileSystem.THUMB_PREFIX;
import static com.picobase.logic.RecordHelper.*;

public class PbRecordLogic {
    private static final PbLog log = PbManager.getLog();
    private static final String[] IMAGE_CONTENT_TYPES = new String[]{"image/png", "image/jpg", "image/jpeg", "image/gif"};
    private static final String[] DEFAULT_THUMB_SIZES = new String[]{"100x100"};
    private final CollectionMapper collectionMapper = PbUtil.findMapper(CollectionModel.class);
    private final RecordMapper recordMapper = PbUtil.findMapper(RecordModel.class);
    private final ConcurrentHashMap<String, CompletableFuture<Void>> thumbGenPending = new ConcurrentHashMap<>();
    private final Semaphore thumbGenSem = new Semaphore(Runtime.getRuntime().availableProcessors() + 2);
    private final Duration thumbGenMaxWait = Duration.ofSeconds(60);
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2);

    /**
     * 取出expand值内容，填充到Map中，将 Record 转换成平铺形式的Map 结构
     *
     * @param record RecordModel对象
     * @return Map<String, Object>
     */
    public static Map<String, Object> flatRecordMap(RecordModel record) {
        if (!record.isAlreadyExported()) {
            record.publicExport();
        }
        Map<String, Object> sourceMap = record.getPublicData();
        sourceMap.forEach((key, value) -> {
            if (PbConstants.FieldName.Expand.equals(key)) {
                if (value instanceof Map) {
                    Map<String, Object> expand = (Map<String, Object>) value;
                    expand.forEach((innerK, innerV) -> {
                        if (innerV instanceof RecordModel r) {
                            // 单关系
                            sourceMap.put(innerK, flatRecordMap(r));
                        } else if (innerV instanceof List l) {
                            //list 结构 多关系
                            sourceMap.put(innerK,
                                    l.stream().map(item -> flatRecordMap((RecordModel) item)).collect(Collectors.toList())
                            );
                        }
                    });
                }
            }
        });
        return sourceMap;
    }

    public <T> List<T> rQueryList(Class<T> tClass, QueryParam queryParam, MappingOptions options) {
        queryParam = queryParam == null ? QueryParam.create() : queryParam;
        queryParam.setSkipTotal(true);
        Page<T> tPage = rQueryPage(tClass, queryParam, options);
        return tPage.getItems();
    }

    public <T> List<T> rQueryList(Class<T> tClass, QueryParam queryParam, String... includeFields) {
        queryParam = queryParam == null ? QueryParam.create() : queryParam;
        queryParam.setSkipTotal(true);
        Page<T> tPage = rQueryPage(tClass, queryParam, includeFields);
        return tPage.getItems();
    }

    public <T> List<T> rQueryList(Class<T> tClass, String queryParam, String... includeFields) {
        return rQueryList(tClass, QueryParam.of(queryParam).setSkipTotal(true), includeFields);
    }

    public <T> List<T> rQueryList(Class<T> tClass, String queryParam, MappingOptions options) {
        Page<T> tPage = rQueryPage(tClass, QueryParam.of(queryParam).setSkipTotal(true), options);
        return tPage.getItems();
    }

    public <T> Page<T> rQueryPage(Class<T> tClass, String queryParam, MappingOptions options) {
        return rQueryPage(tClass, QueryParam.of(queryParam), options);
    }

    public <T> Page<T> rQueryPage(Class<T> tClass, String queryParam, String... includeFields) {
        return rQueryPage(tClass, queryParam, MappingOptions.create(ArrayUtil.isEmpty(includeFields)));
    }

    public <T> Page<T> rQueryPage(Class<T> tClass, QueryParam queryParam, String... includeFields) {
        Editor<String> keyEditor = null;
        if (ArrayUtil.isNotEmpty(includeFields)) {
            final Set<String> propertiesSet = CollUtil.set(false, includeFields);
            keyEditor = property -> propertiesSet.contains(property) ? property : null;
        }
        return rQueryPage(tClass, queryParam == null ? null : queryParam.toQueryStr(), MappingOptions.create(ArrayUtil.isEmpty(includeFields)).setFieldNameEditor(keyEditor));
    }

    public <T> Page<T> rQueryPage(Class<T> tClass, QueryParam qp, MappingOptions options) {
        Assert.notNull(tClass, "tClass cannot be null");
        CollectionModel collection = getClazzCollection(tClass);
        var result = this.rQueryPage(collection, qp, options);
        if (tClass == RecordModel.class) {
            return (Page<T>) result;
        }

        return convertToModelPage(result, tClass, options);
    }

    public Page<RecordModel> rQueryPage(CollectionModel collection, QueryParam qp, MappingOptions options) {


        RequestInfo requestInfo = createRequestInfo();

        // forbid users and guests to query special filter/sort fields
        checkForAdminOnlyRuleFields(requestInfo);

        if (requestInfo.getAdmin() == null && collection.getListRule() == null) {
            // only admins can access if the rule is nil
            throw new ForbiddenException("Only admins can perform this action.");
        }

        var fieldsResolver = new RecordFieldResolver(
                collection,
                requestInfo,
                // hidden fields are searchable only by admins
                requestInfo.getAdmin() != null
        );


        var searchProvider = new PbProvider(fieldsResolver).query(recordMapper.recordQuery(collection));

        if (requestInfo.getAdmin() == null && StrUtil.isNotEmpty(collection.getListRule())) {
            searchProvider.addFilter(new SearchFilter(collection.getListRule()));
        }
        Page<RecordModel> result;

        if (qp == null || qp.isEmpty()) {
            //从request 中获取 queryParams
            result = searchProvider.parseAndExec(collection);
        } else {
            //从入参中获取 queryParams
            result = searchProvider.parseAndExec(qp.toQueryStr(), collection);
        }


        PbUtil.post(new RecordsListEvent(collection, result));

        Error error = enrichRecords(result.getItems(), getSplitExpands(qp));
        if (error != null) {
            PbManager.getLog().error("Failed to expand: {}", error.getMessage());
        }

        return result;


    }

    private <T> CollectionModel getClazzCollection(Class<T> tClass) {
        String collNameOrId;

        PbCollection annotation = AnnotationUtil.getAnnotation(tClass, PbCollection.class);

        if (annotation == null || annotation.value().isEmpty()) {
            //尝试通过 className 获取Collection
            log.warn("Collection annotation not found in class {}", tClass.getName());
            collNameOrId = tClass.getSimpleName();
        } else {
            collNameOrId = annotation.value();
        }

        CollectionModel collection = collectionMapper.findCollectionByNameOrId(collNameOrId);
        if (collection == null) {
            throw new PbException("Collection not found: " + collNameOrId);
        }
        return collection;
    }

    private String[] getSplitExpands(QueryParam queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return new String[0];
        }
        return queryParams.getExpand() == null ? new String[0] : queryParams.getExpand().split(",", -1);
    }

    /**
     * 转换为 Model 类型的 page
     *
     * @param page
     * @param clazz
     * @param options
     * @param <T>     具体的Model类型
     * @return
     */
    private <T> Page<T> convertToModelPage(Page<RecordModel> page, Class<T> clazz, MappingOptions options) {
        return new Page<>(page.getPage(), page.getPerPage(), page.getTotalItems(), page.getTotalPages(),
                page.getItems().stream().map(i -> convertRecordToModel(i, clazz, options)).collect(Collectors.toList()));
    }

    private <T> T convertRecordToModel(RecordModel record, Class<T> clazz, MappingOptions options) {
        Map<String, Object> sourceMap = flatRecordMap(record);
        return BeanUtil.toBean(sourceMap, clazz, options == null ? null : options.toCopyOptions());
    }

    public <T> T rFindOne(String recordId, Class<T> tClass, String queryParams, String... includeFields) {
        Editor<String> keyEditor = null;
        if (ArrayUtil.isNotEmpty(includeFields)) {
            final Set<String> propertiesSet = CollUtil.set(false, includeFields);
            keyEditor = property -> propertiesSet.contains(property) ? property : null;
        }
        return rFindOne(recordId, tClass, queryParams, MappingOptions.create().setFieldNameEditor(keyEditor));
    }

    public <T> T rFindOne(String recordId, Class<T> tClass, QueryParam queryParams, String... includeFields) {
        return rFindOne(recordId, tClass, queryParams.toQueryStr(), includeFields);
    }

    public <T> T rFindOne(String recordId, Class<T> tClass, String queryParams, MappingOptions options) {
        return rFindOne(recordId, tClass, QueryParam.of(queryParams), options);
    }

    public <T> T rFindOne(String recordId, Class<T> tClass, QueryParam queryParams, MappingOptions options) {
        Assert.notNull(tClass, "tClass cannot be null");

        CollectionModel collection = getClazzCollection(tClass);

        var result = this.rFindOne(recordId, collection, queryParams, options);
        if (tClass == RecordModel.class) {
            return (T) result;
        }

        return convertRecordToModel(result, tClass, options);

    }

    public RecordModel rFindOne(String recordId, CollectionModel collection, QueryParam queryParams, MappingOptions options) {
        Assert.notNull(recordId, "recordId cannot be null");


        RequestInfo requestInfo = createRequestInfo();
        if (requestInfo.getAdmin() == null && collection.getViewRule() == null) {
            // only admins can access if the rule is nil
            throw new ForbiddenException("Only admins can perform this action.");
        }

        Consumer<SelectQuery> ruleFunc = selectQuery -> {
            if (requestInfo.getAdmin() == null && StrUtil.isNotEmpty(collection.getViewRule())) {
                RecordFieldResolver recordFieldResolver = new RecordFieldResolver(collection, requestInfo, true);
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

        Error error = enrichRecord(optionalRecordModel.get(), getSplitExpands(queryParams));
        if (error != null) {
            log.error("Failed to enrichRecord ,collection: {}, recordId: {},error: {}", collection, recordId, error.getMessage());
        }
        return optionalRecordModel.get();

    }

    public void rSave(Object data, String... includeFields) {
        //TODO
    }

    public void rSave(Object data, MappingOptions options) {
        //TODO
    }

    public void download(String collectionNameOrId, String recordId, String filename) {
        Assert.notNull(collectionNameOrId, "collectionNameOrId cannot be null");
        Assert.notNull(recordId, "recordId cannot be null");
        Assert.notNull(filename, "filename cannot be null");

        CollectionModel collection = collectionMapper.findCollectionByNameOrId(collectionNameOrId);
        if (collection == null) {
            throw new NotFoundException();
        }

        Optional<RecordModel> recordOptional = recordMapper.findRecordById(collection.getId(), recordId);
        if (recordOptional.isEmpty()) {
            throw new NotFoundException("No record found with the given ID: " + recordId);
        }

        RecordModel record = recordOptional.get();
        SchemaField fileField = record.findFileFieldByFile(filename);
        if (fileField == null) {
            throw new NotFoundException("No file found with the given name: " + filename);
        }

        if (!(fileField.getOptions() instanceof FileOptions options)) {
            throw new BadRequestException("Failed to load file options.");
        }

        // check whether the request is authorized to view the protected file
        if (options.getProtected()) {
            //TODO not implemented
            throw new PbException("not implemented");
        }

        String baseFilesPath = record.baseFilesPath();

        // fetch the original view file field related record
        if (collection.isView()) {
            try {
                var fileRecord = recordMapper.findRecordByViewFile(collection, fileField.getName(), filename);
                if (fileRecord != null) {
                    baseFilesPath = fileRecord.baseFilesPath();
                }
            } catch (Exception e) {
                throw new BadRequestException("Failed to fetch view file field record. detail error: " + e.getMessage());
            }

        }

        PbFileSystem fileSystem = PbManager.getPbFileSystem();

        var originalPath = Paths.get(baseFilesPath, filename);
        var servedPath = originalPath;
        var servedName = filename;
        var thumb = PbHolder.getRequest().getParameter("thumb");

        if (StrUtil.isNotEmpty(thumb) && (ListUtil.existInArray(thumb, DEFAULT_THUMB_SIZES) || ListUtil.existInArray(thumb, options.getThumbs().toArray(new String[0])))) {
            // extract the original file meta attributes and check it existence
            PbFile file;
            try {
                file = fileSystem.getFile(originalPath.toString());
            } catch (Exception e) {
                throw new BadRequestException("File not found.");
            }
            // check if it is an image
            if (ListUtil.existInArray(file.getContentType(), IMAGE_CONTENT_TYPES)) {
                // add thumb size as file suffix
                servedName = thumb + "_" + filename;
                servedPath = Paths.get(baseFilesPath, THUMB_PREFIX + filename, servedName);

                if (!fileSystem.exists(servedPath.toString())) {
                    try {
                        this.createThumb(fileSystem, originalPath.toString(), servedPath.toString(), thumb);
                    } catch (Exception e) {
                        log.error("Fallback to original - failed to create thumb {} due to error. Original: {}, Thumb: {}",
                                servedName,
                                originalPath,
                                servedPath,
                                e);
                        // fallback to the original
                        servedName = filename;
                        servedPath = originalPath;
                    }

                }
            }
        }

        // clickjacking shouldn't be a concern when serving uploaded files,
        // so it safe to unset the global X-Frame-Options to allow files embedding
        // (note: it is out of the hook to allow users to customize the behavior)
        PbHolder.getResponse().setHeader("X-Frame-Options", null);

        try {
            fileSystem.serve(servedPath.toString(), servedName);
        } catch (Exception e) {
            throw new BadRequestException("File serve failed.");
        }


    }

    private void createThumb(PbFileSystem fileSystem, String originalPath, String thumbPath, String thumbSize) throws Exception {
        CompletableFuture<Void> future = thumbGenPending.computeIfAbsent(thumbPath, path -> CompletableFuture.supplyAsync(() -> {
            try {
                if (!thumbGenSem.tryAcquire(thumbGenMaxWait.toSeconds(), TimeUnit.SECONDS)) {
                    throw new RuntimeException("Timeout acquiring semaphore.");
                }
            } catch (Exception e) {
                throw new BadRequestException("acquiring semaphore failed.");
            }

            try {
                fileSystem.createThumb(originalPath, thumbPath, thumbSize);
            } catch (Exception e) {
                throw new BadRequestException("create thumb file failed.");
            } finally {
                thumbGenSem.release();
            }

            return null;
        }, executor));

        future.whenComplete((result, ex) -> {
            thumbGenPending.remove(thumbPath); // 完成后从pending列表中移除
            if (ex != null) {
                throw new RuntimeException(ex);
            }
        }).get(thumbGenMaxWait.toSeconds(), TimeUnit.SECONDS); // 等待生成完成，超时则抛出异常
    }
}

