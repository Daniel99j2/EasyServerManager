package com.daniel99j.servermanager;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.servermanager.minecraft.ServerInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CommandUtil {
    public static String execute(String command) {
        try {
            // For Windows, use "cmd", "/c", "your_command"
            // For Linux/macOS, use "/bin/bash", "-c", "your_command"
            boolean microslop = System.getProperty("os.name").toLowerCase().contains("windows");
            ProcessBuilder builder = new ProcessBuilder(microslop ? "cmd": "/bin/bash", microslop ? "/c" : "-c", command);

            Process process = builder.start();
            process.waitFor(); // Wait for the command to finish

            String output = process.getOutputStream().toString();
            System.out.println("Executed command {"+command+"}\nOUTPUT:\n"+output+"--- END ---");
            return output;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void minecraftCommandNoReturn(String command) {
        if(!ServerInfo.currentStatus.shouldBeRunning()) throw new IllegalArgumentException("Server not running!");
        tmuxCommand(command);
    }

    public static String minecraftCommand(String command) {
        String minecraftPane = captureTmux();
        minecraftCommandNoReturn(command);
        String newPane = captureTmux();

        //works with duplicate lines as time will change
        String lastLine = new StringBuilder(minecraftPane).reverse().toString();
        lastLine = MiscUtils.getTextBetween(lastLine, "", "\n");

        newPane = MiscUtils.replaceTextBetween(newPane, "", lastLine, "");

        return newPane;
    }

    public static String captureTmux() {
        return execute("tmux capture-pane -t test");
    }

    public static String tmuxCommand(String command) {
        return execute("tmux send-keys -t test \""+command.replace("\"", "\\\"")+"\" Enter");
    }

    public static boolean pingMinecraft(String host, int port) {
        try (Socket socket = new Socket()) {
            //small timeout as the server is on the same computer so it SHOULD be only ~1ms
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void pingMinecraftInBackground(String host, int port, Consumer<Boolean> handler) {

    }
}
