package com.daniel99j.servermanager.minecraft;

import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LogManager {
    public LogManager(String fileName, boolean findHistory) {
        Files.newInputStream(Path.of(fileName), StandardOpenOption.READ);
    }
    public void getString() {

    }
}
