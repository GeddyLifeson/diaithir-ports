// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** Logistics pipes (ported design of Not Logistics Pipes): routed item pipes with sink and extractor behaviour, the substrate of Foundry's depot network. */
@Mod(LogisticsMod.ID)
public final class LogisticsMod {
    public static final String ID = "diaithirlogistics";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties pipe() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F, 4.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<LogisticsPipeBlock> PIPE = BLOCKS.registerBlock("pipe", p -> new LogisticsPipeBlock(p, PipeKind.PLAIN), LogisticsMod::pipe);
    public static final DeferredBlock<LogisticsPipeBlock> SINK_PIPE = BLOCKS.registerBlock("sink_pipe", p -> new LogisticsPipeBlock(p, PipeKind.SINK), LogisticsMod::pipe);
    public static final DeferredBlock<LogisticsPipeBlock> EXTRACTOR_PIPE = BLOCKS.registerBlock("extractor_pipe", p -> new LogisticsPipeBlock(p, PipeKind.EXTRACTOR), LogisticsMod::pipe);
    public static final DeferredBlock<LogisticsPipeBlock> SPEED_PIPE = BLOCKS.registerBlock("speed_pipe", p -> new LogisticsPipeBlock(p, PipeKind.SPEED), LogisticsMod::pipe);

    public static final DeferredItem<BlockItem> PIPE_ITEM = ITEMS.registerSimpleBlockItem(PIPE);
    public static final DeferredItem<BlockItem> SINK_PIPE_ITEM = ITEMS.registerSimpleBlockItem(SINK_PIPE);
    public static final DeferredItem<BlockItem> EXTRACTOR_PIPE_ITEM = ITEMS.registerSimpleBlockItem(EXTRACTOR_PIPE);
    public static final DeferredItem<BlockItem> SPEED_PIPE_ITEM = ITEMS.registerSimpleBlockItem(SPEED_PIPE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PipeBlockEntity>> PIPE_BE = BLOCK_ENTITIES.register("pipe",
            () -> new BlockEntityType<>(PipeBlockEntity::new, PIPE.get(), SINK_PIPE.get(), EXTRACTOR_PIPE.get(), SPEED_PIPE.get()));

    public LogisticsMod(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (DeferredItem<BlockItem> i : List.of(PIPE_ITEM, SINK_PIPE_ITEM, EXTRACTOR_PIPE_ITEM, SPEED_PIPE_ITEM)) e.accept(i.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> LogisticsCommands.register(e.getDispatcher()));
    }
}
