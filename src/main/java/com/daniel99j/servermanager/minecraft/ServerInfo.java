package com.daniel99j.servermanager.minecraft;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.servermanager.CommandUtil;
import com.daniel99j.servermanager.Config;
import com.daniel99j.servermanager.Main;
import com.daniel99j.servermanager.WebhookSender;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerInfo {
    public static ServerStatus currentStatus = ServerStatus.OFFLINE;
    private static int processId = -1;
    public static LogManager logManager;

    public static void start() {

    }

    public static void refreshStatus() {
        boolean serverActuallyRunning = CommandUtil.pingMinecraft(Config.INSTANCE.getServerIp(), Config.INSTANCE.getServerPort());

        //first if thinking running then check process id
        if(currentStatus.shouldBeRunning() && !serverActuallyRunning) {
            if(CommandUtil.execute("pwdx "+processId).contains("/var/home/dj/.gradle")) return;
            //the process is not valid! find cause.

            //host server rebooted
            if(CommandUtil.execute("tmux capture-pane -pt test ").contains("no sessions")) {
                currentStatus = ServerStatus.HOST_REBOOTED;
                checkReboot();
                return;
            };

            //used too much memory
            if(CommandUtil.execute("tmux capture-pane -pt test ").contains("Killed")) {
                currentStatus = ServerStatus.KILLED;
                checkReboot();
                return;
            };

            //minecraft crash
            try {
                String latestLog = Files.readString(Path.of("latest.log"));
                if(latestLog.contains("---- Minecraft Crash Report ----")) {
                    //TODO: Check the log line was from the server and not a chat message
                    currentStatus = ServerStatus.CRASHED;
                    checkReboot();
                    return;
                }

                //Intentional stop
                if(latestLog.contains("[Server thread/INFO]: Stopping the server")) {
                    //TODO: Check the log line was from the server and not a chat message
                    currentStatus = ServerStatus.OFFLINE;
                    return;
                }
            } catch (Exception e) {
                System.err.println("Couldn't read latest log");
                e.printStackTrace();
            }

            //still don't know why... try seeing if it was manually started!
            String list = CommandUtil.execute("pgrep java");
            List<String> ids = new ArrayList<>();
            while(list.contains("\n")) {
                ids.add(MiscUtils.getTextBetween(list, "", "\n"));
                list = MiscUtils.replaceTextBetween(list, "", "\n", "");
            }
            ids.add(list);

            for (String id : ids) {
                //if the java is running where the server should be
                if(CommandUtil.execute("pwdx "+id).contains("/var/home/dj/.gradle")) {
                    //TODO: Check if the jar  is minecraft
                    processId = Integer.parseInt(id);
                    refreshStatus();
                    return;
                }
            }

            processId = -1;
            currentStatus = ServerStatus.OFFLINE;
            WebhookSender.sendMessage("Your server has stopped unexpectedly, and we were not able to find out why!\nThe server will NOT be started automatically!");
        } else if(serverActuallyRunning && !currentStatus.shouldBeRunning()) {
            currentStatus = ServerStatus.RUNNING;
            Main.sse.sendToAll("clear_logs", "");
        }

        if(currentStatus.shouldBeRunning() && logManager == null) {
            ServerInfo.logManager = new LogManager("logs/latest.log", false, Main.logsSSE::sendToAll);
        } else if(!currentStatus.shouldBeRunning() && logManager != null) {
            logManager.close();
            Main.logsSSE.dataToSend.clear();
        }
    }

    private static void checkReboot() {
    }
}
