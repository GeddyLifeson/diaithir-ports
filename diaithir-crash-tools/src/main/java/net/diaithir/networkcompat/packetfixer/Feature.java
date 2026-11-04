// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Replaced the properties file and multi-loader platform services with a ModConfigSpec COMMON config and a direct NeoForge entrypoint.
package net.diaithir.networkcompat.packetfixer;

// NOTE: This feature needs mixins (no NeoForge event can raise the hard-coded packet limits).
// The integrator must register "diaithirnetworkcompat.packetfixer.mixins.json" in neoforge.mods.toml via [[mixins]] config="diaithirnetworkcompat.packetfixer.mixins.json".

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

public final class Feature {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, PacketFixerConfig.SPEC, "diaithir_networkcompat_packetfixer-common.toml");
        modBus.addListener(ModConfigEvent.Loading.class, e -> {
            if (e.getConfig().getSpec() == PacketFixerConfig.SPEC) {
                PacketFixerConfig.apply();
            }
        });
        modBus.addListener(ModConfigEvent.Reloading.class, e -> {
            if (e.getConfig().getSpec() == PacketFixerConfig.SPEC) {
                PacketFixerConfig.apply();
            }
        });
        LOGGER.info("Packet Fixer feature has been initialized!");
    }
}
