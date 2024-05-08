package com.picobase.servlet.model;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import com.picobase.context.model.PbRequest;
import com.picobase.exception.PbException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 带有 Content 缓存的 PbRequest
 */
public abstract class PbRequestWithContentCache implements PbRequest {

    private ContentCachingRequestWrapper request;

    public PbRequestWithContentCache(HttpServletRequest request){
        this.request = new ContentCachingRequestWrapper(request);
    }

    public byte[] getCachedContent(){
        if(ArrayUtil.isEmpty(request.getContentAsByteArray())){
            try {
                //读取 inputStream
                return IoUtil.readBytes(request.getInputStream());
            } catch (IOException e) {
                throw new PbException("Failed to read request content", e);
            }
        }
        return request.getContentAsByteArray();
    }
}
