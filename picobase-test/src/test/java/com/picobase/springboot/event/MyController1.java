
package com.picobase.springboot.event;

import com.picobase.annotation.PbEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyController1 {

    private static final Logger logger = LoggerFactory.getLogger(MyController1.class);

    // 事件会被当前线程立刻执行，注意日志打印的线程号
    @PbEventReceiver
    public void onMyNoticeEvent(MyNoticeEvent event) {
        logger.info("方法1同步执行事件：" + event.getMessage());
    }

}
