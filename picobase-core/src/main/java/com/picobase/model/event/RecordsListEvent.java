package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.CollectionModel;
import com.picobase.model.RecordModel;
import com.picobase.persistence.repository.Page;

public class RecordsListEvent implements PbEvent {
    public CollectionModel collection;
    public Page<RecordModel> result;


    public RecordsListEvent(CollectionModel collection, Page<RecordModel> result) {
        this.collection = collection;
        this.result = result;
    }

}
