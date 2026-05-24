package com.daniel99j.servermanager.minecraft;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.servermanager.CommandUtil;
import com.daniel99j.servermanager.Config;
import com.daniel99j.servermanager.Main;
import com.daniel99j.servermanager.WebhookSender;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerInfo {
    public static ServerStatus currentStatus = ServerStatus.OFFLINE;
    public static LogManager logManager;
    public static List<String> onlinePlayers = new ArrayList<>();

    public static void start() {
        if(currentStatus.shouldBeRunning()) return;
        CommandUtil.tmuxCommand(Config.INSTANCE.startCommand);
        setStatus(ServerStatus.STARTING);
        Main.sse.loopConnected(ServerInfo::resend);
    }

    public static void load() {
        Main.sse.sendToAll("clear_logs", "");

        if(CommandUtil.pingMinecraft(Config.INSTANCE.getServerIp(), Config.INSTANCE.getServerPort())) {
            System.out.println("Server ping success; server was manually started");
            setStatus(ServerStatus.MANUALLY_STARTED);
            return;
        }

        boolean running = false;
        String list = CommandUtil.execute("pgrep java");
        List<String> ids = new ArrayList<>();
        while(list.contains("\n")) {
            ids.add(MiscUtils.getTextBetween(list, "", "\n"));
            list = MiscUtils.replaceTextBetween(list, "", "\n", "");
        }
        ids.add(list);

        for (String sId : ids) {
            //if the java is running where the server should be
            if(CommandUtil.execute("jcmd "+sId+" GC.class_histogram").contains("net.minecraft.world")) {
                running = true;
                break;
            }
        }

        if(running) {
            System.out.println("Process running minecraft exists; server was manually started");
            setStatus(ServerStatus.MANUALLY_STARTED);
            return;
        }

        //the host must have been rebooted (or server manager stopped incorrectly)
        if(ServerStatus.valueOf(Config.INSTANCE.previousState) == ServerStatus.ONLINE) {
            setStatus(ServerStatus.HOST_REBOOTED);
        }
    }

    public static void setStatus(ServerStatus status) {
        if(currentStatus != status) System.out.println("Status changed to "+status);
        if(currentStatus.shouldBeRunning() != status.shouldBeRunning()) {
            Main.sse.sendToAll("clear_logs", "");
        }
        currentStatus = status;

        Config.INSTANCE.previousState = status.name();
        Config.INSTANCE.save();

        if(currentStatus.shouldBeRunning() && logManager == null) {
            ServerInfo.logManager = new LogManager("logs/latest.log", false, false, true, (log) -> {
                if(log.type.isRight() && log.type.getRight().equals(LogLineInfo.Type.JOIN)) {
                    onlinePlayers.add(((String) log.type.getRight().value()));
                } else if(log.type.isRight() && log.type.getRight().equals(LogLineInfo.Type.LEAVE)) {
                    onlinePlayers.remove(((String) log.type.getRight().value()));
                } else if(log.type.isLeft() && log.type.getLeft().equals(LogLineInfo.Type.STOP)) {
                    if(currentStatus != ServerStatus.CRASHED) setStatus(ServerStatus.STOPPING);
                } else if(log.type.isLeft() && log.type.getLeft().equals(LogLineInfo.Type.CRASH)) {
                    setStatus(ServerStatus.CRASHED);
                } else if(log.type.isLeft() && log.type.getLeft().equals(LogLineInfo.Type.COMPLETE_SAVE)) {
                    if(currentStatus == ServerStatus.STOPPING) {
                        setStatus(ServerStatus.OFFLINE);
                    }
                } else if(log.type.isLeft() && log.type.getLeft().equals(LogLineInfo.Type.COMPLETE_LOAD)) {
                    if(currentStatus == ServerStatus.STARTING) {
                        setStatus(ServerStatus.ONLINE);
                    }
                }
                Main.logsSSE.sendToAll(log);
            });
        } else if(!currentStatus.shouldBeRunning() && logManager != null) {
            logManager.close();
            logManager = null;
            Main.logsSSE.dataToSend.clear();
        }
        Main.sse.loopConnected(ServerInfo::resend);
    }

    private static void checkReboot() {
    }

    public static void resend(OutputStream responseBody) {
        try {
            JsonObject status = new JsonObject();
            status.addProperty("name", currentStatus.toString());
            status.addProperty("running", currentStatus.shouldBeRunning());
            String colour = "red";
            if(currentStatus == ServerStatus.STARTING) colour = "yellow";
            else if(currentStatus == ServerStatus.ONLINE) colour = "lime";
            else if(currentStatus == ServerStatus.STOPPING) colour = "crimson";
            else if(currentStatus == ServerStatus.OFFLINE) colour = "grey";
            else if(currentStatus == ServerStatus.MANUALLY_STARTED) colour = "dodgerblue";
            status.addProperty("colour", colour);
            Main.sse.sendEventStream("event: status\ndata: " + new GsonBuilder().create().toJson(status) + "\n\n", responseBody, true);
            
            StringBuilder s = new StringBuilder();
            for (String onlinePlayer : onlinePlayers) {
                s.append(onlinePlayer).append(",");
            }
            if(s.toString().endsWith(",")) s.deleteCharAt(s.length()-1);
            Main.sse.sendEventStream("event: players\ndata: " +s + "\n\n", responseBody, true);
        } catch (Exception e) {

        }
    }

    public static void stop() {
        //the minecraft command function checks its running
        CommandUtil.minecraftCommand("stop");
        setStatus(ServerStatus.STOPPING);
    }

//    public static void refreshStatus() {
//        boolean pingable = CommandUtil.pingMinecraft(Config.INSTANCE.getServerIp(), Config.INSTANCE.getServerPort());
//        boolean running = false;
//
//
//        //still don't know why... try seeing if it was manually started!
//        String list = CommandUtil.execute("pgrep java");
//        List<String> ids = new ArrayList<>();
//        while(list.contains("\n")) {
//            ids.add(MiscUtils.getTextBetween(list, "", "\n"));
//            list = MiscUtils.replaceTextBetween(list, "", "\n", "");
//        }
//        ids.add(list);
//
//        for (String sId : ids) {
//            //if the java is running where the server should be
//            if(CommandUtil.execute("jcmd "+sId+" GC.class_histogram").contains("net.minecraft.world")) {
//                int id = Integer.parseInt(sId);
//                int oldId =
//                        processId = id;
//                refreshStatus();
//                return;
//            }
//        }
//
//        //pwdx
//
//        processId = -1;
//
//        boolean serverActuallyRunning = false;
//
//        //first if thinking running then check process id
//        if(currentStatus.shouldBeRunning() && !serverActuallyRunning) {
//            if(CommandUtil.execute("pwdx "+processId).contains("/var/home/dj/.gradle")) return;
//            //the process is not valid! find cause.
//
//            //host server rebooted
//            if(CommandUtil.execute("tmux capture-pane -pt test ").contains("no sessions")) {
//                setStatus(ServerStatus.HOST_REBOOTED);
//                checkReboot();
//                return;
//            };
//
//            //used too much memory
//            if(CommandUtil.execute("tmux capture-pane -pt test ").contains("Killed")) {
//                setStatus(ServerStatus.KILLED);
//                checkReboot();
//                return;
//            };
//
//            //minecraft crash
//            try {
//                String latestLog = Files.readString(Path.of("latest.log"));
//                if(latestLog.contains("---- Minecraft Crash Report ----")) {
//                    //TODO: Check the log line was from the server and not a chat message
//                    setStatus(ServerStatus.CRASHED);
//                    checkReboot();
//                    return;
//                }
//
//                //Intentional stop
//                if(latestLog.contains("[Server thread/INFO]: Stopping the server")) {
//                    //TODO: Check the log line was from the server and not a chat message
//                    setStatus(ServerStatus.OFFLINE);
//                    return;
//                }
//            } catch (Exception e) {
//                System.err.println("Couldn't read latest log");
//                e.printStackTrace();
//            }
//            setStatus(ServerStatus.OFFLINE);
//            Main.sse.loopConnected(ServerInfo::resend);
//            WebhookSender.sendMessage("Your server has stopped unexpectedly, and we were not able to find out why!\nThe server will NOT be started automatically!");
//        } else if(serverActuallyRunning && !currentStatus.shouldBeRunning()) {
//            setStatus(ServerStatus.ONLINE);
//        }
//
//        Main.sse.loopConnected(ServerInfo::resend);
//    }
}
