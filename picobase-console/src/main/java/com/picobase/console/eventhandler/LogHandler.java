package com.picobase.console.eventhandler;

import cn.hutool.core.date.DateUtil;
import com.picobase.PbManager;
import com.picobase.annotation.PbEventReceiver;
import com.picobase.annotation.PbScheduler;
import com.picobase.console.mapper.LogMapper;
import com.picobase.console.model.LogModel;
import com.picobase.log.PbLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static com.picobase.console.PbConsoleRegister.CONFIG_PREFIX;

/**
 * 日志处理组件,仅适用于开发环境调试，生产环境会存在分布式问题且会影响系统性能
 */
@Service
@ConditionalOnProperty(CONFIG_PREFIX + ".isDev")
public class LogHandler {

    private static final int BATCH_SIZE = 1000;
    private static final int CAPACITY = 10000;

    private final Queue<LogModel> queue = new ArrayBlockingQueue<>(CAPACITY);

    private LogMapper logMapper;
    private PbLog log = PbManager.getLog();


    LogHandler(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @PbEventReceiver(isAsync = true)
    public void onLogModel(LogModel log) {
        if (log.getMessage().matches(".*/(api/logs|console/).*")) { //忽略 log 相关的请求
            return;
        }

        log.setRowid(DateUtil.date(log.getCreated()).getTime());
        queue.offer(log);
    }

    /**
     * 每三十秒批量保存日志数据
     */
    @PbScheduler(cron = "0/30 * * * * ?")
    public void cronBatchSaveLogs() {

        List<LogModel> list = new ArrayList<>();
        while (true) {
            LogModel log = queue.poll();
            if (log == null || list.size() == BATCH_SIZE) {
                break;
            }
            list.add(log);
        }
        if (list.isEmpty()) {
            return;
        }
        log.debug("cronBatchSaveLogs log size: {}", list.size());
        logMapper.batchSave(list);
    }

    /**
     * 定时删除七天前的日志
     */
    @PbScheduler(cron = "0 0 10 * * ?") //每天10点执行
    public void cronDeleteLogs() {
        log.debug("cronDeleteLogs.");
        logMapper.deleteBeforeTime(DateUtil.offsetDay(new Date(), -7)); //删除七天前的数据
    }
}
