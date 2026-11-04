// Ported from GUI Scaler (LGPL-3.0-only) https://github.com/zendiik/gui-scaler
// Modifications: ClientTickEvent.Post detects window size changes and sets the guiScale option instead of a resizeGui mixin.
package net.diaithir.clientviewinput.guiscaler.client;

import com.mojang.blaze3d.platform.Window;
import java.util.HashMap;
import java.util.Map;
import net.diaithir.clientviewinput.guiscaler.config.GuiScalerConfig;
import net.diaithir.clientviewinput.guiscaler.core.GUIScaleCalculator;
import net.diaithir.clientviewinput.guiscaler.core.ScaleMode;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GuiScalerClient {
    private static final Logger LOG = LoggerFactory.getLogger("GUI Scaler");

    private static int lastWidth = -1;
    private static int lastHeight = -1;
    private static volatile boolean forceRecalc = false;

    private GuiScalerClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(ModConfigEvent.Reloading.class, e -> forceRecalc = true);
        NeoForge.EVENT_BUS.register(new Handlers());
    }

    private static Map<Integer, Integer> customRules() {
        Map<Integer, Integer> rules = new HashMap<>();
        for (String rule : GuiScalerConfig.CUSTOM_RULES.get()) {
            try {
                String[] parts = rule.split(":");
                if (parts.length == 2) {
                    rules.put(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
                }
            } catch (NumberFormatException ex) {
                LOG.error("Invalid custom rule format: {}", rule);
            }
        }
        return rules;
    }

    private static void apply(Minecraft mc) {
        if (!GuiScalerConfig.ENABLE_AUTO_SCALE.get()) {
            return;
        }
        Window window = mc.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();
        if (width <= 0 || height <= 0) {
            return; // minimized
        }
        // Only recompute when the window size actually changed (or config reloaded) so manual
        // changes of the GUI scale option are not overwritten.
        if (!forceRecalc && width == lastWidth && height == lastHeight) {
            return;
        }
        forceRecalc = false;
        lastWidth = width;
        lastHeight = height;

        ScaleMode mode = GuiScalerConfig.MODE.get();
        int desired = GUIScaleCalculator.calculateOptimalScale(width, height, mode, customRules());
        if (desired > 0) {
            int max = window.calculateScale(0, mc.options.forceUnicodeFont().get());
            desired = Math.min(desired, max);
        }
        if (desired != mc.options.guiScale().get()) {
            mc.options.guiScale().set(desired);
            LOG.info("GUI scale set to {} ({}x{})", desired, width, height);
        }
    }

    public static final class Handlers {
        @SubscribeEvent
        public void onTick(ClientTickEvent.Post e) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getWindow() == null) {
                return;
            }
            apply(mc);
        }
    }
}
