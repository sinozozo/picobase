package com.picobase.core.scheduler;

import com.picobase.annotation.PbScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Component
public class TestScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TestScheduler.class);

    @PbScheduler(cron = "0/5 * * * * ?")
    public void cronScheduler1() {
        logger.info("scheduler1 每5秒时间调度任务");
    }

    @PbScheduler(cron = "0,10,20,40 * * * * ?")
    public void cronScheduler2() {
        logger.info("scheduler2 每分钟的10秒，20秒，40秒调度任务");
    }
}
