package com.picobase.console.eventhandler;

import com.picobase.annotation.PbEventReceiver;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.model.event.CollectionDeleteEvent;
import com.picobase.model.event.CollectionUpdateEvent;
import com.picobase.model.event.TimePosition;
import org.springframework.stereotype.Service;

@Service
public class CollectionEventHandler {


    private final CollectionMapper collectionMapper;

    public CollectionEventHandler(CollectionMapper collectionMapper) {
        this.collectionMapper = collectionMapper;
    }


    @PbEventReceiver
    public void onCollectionUpdateEvent(CollectionUpdateEvent event) {
        if (event.timePosition == TimePosition.AFTER) {
            collectionMapper.removeCache(event.collection.getId());
            collectionMapper.removeCache(event.collection.getName());
        }

    }

    @PbEventReceiver
    public void onCollectionDeleteEvent(CollectionDeleteEvent event) {
        if (event.timePosition == TimePosition.AFTER) {
            collectionMapper.removeCache(event.collection.getId());
            collectionMapper.removeCache(event.collection.getName());
        }

    }

}
