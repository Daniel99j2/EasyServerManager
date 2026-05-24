package com.daniel99j.servermanager.site;

import com.daniel99j.servermanager.Config;
import com.daniel99j.servermanager.Main;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class SiteGenerator {
    public static void load(HttpServer server) {
        try {
            URI uri = Objects.requireNonNull(SiteGenerator.class.getResource("/pages")).toURI();
            if (uri.getScheme().equals("jar")) {
                try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    loadPages(server, fs.getPath("/pages"));
                }
            } else {
                loadPages(server, Paths.get(uri));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadPages(HttpServer server, Path pagesPath) throws IOException {
        try (Stream<Path> paths = Files.list(pagesPath)) {
            paths.forEach(p -> {
                try {
                    String fileName = p.getFileName().toString();
                    if (!fileName.endsWith(".png")) {
                        GeneratedHandler handler = new GeneratedHandler(Files.readString(p));
                        server.createContext("/" + fileName.replace(".html", ""), handler);

                        if (Main.showGeneratedPages) {
                            Path output = Paths.get("generated", fileName);
                            Files.createDirectories(output.getParent());
                            Files.write(
                                    output,
                                    handler.page,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING
                            );
                        }
                    } else {
                        GeneratedHandler handler = new GeneratedHandler(Files.readAllBytes(p), false);
                        server.createContext("/" + fileName, handler);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static class GeneratedHandler implements HttpHandler {
        public final byte[] page;

        GeneratedHandler(String page) {
            this(page.getBytes(), true);
        }

        GeneratedHandler(byte[] page, boolean fixup) {
            if(fixup) {
                String stringPage = new String(page);
                while (true) {
                    stringPage = stringPage.replace("%base%/", "http://"+Main.baseSite+":"+ Config.INSTANCE.managerPort+"/").replace("%base%", "http://localhost:"+Config.INSTANCE.managerPort+"/");
                    String old = stringPage;
                    for (ElementParser elementParser : ElementParser.PARSERS) {
                        stringPage = elementParser.parseFile(stringPage);
                    }
                    if (stringPage.equals(old)) break;
                }
                this.page = stringPage.getBytes();
            } else this.page = page;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Send HTTP 200 OK and specify response length

            exchange.sendResponseHeaders(200, page.length);

            // Write the response body
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(page);
            }
        }
    }

    public static String login() {
        return """
                <html>
                <body>
                <script src="http://baseSite:port/password.js"></script>
                <script type="text/javascript">
                    window.addEventListener("load", () => {
                       login();
                    });
                </script>
                </body>
                </html>
                """.replace("baseSite", Main.baseSite).replace("port", String.valueOf(Config.INSTANCE.getServerPort()));
    }

    public static String redirect(String url) {
        return """
                <html>
                <body>
                <script type="text/javascript">
                    window.addEventListener("load", () => {
                        window.location.replace("url");
                    });
                </script>
                </body>
                </html>
                """.replace("url", url);
    }
}
