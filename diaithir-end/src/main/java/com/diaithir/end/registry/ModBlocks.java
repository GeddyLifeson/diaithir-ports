package com.diaithir.end.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final String MOD_ID = "diaithir_end";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredBlock<Block> VOID_STONE = BLOCKS.registerBlock("void_stone",
            props -> new Block(props.mapColor(MapColor.COLOR_BLACK).strength(3.0F, 9.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)),
            () -> BlockBehaviour.Properties.of());

    public static final DeferredBlock<Block> ASH_SEDIMENT = BLOCKS.registerBlock("ash_sediment",
            props -> new Block(props.mapColor(MapColor.COLOR_GRAY).strength(0.6F).sound(SoundType.SAND)),
            () -> BlockBehaviour.Properties.of());

    public static final DeferredItem<BlockItem> VOID_STONE_ITEM = ITEMS.registerSimpleBlockItem("void_stone", VOID_STONE);
    public static final DeferredItem<BlockItem> ASH_SEDIMENT_ITEM = ITEMS.registerSimpleBlockItem("ash_sediment", ASH_SEDIMENT);

    private ModBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
