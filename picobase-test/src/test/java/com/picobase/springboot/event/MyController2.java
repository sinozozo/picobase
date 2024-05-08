
package com.picobase.springboot.event;

import com.picobase.annotation.PbEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyController2 {

    private static final Logger logger = LoggerFactory.getLogger(MyController2.class);

    /**
     * 同一个事件可以被重复注册和接受
     * 异步事件会被不会立刻执行，注意日志打印的线程号
     */
    @PbEventReceiver(isAsync = true)
    public void onMyNoticeEvent(MyNoticeEvent event) {
        logger.info("方法2异步执行事件：" + event.getMessage());
    }

}
