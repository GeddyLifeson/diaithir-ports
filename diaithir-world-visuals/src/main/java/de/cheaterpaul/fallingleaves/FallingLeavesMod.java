package de.cheaterpaul.fallingleaves;

import com.mojang.logging.LogUtils;
import de.cheaterpaul.fallingleaves.config.Config;
import de.cheaterpaul.fallingleaves.data.provider.LeafProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

public class FallingLeavesMod {
    // Ported from Falling Leaves NeoForge (LGPL-3.0-only) https://github.com/cheaterpaul/fallingleaves (neo/26.1); hosted in diaithir-world-visuals
    public static final String MODID = "fallingleaves";

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CONFIG_SPEC, "diaithir_worldvisuals_fallingleaves-client.toml");
    }

    public static Identifier modId(String string) {
        return Identifier.fromNamespaceAndPath(MODID, string);
    }

}
