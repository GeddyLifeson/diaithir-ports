package net.diaithir.crashtools.crashassistant.util;

import java.io.IOException;
import java.nio.file.Path;

public final class FolderOpener {
    private FolderOpener() {}

    public static boolean open(Path path) {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            String p = path.toAbsolutePath().toString();
            String[] cmd = os.contains("win") ? new String[] {"explorer", p}
                    : os.contains("mac") ? new String[] {"open", p} : new String[] {"xdg-open", p};
            new ProcessBuilder(cmd).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
