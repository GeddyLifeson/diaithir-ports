package net.diaithir.worldvisuals.ctm;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Client only. */
final class ClientSetup {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    private static final ModConfigSpec.BooleanValue ENABLED = B.comment("Pick connected tiles from the neighbours. Off until checked in a client; off shows each block's plain tile 0. Reload chunks (F3+A) after changing it.").define("enabled", false);
    private static final ModConfigSpec SPEC = B.build();

    private ClientSetup() {}

    static boolean enabled() {
        return SPEC.isLoaded() && ENABLED.get();
    }

    static void init(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC, "diaithir-world-visuals-ctm-client.toml");
        bus.addListener((RegisterBlockStateModels e) -> {
            e.registerModel(Identifier.fromNamespaceAndPath(DiaithirCtm.ID, "connected"), CtmModel.Unbaked.CODEC);
            org.slf4j.LoggerFactory.getLogger("diaithirctm").info("Registered diaithirctm:connected block state model");
        });
    }
}
