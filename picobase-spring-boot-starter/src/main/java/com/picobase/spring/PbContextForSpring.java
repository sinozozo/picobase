package com.picobase.spring;

import cn.hutool.core.convert.Convert;
import com.picobase.PbManager;
import com.picobase.context.PbContext;
import com.picobase.context.model.PbRequest;
import com.picobase.context.model.PbResponse;
import com.picobase.context.model.PbStorage;
import com.picobase.exception.PbException;
import com.picobase.servlet.model.PbRequestForServlet;
import com.picobase.servlet.model.PbResponseForServlet;
import com.picobase.servlet.model.PbStorageForServlet;
import com.picobase.spring.pathmatch.PbPatternsRequestConditionHolder;

import java.lang.reflect.*;
import java.util.*;

import static org.springframework.http.MediaType.*;

/**
 * 上下文处理器 [ SpringMVC版本实现 ]。在 SpringMVC、SpringBoot 中使用 Picobase 时，必须注入此实现类，否则会出现上下文无效异常
 */
public class PbContextForSpring implements PbContext {




    /**
     * 获取当前请求的 Request 包装对象
     */
    @Override
    public PbRequest getRequest() {
        return new PbRequestForServlet(SpringMVCUtil.getRequest());
    }

    /**
     * 获取当前请求的 Response 包装对象
     */
    @Override
    public PbResponse getResponse() {
        return new PbResponseForServlet(SpringMVCUtil.getResponse());
    }

    /**
     * 获取当前请求的 Storage 包装对象
     */
    @Override
    public PbStorage getStorage() {
        return new PbStorageForServlet(SpringMVCUtil.getRequest());
    }

    /**
     * 判断：指定路由匹配符是否可以匹配成功指定路径
     */
    @Override
    public boolean matchPath(String pattern, String path) {
        return PbPatternsRequestConditionHolder.match(pattern, path);
    }

    /**
     * 判断：在本次请求中，此上下文是否可用。
     */
    @Override
    public boolean isValid() {
        return SpringMVCUtil.isWeb();
    }

    /**
     * 将 request 数据(path params, query params and the request body)绑定到对象 form 上 （支持提交的数据格式为 form 或 json）
     * 如果类型转换异常则取默认值， 包装类取 null ， 非包装类取基础类型默认值
     *
     * @param dto 待进行数据绑定的对象，支持的类型为 原始 pojo 或 Map<String,Object> 类型
     */
    @Override
    public <T> Optional<T> bindRequest(Class<T> dto) {

        //为了避免在处理请求时出现意外行为，只将GET/DELETE/HEAD请求中的查询参数与目标结构进行绑定，不从请求体中进行绑定。
        // 举个例子，如果请求URL中包含&id=1&lang=en，而请求体中包含{"id":100,"lang":"de"}，如果同时从URL和请求体中绑定参数的话会导致优先级问题，
        // 所以为了避免这种情况，只对GET/DELETE/HEAD请求中的查询参数进行绑定。
        PbRequest request = getRequest();
        String method = request.getMethod();

        // Bind query parameters for GET/DELETE/HEAD requests
        if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("DELETE") || method.equalsIgnoreCase("HEAD")) {

            if (dto == Map.class) {
                Map map = new HashMap();
                map.putAll(request.getParamMap());
                return (Optional<T>) Optional.of(map);
            }


            return requestToObject(request, dto);
        }

        //从 http body 中处理请求数据 进行绑定。

        if (request.getContentLength() == 0) {
            return null;
        }
        // Bind request body data if present
        if (request.getContentType().startsWith(APPLICATION_JSON_VALUE)) {
            String json = new String(request.getCachedContent());
            return Optional.of(PbManager.getPbJsonTemplate().parseJsonToObject(json, dto));
        } else if (request.getContentType().startsWith(APPLICATION_FORM_URLENCODED_VALUE) || request.getContentType().startsWith(MULTIPART_FORM_DATA_VALUE)) {
            if (dto == Map.class) {
                Map map = new HashMap();
                map.putAll(request.getParamMap());
                return (Optional<T>) Optional.of(map);
            }
            return requestToObject(request, dto);
        }

        return Optional.empty();
    }




    /**
     * 将 request 转换为 DTO
     *
     * @param request
     * @param clazz
     * @param <T>
     * @return
     */
    private static <T> Optional<T> requestToObject(PbRequest request, Class<T> clazz) {
        if (clazz == null) {
            return Optional.empty();
        }

        try {
            Map<String, String[]> map = request.getParamMap();
            T instance = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                Class<?> type = field.getType();


                String[] paramValue = map.get(field.getName());

                if (paramValue == null || paramValue.length == 0) {
                    continue;
                }

                Type genericType = field.getGenericType();
                Type actualGenericType = null;//实际的泛型信息
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericType;
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();

                    actualGenericType = typeArguments[0];
                }

                Object value = convertToType(paramValue, type, actualGenericType);
                field.set(instance, value);
            }

            return Optional.of(instance);
        } catch (Exception e) {
            throw new PbException("Mapping to " + clazz.getName() + " failed: " + e.getMessage(), e);
        }
    }

    /**
     * 将 values 数组转换为特定类型对象
     *
     * @param values            待映射的数组
     * @param type              当前field 的类型
     * @param actualGenericType 实际的泛型信息 ， 不包含泛型信息则直接转换
     * @return
     */
    private static Object convertToType(String[] values, Class<?> type, Type actualGenericType) {

        if (!type.isArray() && !Collection.class.isAssignableFrom(type)) {
            //基本类型
            return Convert.convert(type,values[0]);

        }else if (type == List.class) {
            List<Object> list = new ArrayList<>();
            if (actualGenericType != null) {
                for (String value : values) {
                    list.add(convertToType(new String[]{value}, (Class<?>) actualGenericType, null));
                }
                return list;
            } else {
                return list.addAll(Arrays.asList(values));
            }
        } else if (type == Set.class) {
            Set<Object> set = new HashSet<>();
            if (actualGenericType != null) {
                for (String value : values) {
                    set.add(convertToType(new String[]{value}, (Class<?>) actualGenericType, null));
                }
                return set;
            } else {
                return new HashSet<>(Arrays.asList(values));
            }
        } else if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            Object array = Array.newInstance(componentType, values.length);
            for (int i = 0; i < values.length; i++) {
                Object intValue = convertToType(new String[]{values[i]}, componentType, null);
                // 安全地将Integer[]数组的值复制到int[]数组中
                if (componentType.isPrimitive()) {
                    Array.set(array, i, intValue==null?0:intValue);
                }else{
                    Array.set(array, i, intValue);
                }
            }
            return array;
        } else {
            // 未知类型
            return values[0];
        }
    }


}
