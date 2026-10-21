package net.diaithir.earlywindow;

import java.nio.file.Path;
import java.util.List;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import org.lwjgl.opengl.GL;

/**
 * NeoForge's early loading window, with the fatal loading error screen swapped for Diaithir's.
 * Selected by {@code earlyWindowProvider = "diaithir"} in config/fml.toml.
 */
public class DiaithirWindow extends DisplayWindow {
    public DiaithirWindow() {
        // FML closes the early service jars before it reports a fatal error, so the error window's classes are loaded now.
        ErrorWindow.preload();
    }

    @Override
    public String name() {
        return "diaithir";
    }

    @Override
    public void displayFatalErrorAndExit(List<ModLoadingIssue> issues, Path modsFolder, Path logFile, Path crashReportFile) {
        long window = takeOverGlfwWindow();
        GL.createCapabilities();
        close();
        ErrorWindow.show(window, issues, logFile, crashReportFile);
    }
}
