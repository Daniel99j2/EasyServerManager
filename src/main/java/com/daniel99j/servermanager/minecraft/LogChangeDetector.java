package com.daniel99j.servermanager.minecraft;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class LogChangeDetector {
    public static void detect(String name, Runnable onChange, Supplier<Boolean> valid, Runnable onFinish, int rate) {
        new Thread(() -> {
            try {
                Path path = Path.of(name).toAbsolutePath();
                long size = Files.size(path);

                while (valid.get()) {
                    if(size != Files.size(path)) {
                        onChange.run();
                    }
                    Thread.sleep(rate);
                }
                onFinish.run();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }).start();
    }
}
