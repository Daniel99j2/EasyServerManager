package com.daniel99j.servermanager.minecraft;

import com.daniel99j.servermanager.Config;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.function.Consumer;

public class LogManager implements Closeable {
    private boolean shouldBeRunning = true;
    private String currentLog = "";

    public LogManager(String fileName, boolean findHistory, Consumer<String> newLines) {
        new Thread(() -> {
            try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
                long currentPos = 0;
                while (shouldBeRunning) {
                    long fileLength = file.length();
                    if(fileLength == 0) {
                        currentPos = -1;
                    }
                    if (fileLength > currentPos) {
                        file.seek(currentPos);
                        String line;
                        String newText = "";
                        while ((line = file.readLine()) != null) {
                            newText += line + "\n";
                        }
                        newLines.accept(newText);
                        currentLog += newText;
                        currentPos = file.getFilePointer();
                    }
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
