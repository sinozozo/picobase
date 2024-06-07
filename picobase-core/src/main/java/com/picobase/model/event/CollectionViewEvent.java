package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.CollectionModel;

public class CollectionViewEvent implements PbEvent {
    public CollectionModel collection;

    public CollectionViewEvent(CollectionModel collection) {
        this.collection = collection;
    }
}
