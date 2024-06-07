package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.AdminModel;

public class AdminAuthWithPasswordEvent implements PbEvent {

    /**
     * 请求 from 的 identity
     */
    public String identity;

    /**
     * 请求 from 的 password
     */
    public String password;

    /**
     * 从 DB or 配置文件中获取到的 adminModel
     */
    public AdminModel adminModel;

    /**
     * 事件触发时机
     */
    public TimePosition timePosition;

    public AdminAuthWithPasswordEvent(String identity, String password, AdminModel adminModel, TimePosition timePosition) {
        this.identity = identity;
        this.password = password;
        this.adminModel = adminModel;
        this.timePosition = timePosition;
    }

}
