package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.CollectionModel;
import com.picobase.persistence.repository.Page;

import java.util.List;

public class CollectionsListEvent implements PbEvent {

    public List<CollectionModel> collections;
    public Page<CollectionModel> result;

    public CollectionsListEvent(List<CollectionModel> items, Page<CollectionModel> result) {
        this.collections = items;
        this.result = result;
    }
}
