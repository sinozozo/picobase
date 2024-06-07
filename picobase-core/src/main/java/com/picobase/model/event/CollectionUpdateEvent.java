package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.CollectionModel;

public class CollectionUpdateEvent implements PbEvent {

    public CollectionModel collection;
    public TimePosition timePosition;

    public CollectionUpdateEvent(CollectionModel collection, TimePosition timePosition) {
        this.collection = collection;
        this.timePosition = timePosition;
    }
}
