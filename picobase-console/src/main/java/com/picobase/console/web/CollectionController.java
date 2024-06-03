package com.picobase.console.web;


import com.picobase.PbUtil;
import com.picobase.console.event.*;
import com.picobase.exception.BadRequestException;
import com.picobase.interceptor.InterceptorFunc;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.model.CollectionModel;
import com.picobase.model.CollectionUpsert;
import com.picobase.persistence.mapper.PbMapper;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.FieldResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {


    private static final Logger log = LoggerFactory.getLogger(CollectionController.class);

    private CollectionMapper mapper;
    public CollectionController(PbMapperManager mapperManager) {
        this.mapper = mapperManager.findMapper(CollectionModel.class);
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
        CollectionModel collection = mapper.findCollectionByNameOrId(collectionIdOrName);

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


    @DeleteMapping(value = "{collectionIdOrName}")
    public void delete(@PathVariable String collectionIdOrName) {
        CollectionModel collection = mapper.findCollectionByNameOrId(collectionIdOrName);
        if (collection == null) {
            throw new BadRequestException("the requested resource wasn't found.");
        }
        PbUtil.post(new CollectionDeleteEvent(collection, TimePosition.BEFORE));


        mapper.deleteCollection(collection);
        PbUtil.post(new CollectionDeleteEvent(collection, TimePosition.AFTER));

    }


}
