package com.picobase.logic;

import cn.hutool.core.annotation.AnnotationUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.annotation.PbCollection;
import com.picobase.log.PbLog;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.logic.mapper.RecordMapper;
import com.picobase.model.CollectionModel;
import com.picobase.model.RecordModel;
import com.picobase.persistence.repository.Page;

public class PbRecordLogic {
    private static final PbLog log = PbManager.getLog();

    private final RecordMapper recordMapper = PbUtil.findMapper(RecordModel.class);

    public <T> Page<T> rQuery(Class<T> tClass){
        PbCollection annotation = AnnotationUtil.getAnnotation(tClass, PbCollection.class);
        String collNameOrId;
        if (annotation==null||annotation.value().isEmpty()){
            //尝试通过 className 获取Collection
            log.warn("Collection annotation not found in class {}",tClass.getName());
            collNameOrId = tClass.getSimpleName();
        }else {
            collNameOrId = annotation.value();
        }





        return null;
    }


}
