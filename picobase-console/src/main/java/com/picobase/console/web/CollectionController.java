package com.picobase.console.web;


import com.picobase.PbUtil;
import com.picobase.console.event.CollectionsListEvent;
import com.picobase.console.mapper.CollectionMapper;
import com.picobase.model.CollectionModel;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.FieldResolver;
import com.picobase.search.PbProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private CollectionMapper mapper;

    public CollectionController(PbMapperManager mapperManager) {
        this.mapper = mapperManager.findMapper(CollectionModel.class);
    }
    @GetMapping
    public Page<CollectionModel> list(){
        var fieldResolver = FieldResolver.newSimpleFieldResolver(
                "id", "created", "updated", "name", "system", "type"
        );

        Page<CollectionModel> result = new PbProvider(fieldResolver).query(mapper.modelQuery()).parseAndExec(CollectionModel.class);
        CollectionsListEvent event = new CollectionsListEvent();
        event.collections = result.getItems();
        event.result = result;
        PbUtil.post(event);

        return result;
    }

    @PostMapping
    public CollectionModel create(){

        return null;
    }


}
