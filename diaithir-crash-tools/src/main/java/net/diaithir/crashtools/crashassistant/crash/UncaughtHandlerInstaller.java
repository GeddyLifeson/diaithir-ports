package net.diaithir.crashtools.crashassistant.crash;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UncaughtHandlerInstaller {
    private static boolean installed;

    private UncaughtHandlerInstaller() {}

    public static synchronized void install() {
        if (installed) return;
        installed = true;
        Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Path dir = CrashReportLocator.stateDir();
                Files.createDirectories(dir);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Files.writeString(dir.resolve("live_crash.txt"),
                        "Thread: " + t.getName() + "\nTime: " + System.currentTimeMillis() + "\n" + sw);
            } catch (Throwable ignored) {
            }
            if (prev != null) prev.uncaughtException(t, e);
        });
    }
}
