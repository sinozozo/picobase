
package com.picobase.springboot.event;


import cn.hutool.core.thread.ThreadUtil;
import com.picobase.PbUtil;
import com.picobase.StartUpApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = StartUpApplication.class)
public class PbEventTest {

    // Event Bus tutorial, the core idea is the observer pattern.(核心思想是设计模式中的观察者模式)
    @Test
    public void startEventTest() {

        PbUtil.post(MyNoticeEvent.valueOf("我的事件"));

        ThreadUtil.sleep(1000);
    }

}
