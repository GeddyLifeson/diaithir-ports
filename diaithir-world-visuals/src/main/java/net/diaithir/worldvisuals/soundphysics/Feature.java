package net.diaithir.worldvisuals.soundphysics;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Muffles sounds that reach the player through solid blocks. Occlusion only (no reverb); clean-room, written from how
 * sound-physics mods behave.
 */
public final class Feature {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    static final ModConfigSpec.BooleanValue ENABLED = B.define("enabled", true);
    static final ModConfigSpec.DoubleValue PER_BLOCK = B.comment("Volume kept after each solid block between you and the sound").defineInRange("perBlock", 0.6, 0.05, 1.0);
    static final ModConfigSpec.IntValue MAX_BLOCKS = B.comment("Solid blocks counted at most").defineInRange("maxBlocks", 4, 1, 16);
    static final ModConfigSpec SPEC = B.build();

    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, SPEC, "diaithir-world-visuals-soundphysics-client.toml");
        NeoForge.EVENT_BUS.addListener(Occlusion::onSound);
    }
}
