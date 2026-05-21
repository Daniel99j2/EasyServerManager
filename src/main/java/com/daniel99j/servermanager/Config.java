package com.daniel99j.servermanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Config {
    public String webhookURL = "";
    public String webhookToken = "";
    public String loginKey = "";

    private static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static Config INSTANCE = new Config();

    public void refresh() {
        if (!loginKey.startsWith("HASHED: ")) {
            loginKey = UserLoader.hashPassword(loginKey);
            save();
        }
    }

    public void save() {
        try {
            var obj = GSON_PRETTY.toJson(this);
            Files.writeString(Path.of("e"), GSON_PRETTY.toJson(obj), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        INSTANCE = loadNoReplace();
    }

    public static Config loadNoReplace() {
        try {
            String json = Files.readString(Path.of("e"));
            Config config = GSON_PRETTY.fromJson(json, Config.class);
            config.refresh();
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}