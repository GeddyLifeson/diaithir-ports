package net.diaithir.crashtools.crashassistant.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;

public final class ClipboardUtil {
    private ClipboardUtil() {}

    public static boolean copy(String text) {
        try {
            Minecraft.getInstance().keyboardHandler.setClipboard(text);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Path saveFallback(String text, Path gameDir) {
        try {
            Path dir = gameDir.resolve("crashtools");
            Files.createDirectories(dir);
            Path out = dir.resolve("report-" + System.currentTimeMillis() + ".md");
            Files.writeString(out, text);
            return out;
        } catch (IOException e) {
            return null;
        }
    }
}
