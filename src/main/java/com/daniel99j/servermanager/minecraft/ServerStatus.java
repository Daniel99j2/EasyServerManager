package com.daniel99j.servermanager.minecraft;

public enum ServerStatus {
    OFFLINE(false),
    STARTING(true),
    RUNNING(true),
    STOPPING(true),
    CRASHED(false),
    STARTING_TO_REBOOT(false),
    KILLED(false),
    HOST_REBOOTED(false);

    private final boolean shouldBeRunning;

    ServerStatus(boolean shouldBeRunning) {
        this.shouldBeRunning = shouldBeRunning;
    }

    public boolean shouldBeRunning() {
        return shouldBeRunning;
    }
}
