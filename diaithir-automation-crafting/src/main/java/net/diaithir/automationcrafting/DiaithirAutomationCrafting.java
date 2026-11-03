package net.diaithir.automationcrafting;

import com.mojang.serialization.Codec;
import net.diaithir.automationcrafting.packages.BenchBlock;
import net.diaithir.automationcrafting.packages.BenchEntity;
import net.diaithir.automationcrafting.packages.Kind;
import net.diaithir.automationcrafting.packages.PackageCommands;
import net.diaithir.automationcrafting.packages.Packages;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Automation and crafting module. Holds the port of The Package Mod (whizzball1, LGPL-3.0, Minecraft 1.12.2): research, then bulk crafting in
 * packages. Source under packages/. Ids use the pkgmod_ prefix so Codex and quests group them.
 */
@Mod(DiaithirAutomationCrafting.ID)
public final class DiaithirAutomationCrafting {
    public static final String ID = "diaithirautomationcrafting";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ID);

    /** Which package a crate holds. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PACKAGE_ID = COMPONENTS.registerComponentType("package_id",
            b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private static BlockBehaviour.Properties steel() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<BenchBlock> RESEARCHER = BLOCKS.registerBlock("pkgmod_researcher", p -> new BenchBlock(p, Kind.RESEARCHER), DiaithirAutomationCrafting::steel);
    public static final DeferredBlock<BenchBlock> PACKAGER = BLOCKS.registerBlock("pkgmod_packager", p -> new BenchBlock(p, Kind.PACKAGER), DiaithirAutomationCrafting::steel);
    public static final DeferredBlock<BenchBlock> UNPACKAGER = BLOCKS.registerBlock("pkgmod_unpackager", p -> new BenchBlock(p, Kind.UNPACKAGER), DiaithirAutomationCrafting::steel);

    public static final DeferredItem<BlockItem> RESEARCHER_ITEM = ITEMS.registerSimpleBlockItem(RESEARCHER);
    public static final DeferredItem<BlockItem> PACKAGER_ITEM = ITEMS.registerSimpleBlockItem(PACKAGER);
    public static final DeferredItem<BlockItem> UNPACKAGER_ITEM = ITEMS.registerSimpleBlockItem(UNPACKAGER);
    public static final DeferredItem<Item> PACKAGE = ITEMS.registerItem("pkgmod_package", Item::new, () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> READER = ITEMS.registerItem("pkgmod_reader", Item::new, () -> new Item.Properties().stacksTo(1));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BenchEntity>> BENCH_BE = BLOCK_ENTITIES.register("pkgmod_bench",
            () -> new BlockEntityType<>(BenchEntity::new, RESEARCHER.get(), PACKAGER.get(), UNPACKAGER.get()));

    public DiaithirAutomationCrafting(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        COMPONENTS.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> e.registerBlockEntity(Capabilities.Item.BLOCK, BENCH_BE.get(), (be, side) -> be.io()));
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (DeferredItem<? extends Item> i : List.of(RESEARCHER_ITEM, PACKAGER_ITEM, UNPACKAGER_ITEM, READER)) e.accept(i.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> PackageCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.AddServerReloadListenersEvent.class, Packages::onAddListeners);
    }
}
