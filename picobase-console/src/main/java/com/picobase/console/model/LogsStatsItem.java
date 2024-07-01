package com.picobase.console.model;

import java.time.LocalDateTime;

public class LogsStatsItem {
    private int total;
    private LocalDateTime date;

    public int getTotal() {
        return total;
    }

    public LogsStatsItem setTotal(int total) {
        this.total = total;
        return this;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public LogsStatsItem setDate(LocalDateTime date) {
        this.date = date;
        return this;
    }
}
