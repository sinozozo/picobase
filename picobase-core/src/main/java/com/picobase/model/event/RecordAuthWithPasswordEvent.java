package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.CollectionModel;
import com.picobase.model.RecordModel;

public class RecordAuthWithPasswordEvent implements PbEvent {

    public CollectionModel collection;
    public String identity;

    public String password;

    public RecordModel record;

    public TimePosition timePosition;

    public RecordAuthWithPasswordEvent(CollectionModel collection, String identity, String password, RecordModel record, TimePosition timePosition) {
        this.collection = collection;
        this.identity = identity;
        this.password = password;
        this.record = record;
        this.timePosition = timePosition;
    }
}
