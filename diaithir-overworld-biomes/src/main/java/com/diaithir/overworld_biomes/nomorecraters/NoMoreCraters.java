package com.diaithir.overworld_biomes.nomorecraters;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(NoMoreCraters.MOD_ID)
public final class NoMoreCraters {
    public static final String MOD_ID = "diaithir_overworld_biomes";
    public static final Identifier LEGACY_MOD_ID_NOTE = Identifier.fromNamespaceAndPath("nomorecraters", "config");

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredBlock<Block> METEOR_TRACE = BLOCKS.registerBlock("meteor_trace",
            Block::new,
            () -> BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> METEOR_TRACE_ITEM = ITEMS.registerSimpleBlockItem("meteor_trace", METEOR_TRACE);

    public NoMoreCraters(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, MeteorConfig.SPEC, "nomorecraters-common.toml");
        MeteorEvents.register();
    }
}
