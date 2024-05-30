package com.picobase.console.web.interceptor;

import cn.hutool.core.util.ArrayUtil;
import com.picobase.PbUtil;
import com.picobase.exception.NotFoundException;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.model.CollectionModel;
import com.picobase.persistence.mapper.PbMapperManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

public class LoadCollectionInterceptor implements HandlerInterceptor {

    public static final String VARIABLES_ATTRIBUTE_COLLECTION_NAME_OR_ID = "collectionIdOrName";

    private CollectionMapper mapper; //这里没办法直接使用 PbManager获取 PbMapperManager ，因为inject这个组件的过程可能在LoadCollectionInterceptor初始化之后，导致空指针

    public LoadCollectionInterceptor(PbMapperManager pmm) {
        this.mapper = pmm.findMapper(CollectionModel.class);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            //HttpMethod method = HttpMethod.resolve(request.getMethod());
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method methodObject = handlerMethod.getMethod();

            if (methodObject.isAnnotationPresent(LoadCollection.class)) {
                Map attribute = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                String collectionIdOrName = attribute.get(VARIABLES_ATTRIBUTE_COLLECTION_NAME_OR_ID).toString();

                // Fetch collection using your application logic
                CollectionModel collection = mapper.findCollectionByNameOrId(collectionIdOrName);
                if (collection == null) {
                    throw new NotFoundException("Collection not found");
                }

                LoadCollection annotation = methodObject.getAnnotation(LoadCollection.class);
                if (collection != null && (annotation.optCollectionTypes().length == 0 || ArrayUtil.contains(annotation.optCollectionTypes(), collection.getType()))) {
                    //将Collection存储到storage ， 底层 request setAttribute中
                    PbUtil.setCollectionToStorage(collection);
                } else {
                    throw new RuntimeException("Collection not found or invalid type");
                }
            }
        }
        return true;
    }

}