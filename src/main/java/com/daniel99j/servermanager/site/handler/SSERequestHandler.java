package com.daniel99j.servermanager.site.handler;

import com.daniel99j.servermanager.Main;
import com.daniel99j.servermanager.UserLoader;
import com.daniel99j.servermanager.minecraft.ServerInfo;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SSERequestHandler implements HttpHandler {
    protected final List<OutputStream> outputs = new ArrayList<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!UserLoader.checkLoggedIn(exchange)) return;

        exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().add("charset", "utf-8");
        exchange.getResponseHeaders().add("Connection", "keep-alive");
        exchange.getResponseHeaders().add("access-control-allow-origin", "*");
        exchange.sendResponseHeaders(200, 0);
        outputs.add(exchange.getResponseBody());

        String response = """
                event: init
                data: hello world \n\n""";
        this.sendEventStream(response, exchange.getResponseBody(), true);

        ServerInfo.resend(exchange.getResponseBody());
    }

    public synchronized void sendEventStream(String message, OutputStream sseOutputStream, boolean flush) throws IOException {
        sseOutputStream.write(message.getBytes());
        if(flush) sseOutputStream.flush();
    }

    public void sendToAll(String id, String data) {
        List<OutputStream> toRemove = new ArrayList<>();
        this.outputs.forEach(os-> {
            try {
                this.sendEventStream("event: "+id+"\ndata: "+data.replace("\n", "\\n")+"\n\n", os, true);
            } catch (IOException e) {
                toRemove.add(os);
                if(!e.getMessage().equals("Broken pipe")) System.out.println("Disconnected with reason:"+e.getMessage());
            }
        });
        this.outputs.removeAll(toRemove);
    }

    public void loopConnected(Consumer<OutputStream> runnable) {
        List<OutputStream> toRemove = new ArrayList<>();
        this.outputs.forEach(os-> {
            try {
                this.sendEventStream("event: heartbeat\ndata: ping\n\n", os, true);
                runnable.accept(os);
            } catch (IOException e) {
                toRemove.add(os);
                if(!e.getMessage().equals("Broken pipe")) System.out.println("Disconnected with reason:"+e.getMessage());
            }
        });
        this.outputs.removeAll(toRemove);
    }
}
