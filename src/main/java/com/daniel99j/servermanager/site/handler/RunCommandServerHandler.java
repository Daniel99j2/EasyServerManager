package com.daniel99j.servermanager.site.handler;

import com.daniel99j.servermanager.CommandUtil;
import com.daniel99j.servermanager.UserLoader;
import com.daniel99j.servermanager.minecraft.ServerInfo;
import com.daniel99j.servermanager.site.SiteUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class RunCommandServerHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (exchange.getRequestMethod().equals("POST") && UserLoader.checkLoggedIn(exchange)) {
                CommandUtil.minecraftCommand(new String(exchange.getRequestBody().readAllBytes()));
                SiteUtil.respond(exchange, "Executed command", 200);
            } else throw new Exception("Invalid request");
        } catch (Exception e) {
            SiteUtil.respond(exchange, "Bad Request", 500);
        }
    }
}
