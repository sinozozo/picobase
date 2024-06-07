package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.CollectionModel;

public class CollectionCreateEvent implements PbEvent {

    public CollectionModel collection;
    public TimePosition timePosition;

    public CollectionCreateEvent(CollectionModel collection, TimePosition timePosition) {
        this.collection = collection;
        this.timePosition = timePosition;
    }
}
