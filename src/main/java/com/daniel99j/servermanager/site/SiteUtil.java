package com.daniel99j.servermanager.site;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class SiteUtil {
    public static void respond(HttpExchange exchange, String response, int code) {
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(code, response.getBytes().length);
            os.write(response.getBytes());
        } catch (IOException ignored) {
        }
    }
}
