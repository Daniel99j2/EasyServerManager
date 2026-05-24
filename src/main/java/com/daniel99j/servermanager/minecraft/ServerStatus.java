package com.daniel99j.servermanager.minecraft;

public enum ServerStatus {
    OFFLINE(false),
    STARTING(true),
    ONLINE(true),
    STOPPING(true),
    CRASHED(false),
    KILLED(false),
    MANUALLY_STARTED(true),
    HOST_REBOOTED(false);

    private final boolean shouldBeRunning;

    ServerStatus(boolean shouldBeRunning) {
        this.shouldBeRunning = shouldBeRunning;
    }

    public boolean shouldBeRunning() {
        return shouldBeRunning;
    }
}
