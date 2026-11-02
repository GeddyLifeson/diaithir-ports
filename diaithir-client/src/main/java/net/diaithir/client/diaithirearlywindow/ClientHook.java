package net.diaithir.client.diaithirearlywindow;

import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class ClientHook {
    static final ModConfigSpec SPEC;
    static final ModConfigSpec.BooleanValue ENABLED;
    static final ModConfigSpec.ConfigValue<String> THEME;
    static final ModConfigSpec.ConfigValue<String> ACCENT;
    static final ModConfigSpec.ConfigValue<String> BACKGROUND;
    static final ModConfigSpec.ConfigValue<String> TEXT;
    static final ModConfigSpec.ConfigValue<String> BACKGROUND_IMAGE;
    static final ModConfigSpec.BooleanValue SHOW_LOG_TAIL;
    static final ModConfigSpec.IntValue LOG_TAIL_LINES;
    static final ModConfigSpec.BooleanValue ERROR_WINDOW;
    static final ModConfigSpec.IntValue MAX_FPS;
    static final ModConfigSpec.BooleanValue FALLBACK;
    static final ModConfigSpec.BooleanValue REDUCED_MOTION;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.comment("Enable the themed early loading window").define("enabled", true);
        THEME = b.define("theme", "default");
        ACCENT = b.define("accentColor", "#5B8CFF");
        BACKGROUND = b.define("backgroundColor", "#101418");
        TEXT = b.define("textColor", "#E8EEF5");
        BACKGROUND_IMAGE = b.comment("Path relative to the config directory, optional").define("backgroundImage", "");
        SHOW_LOG_TAIL = b.define("showLogTail", true);
        LOG_TAIL_LINES = b.defineInRange("logTailLines", 6, 1, 12);
        ERROR_WINDOW = b.define("errorWindowEnabled", true);
        MAX_FPS = b.defineInRange("maxFps", 60, 10, 240);
        FALLBACK = b.define("fallbackOnError", true);
        REDUCED_MOTION = b.define("reducedMotion", false);
        SPEC = b.build();
    }

    private static volatile boolean handedOff;

    private ClientHook() {}

    static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        NeoForge.EVENT_BUS.register(new ClientHook());
    }

    @SubscribeEvent
    public void onScreenOpening(ScreenEvent.Opening event) {
        if (handedOff || !(event.getNewScreen() instanceof TitleScreen)) {
            return;
        }
        handedOff = true;
        signalHandoff();
    }

    private static void signalHandoff() {
        try {
            Path flag = Path.of("config", "diaithir-early.handoff");
            Properties p = new Properties();
            p.setProperty("handoff", Long.toString(System.currentTimeMillis()));
            p.setProperty("enabled", Boolean.toString(ENABLED.get()));
            p.setProperty("theme", THEME.get());
            Files.createDirectories(flag.getParent());
            try (var out = Files.newOutputStream(flag)) {
                p.store(out, "Diaithir early window handoff");
            }
        } catch (Throwable ignored) {
        }
    }
}
