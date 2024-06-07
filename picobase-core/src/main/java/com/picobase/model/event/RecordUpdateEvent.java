package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.file.PbFile;
import com.picobase.model.CollectionModel;
import com.picobase.model.RecordModel;

import java.util.List;
import java.util.Map;

public class RecordUpdateEvent implements PbEvent {
    public CollectionModel collection;
    public RecordModel record;
    public Map<String, List<PbFile>> uploadedFiles;
    public TimePosition timePosition;

    public RecordUpdateEvent(CollectionModel collection, RecordModel record, Map<String, List<PbFile>> uploadedFiles, TimePosition timePosition) {
        this.collection = collection;
        this.record = record;
        this.uploadedFiles = uploadedFiles;
        this.timePosition = timePosition;
    }
}
