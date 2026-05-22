package com.daniel99j.servermanager.minecraft;

public class LogLineInfo<T> {
    private final T data;

    public static final LogLineInfo COMMAND = new LogLineInfo(JoinData);
    CHAT(null),
    INFO(null),
    ERROR(null),
    WARN(null),
    FATAL(null),
    DEBUG(null),
    JOIN(null),
    LEAVE(null);

    LogLineInfo(T data) {
        this.data = data;
    }
}
