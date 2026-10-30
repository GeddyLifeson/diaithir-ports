package net.diaithir.earlywindow;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.loading.ProgramArgs;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import org.lwjgl.opengl.GL;

/**
 * NeoForge's early loading window, with the fatal loading error screen swapped for Diaithir's.
 * Selected by {@code earlyWindowProvider = "diaithir"} in config/fml.toml.
 */
public class DiaithirWindow extends DisplayWindow {
    private final ScheduledExecutorService labels = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().name("diaithir-labels").daemon().factory());

    public DiaithirWindow() {
        // FML closes the early service jars before it reports a fatal error, so the error window's classes are loaded now.
        ErrorWindow.preload();
    }

    @Override
    public String name() {
        return "diaithir";
    }

    // FML's step names ("Loading mods", "Launching minecraft") read as one Diaithir line over both bars.
    @Override
    public void initialize(ProgramArgs args) {
        super.initialize(args);
        labels.scheduleAtFixedRate(() -> {
            var bars = StartupNotificationManager.getCurrentProgress();
            for (int i = 0; i < bars.size(); i++) {
                String want = i == 0 ? "Loading Diaithir" : "";
                if (!bars.get(i).label().getText().equals(want)) bars.get(i).label(want);
            }
        }, 0, 20, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        labels.shutdownNow();
        super.close();
    }

    @Override
    public void displayFatalErrorAndExit(List<ModLoadingIssue> issues, Path modsFolder, Path logFile, Path crashReportFile) {
        long window = takeOverGlfwWindow();
        GL.createCapabilities();
        close();
        ErrorWindow.show(window, issues, logFile, crashReportFile);
    }
}
