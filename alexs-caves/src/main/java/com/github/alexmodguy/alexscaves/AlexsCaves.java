package com.github.alexmodguy.alexscaves;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import com.github.alexmodguy.alexscaves.server.CommonProxy;
import com.github.alexmodguy.alexscaves.server.config.ACServerConfig;
import org.slf4j.Logger;

@Mod(AlexsCaves.MODID)
public class AlexsCaves {
    public static final String MODID = "alexscaves";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static CommonProxy PROXY = new CommonProxy();
    public static final ACServerConfig COMMON_CONFIG;
    private static final ModConfigSpec COMMON_CONFIG_SPEC;
    public static final java.util.List<String> MOD_GENERATION_CONFLICTS = new java.util.ArrayList<>();

    static {
        final Pair<ACServerConfig, ModConfigSpec> serverPair = new ModConfigSpec.Builder().configure(ACServerConfig::new);
        COMMON_CONFIG = serverPair.getLeft();
        COMMON_CONFIG_SPEC = serverPair.getRight();
    }

    public AlexsCaves(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG_SPEC, "alexscaves-general.toml");
        // registries are attached stage by stage, see PORT_PLAN.md
    }
}
