package com.daniel99j.servermanager;

import com.daniel99j.djutil.MiscUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Config {
    public String webhookURL = "";
    private String loginKey = "";
    public String serverIp = "https://example.com:1010";
    private static boolean tempSkipPasswordCheck = false;

    private static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static Config INSTANCE = new Config();
    public int maxLogHistory = 20;

    public void refresh() {
        if(loginKey.isBlank() && !tempSkipPasswordCheck) throw new RuntimeException("Please set a login password!");
        if (!loginKey.startsWith("HASHED: ") && !tempSkipPasswordCheck) {
            loginKey = "HASHED: "+UserLoader.hashPassword(loginKey);
            save();
        }
        if(serverIp.isBlank() || serverIp.contains("example.com")) throw new RuntimeException("Please set the server IP!");
    }

    public void save() {
        try {
            Files.writeString(Path.of("easyservermanager.json"), GSON_PRETTY.toJson(this), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        INSTANCE = loadNoReplace();
    }

    public static Config loadNoReplace() {
        Path path = Path.of("easyservermanager.json");
        if(!Files.exists(path)) {
            Config config = new Config();
            tempSkipPasswordCheck = true;
            config.refresh();
            tempSkipPasswordCheck = false;
            config.save();
            config.refresh();
            return config;
        } else try {
            String json = Files.readString(path);
            Config config = GSON_PRETTY.fromJson(json, Config.class);
            config.refresh();
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getServerIp() {
        return MiscUtils.replaceTextBetween(serverIp, ":", "", "");
    }

    public int getServerPort() {
        return serverIp.contains(":") ? Integer.parseInt(MiscUtils.replaceTextBetween(serverIp, "", ":", "")) : 25565;
    }

    public String getLoginKey() {
        if(!loginKey.contains("HASHED: ")) throw new IllegalStateException("Key was not hashed!");
        else return loginKey.replace("HASHED: ", "");
    }
}