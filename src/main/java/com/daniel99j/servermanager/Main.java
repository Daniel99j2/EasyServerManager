package com.daniel99j.servermanager;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.servermanager.minecraft.LogManager;
import com.daniel99j.servermanager.minecraft.ServerInfo;
import com.daniel99j.servermanager.site.handler.*;
import com.daniel99j.servermanager.site.handler.LoginHandler;
import com.daniel99j.servermanager.site.*;
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {
    public static int PORT = 8082;
    public static LogsStreamHandler logsSSE = new LogsStreamHandler();
    public static SSERequestHandler sse = new SSERequestHandler();

    static void main(String[] args) throws IOException {
        Config.load();

        String customPort = MiscUtils.getArgValue("--port", args);
        if(customPort != null) PORT = Integer.parseInt(customPort);
        Scanner input = new Scanner(System.in);  // the scanner is the console input

        ServerInfo.refreshStatus();

        ElementParser.PARSERS.add(new ItemsElement());
        ElementParser.PARSERS.add(new ItemElement());
        ElementParser.PARSERS.add(new CartDataElement());
        ElementParser.PARSERS.add(new UserIconElement());
        ElementParser.PARSERS.add(new SiteTitleElement());
        ElementParser.PARSERS.add(new SidebarElement());
        ElementParser.PARSERS.add(new UsersElement());
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        SiteGenerator.load(server);

        server.createContext("/api/login", new LoginHandler());
        //server.createContext("/api/account", new AccountApiHandler());
        server.createContext("/event-stream", sse);
        server.createContext("/logs-stream", logsSSE);
        server.createContext("/", new RedirectHandler("http://localhost:"+PORT+"/store"));
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port "+PORT);
    }
}