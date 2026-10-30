package net.diaithir.forcefield;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

/** Power-fed force-field projector with upgrade modules. Clean-room: written from public descriptions of the idea only. */
@Mod(DiaithirForcefield.ID)
public final class DiaithirForcefield {
    public static final String ID = "diaithirforcefield";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    public static final DeferredBlock<ProjectorBlock> PROJECTOR = BLOCKS.registerBlock("projector", ProjectorBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 12.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredBlock<FieldBlock> FIELD = BLOCKS.registerBlock("field", FieldBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion().noCollision()
                    .isValidSpawn((s, g, p, t) -> false).isSuffocating((s, g, p) -> false).isViewBlocking((s, g, p) -> false)
                    .isRedstoneConductor((s, g, p) -> false).pushReaction(PushReaction.BLOCK).sound(SoundType.GLASS));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PROJECTOR_ITEM = ITEMS.registerSimpleBlockItem(PROJECTOR);
    public static final Map<Module, DeferredItem<ModuleItem>> MODULES = new EnumMap<>(Module.class);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProjectorBlockEntity>> PROJECTOR_BE = BLOCK_ENTITIES.register("projector",
            () -> new BlockEntityType<>(ProjectorBlockEntity::new, PROJECTOR.get()));

    static {
        for (Module m : Module.values()) {
            MODULES.put(m, ITEMS.registerItem(m.itemId, p -> new ModuleItem(m, p), () -> new Item.Properties().stacksTo(16)));
        }
    }

    public DiaithirForcefield(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> e.registerBlockEntity(Capabilities.Energy.BLOCK, PROJECTOR_BE.get(), (be, side) -> {
            be.energy.refit();
            return be.energy;
        }));
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(PROJECTOR_ITEM.get());
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) MODULES.values().forEach(i -> e.accept(i.get()));
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> FieldCommands.register(e.getDispatcher()));
    }
}
