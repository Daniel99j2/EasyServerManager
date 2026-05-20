package com.daniel99j.servermanager;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.servermanager.site.handler.*;
import com.daniel99j.servermanager.site.handler.LoginHandler;
import com.daniel99j.servermanager.site.*;
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {
    public static int PORT = 8082;
    private static SSERequestHandler sse = new SSERequestHandler();

    static void main(String[] args) throws IOException {
        String customPort = MiscUtils.getArgValue("--port", args);
        if(customPort != null) PORT = Integer.parseInt(customPort);
        Scanner input = new Scanner(System.in);  // the scanner is the console input

        UserLoader.load();

        new ItemsElement();
        new ItemElement();
        new CartDataElement();
        new UserIconElement();
        new SiteTitleElement();
        new UsersElement();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        SiteGenerator.load(server);

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/account", new AccountApiHandler());
        server.createContext("/sse-stream", sse);
        server.createContext("/", new RedirectHandler("http://localhost:"+PORT+"/store"));
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port "+PORT);

        long time = System.currentTimeMillis();
        int second = 0;
        new Thread(() -> {
            while (true) {
                if(input.hasNext()) {
                    String command = input.nextLine();
                    sse.sendToAll("runCommand", command);
                }
            }
        }).start();
    }
}