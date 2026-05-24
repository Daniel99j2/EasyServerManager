package com.daniel99j.servermanager.site.handler;

import com.daniel99j.servermanager.UserLoader;
import com.daniel99j.servermanager.minecraft.LogLineInfo;
import com.daniel99j.servermanager.minecraft.ServerInfo;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

public class LogsStreamHandler extends SSERequestHandler {
    public final List<String> dataToSend = new ArrayList<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!UserLoader.checkLoggedIn(exchange)) return;

        System.out.println(exchange.getRequestURI());
        exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().add("charset", "utf-8");
        exchange.getResponseHeaders().add("Connection", "keep-alive");
        exchange.sendResponseHeaders(200, 0);
        outputs.add(exchange.getResponseBody());

        if(!ServerInfo.currentStatus.shouldBeRunning()) {
            this.send("Server not running", exchange.getResponseBody(), true);
        } else {
            this.send("==== LOGS START ====\n", exchange.getResponseBody(), false);
            for (String s : dataToSend) {
                send(s, exchange.getResponseBody(), false);
            }

            exchange.getResponseBody().flush();
        }
    }

    private void send(String data, OutputStream os, boolean flush) throws IOException {
        sendEventStream("event: message\ndata: "+data.replace("\n", "\\n")+"\n\n", os, flush);
    }

    public void sendToAll(String data) {
        sendToAll("message", data);
        dataToSend.add(data);
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    public void sendToAll(LogLineInfo data) {
        String line = data.log;
        if (data.type.equals(LogLineInfo.Type.INFO)) line = "$0" + line;
        else if (data.type.equals(LogLineInfo.Type.COMPLETE_SAVE)) line = "$0" + line;
        else if (data.type.equals(LogLineInfo.Type.COMPLETE_LOAD)) line = "$0" + line;
        else if (data.type.equals(LogLineInfo.Type.WARN)) line = "$1" + line;
        else if (data.type.equals(LogLineInfo.Type.ERROR)) line = "$2" + line;
        else if (data.type.equals(LogLineInfo.Type.FATAL)) line = "$3" + line;
        else if (data.type.equals(LogLineInfo.Type.CRASH)) line = "$3" + line;
        else if (data.type.equals(LogLineInfo.Type.COMMAND)) line = "$5" + line;
        else if (data.type.equals(LogLineInfo.Type.STOP)) line = "$2" + line;
        else if (data.type.equals(LogLineInfo.Type.JOIN) || data.type.equals(LogLineInfo.Type.LEAVE)) line = "$4" + line;
        sendToAll("message", line);
        dataToSend.add(line);
    }

}
