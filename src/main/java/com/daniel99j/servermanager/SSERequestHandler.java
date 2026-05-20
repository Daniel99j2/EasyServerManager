package com.daniel99j.servermanager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

public class SSERequestHandler implements HttpHandler {
    private final List<OutputStream> sseOutputStreamList = new ArrayList<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println(exchange.getRequestURI());
        exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().add("charset", "utf-8");
        exchange.getResponseHeaders().add("Connection", "keep-alive");
        exchange.getResponseHeaders().add("access-control-allow-origin", "*");
        exchange.sendResponseHeaders(200, 0);
        sseOutputStreamList.add(exchange.getResponseBody());

        String response = """
                data: hello \n\n""";
        this.sendEventStream(response, exchange.getResponseBody());
    }

    public synchronized void sendEventStream(String message, OutputStream sseOutputStream) throws IOException {
        sseOutputStream.write(message.getBytes());
        sseOutputStream.flush();
    }

    public void sendToAll(String id, String data) {
        List<OutputStream> toRemove = new ArrayList<>();
        this.sseOutputStreamList.forEach(os-> {
            try {
                this.sendEventStream("event: "+id+"\ndata: "+data.replace("\\", "\\\\")+"\n\n", os);
            } catch (IOException e) {
                if(!e.getMessage().equals("Broken pipe")) throw new RuntimeException(e);
                else toRemove.add(os);
            }
        });
        this.sseOutputStreamList.removeAll(toRemove);
    }
}
