package net.diaithir.crashtools.crashassistant.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import java.util.Optional;
import net.diaithir.crashtools.crashassistant.client.screen.CrashSummaryScreen;
import net.diaithir.crashtools.crashassistant.crash.CrashReportLocator;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class KeybindHandler {
    private static KeyMapping key;

    public static void register(RegisterKeyMappingsEvent event) {
        key = new KeyMapping("key.crashtools.open_last_crash",
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(),
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("crashtools", "main")));
        event.register(key);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Post event) {
        if (key == null) return;
        while (key.consumeClick()) {
            Optional<Path> latest = CrashReportLocator.findLatest();
            Minecraft mc = Minecraft.getInstance();
            latest.ifPresent(p -> mc.setScreen(new CrashSummaryScreen(mc.screen, p)));
        }
    }
}
