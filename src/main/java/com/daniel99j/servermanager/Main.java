package com.daniel99j.servermanager;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.servermanager.minecraft.LogManager;
import com.daniel99j.servermanager.minecraft.ServerInfo;
import com.daniel99j.servermanager.site.handler.*;
import com.daniel99j.servermanager.site.handler.LoginHandler;
import com.daniel99j.servermanager.site.*;
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {
    public static LogsStreamHandler logsSSE = new LogsStreamHandler();
    public static SSERequestHandler sse = new SSERequestHandler();
    public static boolean showGeneratedPages = false;
    public static String baseSite = "localhost";

    static void main(String[] args) throws IOException {
        Config.load();
        if(!MiscUtils.containsArg("-local", args)) {
            //https://stackoverflow.com/a/38342964
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                baseSite = socket.getLocalAddress().getHostAddress();
            }
        }

        showGeneratedPages = MiscUtils.containsArg("-genpages", args);

        ElementParser.PARSERS.add(new ItemsElement());
        ElementParser.PARSERS.add(new ItemElement());
        ElementParser.PARSERS.add(new CartDataElement());
        ElementParser.PARSERS.add(new UserIconElement());
        ElementParser.PARSERS.add(new SiteTitleElement());
        ElementParser.PARSERS.add(new SidebarElement());
        ElementParser.PARSERS.add(new UsersElement());
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", Config.INSTANCE.managerPort), 0);
        SiteGenerator.load(server);

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/start", new StartServerHandler());
        server.createContext("/api/stop", new StopServerHandler());
        //server.createContext("/api/account", new AccountApiHandler());
        server.createContext("/event-stream", sse);
        server.createContext("/logs-stream", logsSSE);
        server.createContext("/", new RedirectHandler("http://"+baseSite+":"+Config.INSTANCE.managerPort+"/store"));
        server.setExecutor(null);
        server.start();
        System.out.println("Server started at "+baseSite+" on port "+Config.INSTANCE.managerPort);

        ServerInfo.load();
    }
}