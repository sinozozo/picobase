package com.picobase.console.web;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.console.PbConsoleConstants;
import com.picobase.console.model.LogModel;
import com.picobase.context.PbHolder;
import com.picobase.log.PbLog;
import com.picobase.model.FailureResult;
import com.picobase.model.RequestInfo;
import com.picobase.util.PbConstants;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.picobase.console.PbConsoleConstants.ErrorResponse;
import static com.picobase.console.util.HttpUtil.getFullRequestUrl;
import static com.picobase.console.util.HttpUtil.realUserIp;
import static com.picobase.util.PbConstants.REQUEST_INFO_KEY;

@Order(PbConstants.ASSEMBLY_ORDER - 1)
public class LogFilter implements Filter {

    private PbLog log = PbManager.getLog();


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String fullRequestUrl = getFullRequestUrl(request);
        log.info("{} {}", ((RequestFacade) request).getMethod(), fullRequestUrl);

        LogModel logModel = new LogModel();
        logModel.refreshId();
        logModel.setMessage(fullRequestUrl);
        logModel.setCreated(DateUtil.toLocalDateTime(new Date()));
        logModel.refreshUpdated();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        chain.doFilter(request, response); //执行 chain 调用
        stopWatch.stop();

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        RequestInfo requestInfo = (RequestInfo) PbHolder.getStorage().get(REQUEST_INFO_KEY);

        String auth = PbConsoleConstants.RequestAuthGuest;
        if (requestInfo == null) {
            auth = PbConsoleConstants.RequestAuthGuest;
        } else if (requestInfo.getAuthRecord() != null) {
            auth = PbConsoleConstants.RequestAuthRecord;
        } else if (requestInfo.getAdmin() != null) {
            auth = PbConsoleConstants.RequestAuthAdmin;
        }

        FailureResult failureResult = null;
        if (res.getStatus() > 200) {
            failureResult = (FailureResult) PbHolder.getStorage().get(ErrorResponse);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("execTime", stopWatch.getTotalTimeMillis());
        data.put("type", "request");
        data.put("auth", auth);
        data.put("status", res.getStatus());
        data.put("method", req.getMethod().toUpperCase());
        data.put("url", getFullRequestUrl(request));
        data.put("referer", req.getHeader("referer"));
        data.put("remoteIp", request.getRemoteAddr());
        data.put("userIp", realUserIp(req));
        data.put("userAgent", req.getHeader("user-agent"));
        if (failureResult != null) {
            data.put("error", failureResult.getMessage());
            data.put("details", failureResult.getData());
        }
        logModel.setData(data);

        if (res.getStatus() == 200) {
            logModel.setLevel(0);
        } else {
            logModel.setLevel(8);
        }


        PbUtil.post(logModel); //异步执行该日志信息的处理

    }


}
