package com.daniel99j.servermanager.site.handler;

import com.daniel99j.servermanager.Config;
import com.daniel99j.servermanager.UserLoader;
import com.daniel99j.servermanager.site.SiteUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

public class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            //check if logged in
            if (exchange.getRequestMethod().equals("POST")) {
                if(!UserLoader.checkLoggedIn(exchange)) return;
                SiteUtil.respond(exchange, "Logged in", 200);
            //log in through login page
            } else if (exchange.getRequestMethod().equals("PUT")) {
                //decode bas64 as it used to remove ? or / etc
                String password = new String(Base64.getDecoder().decode(exchange.getRequestBody().readAllBytes()));
                boolean valid = UserLoader.login(password);
                if(valid) {
                    exchange.getResponseHeaders().add("Set-Cookie", "Token=" + Config.INSTANCE.loginKey);
                    SiteUtil.respond(exchange, "Login successful", 200);
                } else {
                    SiteUtil.respond(exchange, "Not Authorised", 401);
                }
            } else throw new Exception("Invalid request");
        } catch (Exception e) {
            SiteUtil.respond(exchange, "Bad Request", 500);
        }
    }
}
