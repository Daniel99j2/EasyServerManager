package com.daniel99j.servermanager.site.handler;

import com.daniel99j.servermanager.Config;
import com.daniel99j.servermanager.UserLoader;
import com.daniel99j.servermanager.minecraft.ServerInfo;
import com.daniel99j.servermanager.site.SiteGenerator;
import com.daniel99j.servermanager.site.SiteUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class StopServerHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (exchange.getRequestMethod().equals("POST")) {
                if(!UserLoader.checkLoggedIn(exchange)) return;
                ServerInfo.stop();
                SiteUtil.respond(exchange, "Stopping server", 200);
            } else throw new Exception("Invalid request");
        } catch (Exception e) {
            SiteUtil.respond(exchange, "Bad Request", 500);
        }
    }
}
