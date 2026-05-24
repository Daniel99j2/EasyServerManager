package com.daniel99j.servermanager.minecraft;

import com.daniel99j.servermanager.Config;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.function.Consumer;

public class LogManager implements Closeable {
    private boolean shouldBeRunning = true;
    private String currentLog = "";

    public LogManager(String fileName, boolean findHistory, boolean fastLoad, boolean waitUntilNew, Consumer<LogLineInfo> newLines) {
        new Thread(() -> {
            if(waitUntilNew) {
                Instant old = Instant.now().minusSeconds(1);
                while (true) {
                    try {
                        if(Files.getLastModifiedTime(Path.of(fileName)).toInstant().isAfter(old)) break;
                        Thread.sleep(100);
                    } catch (Exception _) {

                    }
                }
            }
            try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
                long currentPos = 0;
                while (shouldBeRunning) {
                    long fileLength = file.length();
                    if (fileLength > currentPos) {
                        file.seek(currentPos);
                        String line;
                        String newText = "";

                        while ((line = file.readLine()) != null) {
                            if(!shouldBeRunning) throw new CloseLogManager();
                            //ansi colour codes
                            while (line.contains("")) {
                                if(!shouldBeRunning) throw new CloseLogManager();
                                int i = line.indexOf("");
                                int end = line.indexOf("m", i);
                                line = line.replace(line.substring(i, end), "");
                            }
                            newText += line + "\n";
                            LogLineInfo info = fastLoad ? new LogLineInfo(line, LogLineInfo.Type.UNKNOWN) : LogLineInfo.get(line);
                            if(!shouldBeRunning) throw new CloseLogManager();
                            else newLines.accept(info);
                        }
                        currentLog += newText;
                        currentPos = file.getFilePointer();
                    }
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                if(!(e instanceof CloseLogManager)) {
                    e.printStackTrace();
                }
            }
        }).start();

        for (int i = 0; i < Config.INSTANCE.maxLogHistory; i++) {

        }
    }

    public String getString() {
        return currentLog;
    }

    @Override
    public void close() {
        shouldBeRunning = false;
    }
}
