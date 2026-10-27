package com.otectus.immersivetanning;

import com.mojang.logging.LogUtils;
import com.otectus.immersivetanning.config.ClientConfig;
import com.otectus.immersivetanning.config.ServerConfig;
import com.otectus.immersivetanning.registry.ModBlockEntities;
import com.otectus.immersivetanning.registry.ModBlocks;
import com.otectus.immersivetanning.registry.ModCreativeTabs;
import com.otectus.immersivetanning.registry.ModItems;
import com.otectus.immersivetanning.registry.ModLootFunctions;
import com.otectus.immersivetanning.registry.ModLootModifiers;
import com.otectus.immersivetanning.registry.ModSounds;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(ImmersiveTanning.MOD_ID)
public class ImmersiveTanning {
    public static final String MOD_ID = "immersive_tanning";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ImmersiveTanning(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modBus);
        ModLootFunctions.LOOT_FUNCTIONS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
