package com.picobase.context;


import com.picobase.context.model.PbRequest;
import com.picobase.context.model.PbResponse;
import com.picobase.context.model.PbStorage;
import com.picobase.error.PbErrorCode;
import com.picobase.exception.InvalidContextException;

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * Picobase 上下文处理器 [ 默认实现类 ]
 *
 * <p>
 * 一般情况下框架会为你自动注入合适的上下文处理器，如果代码断点走到了此默认实现类，
 * </p>
 */
public class PbContextDefaultImpl implements PbContext {

    /**
     * 默认的上下文处理器对象
     */
    public static PbContextDefaultImpl defaultContext = new PbContextDefaultImpl();

    /**
     * 错误提示语
     */
    public static final String ERROR_MESSAGE = "未能获取有效的上下文处理器";

    @Override
    public PbRequest getRequest() {
        throw new InvalidContextException(ERROR_MESSAGE).setCode(PbErrorCode.CODE_10001);
    }

    @Override
    public PbResponse getResponse() {
        throw new InvalidContextException(ERROR_MESSAGE).setCode(PbErrorCode.CODE_10001);
    }

    @Override
    public PbStorage getStorage() {
        throw new InvalidContextException(ERROR_MESSAGE).setCode(PbErrorCode.CODE_10001);
    }

    @Override
    public boolean matchPath(String pattern, String path) {
        throw new InvalidContextException(ERROR_MESSAGE).setCode(PbErrorCode.CODE_10001);
    }

    @Override
    public <T> Optional<T> createObjFromRequest(Class<T> dto) {
        throw new InvalidContextException(ERROR_MESSAGE).setCode(PbErrorCode.CODE_10001);
    }

    @Override
    public void bindRequestTo(Object obj) {
        throw new InvalidContextException(ERROR_MESSAGE).setCode(PbErrorCode.CODE_10001);
    }


}
