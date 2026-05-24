package com.daniel99j.servermanager;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.servermanager.minecraft.ServerStatus;
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
    public int managerPort = 8082;
    public String webhookURL = "";
    private String loginKey = "";
    public String serverIp = "";
    public String startCommand = "";
    public boolean startAfterGameCrash = true;
    public boolean startAfterHostCrash = true;
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private String doNotChangeBelow = "Do not change values below";
    public String previousState = ServerStatus.OFFLINE.name();
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
        INSTANCE.save();
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
        int i = serverIp.lastIndexOf(":");
        return serverIp.substring(0, i);
    }

    public int getServerPort() {
        int i = serverIp.lastIndexOf(":");
        return Integer.parseInt(serverIp.substring(i+1));
    }

    public String getLoginKey() {
        if(!loginKey.contains("HASHED: ")) throw new IllegalStateException("Key was not hashed!");
        else return loginKey.replace("HASHED: ", "");
    }
}