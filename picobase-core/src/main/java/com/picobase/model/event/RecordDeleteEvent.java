package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.CollectionModel;
import com.picobase.model.RecordModel;

public class RecordDeleteEvent implements PbEvent {
    public CollectionModel collection;
    public RecordModel record;

    public TimePosition timePosition;

    public RecordDeleteEvent(CollectionModel collection, RecordModel record, TimePosition timePosition) {
        this.collection = collection;
        this.record = record;
        this.timePosition = timePosition;
    }
}
