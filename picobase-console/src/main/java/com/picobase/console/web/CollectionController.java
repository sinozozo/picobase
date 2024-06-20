package com.picobase.console.web;


import com.picobase.PbUtil;
import com.picobase.exception.BadRequestException;
import com.picobase.interceptor.InterceptorFunc;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.model.CollectionModel;
import com.picobase.model.CollectionUpsert;
import com.picobase.model.event.*;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.FieldResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {


    private static final Logger log = LoggerFactory.getLogger(CollectionController.class);

    private CollectionMapper mapper;

    public CollectionController(CollectionMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public Page<CollectionModel> list() {
        var fieldResolver = FieldResolver.newSimpleFieldResolver(
                "id", "created", "updated", "name", "system", "type"
        );

        Page<CollectionModel> result = PbUtil.queryPage(fieldResolver, CollectionModel.class);

        CollectionsListEvent event = new CollectionsListEvent(result.getItems(), result);
        PbUtil.post(event);

        return result;
    }

    @PostMapping
    public CollectionModel create() {

        //待保存
        CollectionModel collection = new CollectionModel();

        CollectionUpsert form = new CollectionUpsert(collection);

        // load request
        PbUtil.bindRequestTo(form);

        InterceptorFunc<CollectionModel, CollectionModel> postEventFunc = next -> collectionModel -> {
            PbUtil.post(new CollectionCreateEvent(collectionModel, TimePosition.BEFORE));
            CollectionModel cm = next.run(collectionModel);
            PbUtil.post(new CollectionCreateEvent(cm, TimePosition.AFTER));
            return cm;
        };

        return form.submit(true, postEventFunc);
    }

    @PatchMapping("{collectionIdOrName}")
    public CollectionModel update(@PathVariable String collectionIdOrName) {
        //待保存

        CollectionModel collection = new CollectionModel(mapper.findCollectionByNameOrId(collectionIdOrName)); // findCollectionByNameOrId 返回的是缓存对象， 复制一份新的 Collection ，防止绑定参数时修改了缓存对象。

        CollectionUpsert form = new CollectionUpsert(collection);

        // load request
        PbUtil.bindRequestTo(form);

        InterceptorFunc<CollectionModel, CollectionModel> postEventFunc = next -> collectionModel -> {
            PbUtil.post(new CollectionUpdateEvent(collectionModel, TimePosition.BEFORE));
            CollectionModel cm = next.run(collectionModel);
            PbUtil.post(new CollectionUpdateEvent(cm, TimePosition.AFTER));
            return cm;
        };

        return form.submit(false, postEventFunc);
    }

    @GetMapping(value = "{collectionIdOrName}")
    public CollectionModel view(@PathVariable String collectionIdOrName) {
        CollectionModel collection = mapper.findCollectionByNameOrId(collectionIdOrName);
        PbUtil.post(new CollectionViewEvent(collection));
        return collection;
    }


    /**
     * TODO 【bug】当存在一个普通 Collection  A, 和一个依赖 A 的 View Collection B， 直接删除 A时会报错。
     */
    @DeleteMapping(value = "{collectionIdOrName}")
    public void delete(@PathVariable String collectionIdOrName) {
        CollectionModel collection = mapper.findCollectionByNameOrId(collectionIdOrName);
        if (collection == null) {
            throw new BadRequestException("the requested resource wasn't found.");
        }
        PbUtil.post(new CollectionDeleteEvent(collection, TimePosition.BEFORE));


        try {
            mapper.deleteCollection(collection);
        } catch (Exception e) {
            throw new BadRequestException("Failed to delete collection due to existing dependency.");
        }
        PbUtil.post(new CollectionDeleteEvent(collection, TimePosition.AFTER));

    }


}
