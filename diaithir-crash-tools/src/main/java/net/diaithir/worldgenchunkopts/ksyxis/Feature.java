// Ported from Ksyxis (MIT) https://github.com/VidTu/Ksyxis
// Modifications: NeoForge 26.1.2 only; legacy/other-loader code, ModernFix hack and the mixin plugin dropped; Mojang-mapped targets; no config.
package net.diaithir.worldgenchunkopts.ksyxis;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;

/**
 * NOTE: This feature requires mixins. Config resource: {@code net.diaithir.worldgenchunkopts.ksyxis.mixins.json}
 * (src/main/resources). The integrator must register it in neoforge.mods.toml with
 * {@code [[mixins]] config = "net.diaithir.worldgenchunkopts.ksyxis.mixins.json"}.
 * No NeoForge event can alter the spawn preparation constants, so mixins are kept. The feature has no config.
 */
public final class Feature {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        LOGGER.info("Ksyxis: Ready to remove unneeded chunks (spawn chunk loading skipped via mixins).");
    }
}
