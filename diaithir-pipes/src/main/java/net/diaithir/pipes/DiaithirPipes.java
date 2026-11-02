package net.diaithir.pipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/** Tiered item, fluid and energy pipes, a wrench and a connector. Clean-room: written from a behaviour description only. */
@Mod(DiaithirPipes.ID)
public final class DiaithirPipes {
    public static final String ID = "diaithirpipes";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);

    private static final List<DeferredBlock<PipeBlock2>> PIPES = new ArrayList<>();
    private static final List<DeferredItem<BlockItem>> PIPE_ITEMS = new ArrayList<>();

    static {
        for (PipeKind k : PipeKind.values()) {
            for (PipeTier t : PipeTier.values()) {
                DeferredBlock<PipeBlock2> b = BLOCKS.registerBlock(k.id() + "_pipe_" + t.id(), p -> new PipeBlock2(p, k, t),
                        () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F, 4.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops());
                PIPES.add(b);
                PIPE_ITEMS.add(ITEMS.registerSimpleBlockItem(b));
            }
        }
    }

    public static final DeferredItem<WrenchItem> WRENCH = ITEMS.registerItem("wrench", WrenchItem::new, () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<ConnectorItem> CONNECTOR = ITEMS.registerItem("connector", ConnectorItem::new, () -> new Item.Properties().stacksTo(1));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PipeBlockEntity>> PIPE_BE = BLOCK_ENTITIES.register("pipe",
            () -> new BlockEntityType<>(PipeBlockEntity::new, PIPES.stream().map(DeferredBlock::get).toArray(net.minecraft.world.level.block.Block[]::new)));

    public static final DeferredHolder<MenuType<?>, MenuType<FilterMenu>> FILTER_MENU = MENUS.register("filter",
            () -> new MenuType<>((id, inv) -> new FilterMenu(id, inv, null), FeatureFlags.VANILLA_SET));

    public DiaithirPipes(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (DeferredItem<BlockItem> i : PIPE_ITEMS) e.accept(i.get());
                e.accept(WRENCH.get());
                e.accept(CONNECTOR.get());
            }
        });
    }
}
