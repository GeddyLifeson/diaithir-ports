package net.diaithir.crashtools.crashassistant.client.event;

import java.nio.file.Path;
import java.util.Optional;
import net.diaithir.crashtools.crashassistant.client.screen.CrashSummaryScreen;
import net.diaithir.crashtools.crashassistant.config.CrashToolsConfig;
import net.diaithir.crashtools.crashassistant.crash.CrashReportLocator;
import net.diaithir.crashtools.crashassistant.util.FolderOpener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class TitleScreenHook {
    private boolean done;

    @SubscribeEvent
    public void onOpening(ScreenEvent.Opening event) {
        if (done || !(event.getNewScreen() instanceof TitleScreen)) return;
        done = true;
        if (!CrashToolsConfig.ENABLED.get() || !CrashToolsConfig.SHOW_ON_NEXT_LAUNCH.get()) return;
        Optional<Path> report = CrashReportLocator.findUnseen();
        if (report.isEmpty()) return;
        if (CrashToolsConfig.AUTO_OPEN_FOLDER.get()) FolderOpener.open(CrashReportLocator.crashDir());
        event.setNewScreen(new CrashSummaryScreen(event.getNewScreen(), report.get()));
    }
}
