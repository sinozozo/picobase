package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.CollectionModel;
import com.picobase.model.RecordModel;

public class RecordViewEvent implements PbEvent {
    public CollectionModel collection;
    public RecordModel record;

    public RecordViewEvent(CollectionModel collection, RecordModel record) {
        this.collection = collection;
        this.record = record;
    }
}
