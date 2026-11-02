package net.diaithir.logistics.foundry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Foundry courier depots and ghost placement. Clean-room: written from a behaviour spec (docs/SPEC.md) and the general idea of
 * construction-bot networks; no code or assets from any other mod.
 */
@Mod(DiaithirLogistics.ID)
public final class DiaithirLogistics {
    public static final String ID = "diaithirlogistics";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties ghost() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().noOcclusion().instabreak().noLootTable().sound(SoundType.GLASS);
    }

    public static final DeferredBlock<DepotBlock> DEPOT = BLOCKS.registerBlock("foundry_depot", DepotBlock::new, DiaithirLogistics::metal);
    public static final DeferredBlock<GhostBlock> GHOST = BLOCKS.registerBlock("foundry_ghost", GhostBlock::new, DiaithirLogistics::ghost);

    public static final DeferredItem<BlockItem> DEPOT_ITEM = ITEMS.registerSimpleBlockItem(DEPOT);
    public static final DeferredItem<PlanWandItem> PLAN_WAND = ITEMS.registerItem("foundry_plan_wand", PlanWandItem::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DepotBlockEntity>> DEPOT_BE = BLOCK_ENTITIES.register("foundry_depot",
            () -> new BlockEntityType<>(DepotBlockEntity::new, DEPOT.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GhostBlockEntity>> GHOST_BE = BLOCK_ENTITIES.register("foundry_ghost",
            () -> new BlockEntityType<>(GhostBlockEntity::new, GHOST.get()));

    public DiaithirLogistics(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(DEPOT_ITEM.get());
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(PLAN_WAND.get());
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> FoundryCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, e -> Sites.clear());
    }
}
