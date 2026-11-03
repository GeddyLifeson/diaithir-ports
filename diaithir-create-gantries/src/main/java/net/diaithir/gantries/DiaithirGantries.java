package net.diaithir.gantries;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Gantry split shafts for the Create port. Folded from Create: Extended Gantries (GPL-3.0), see NOTICE. */
@Mod(DiaithirGantries.ID)
public final class DiaithirGantries {
    public static final String ID = "diaithirgantries";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    public static final DeferredBlock<GantrySplitBlock> GANTRY_SPLIT = BLOCKS.registerBlock("gantry_split", GantrySplitBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(3.0F, 6.0F).sound(SoundType.NETHERITE_BLOCK).forceSolidOn().requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> GANTRY_SPLIT_ITEM = ITEMS.registerSimpleBlockItem(GANTRY_SPLIT);

    public DiaithirGantries(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        // the split uses Create's own gantry shaft block entity, so that type must accept it
        bus.addListener(BlockEntityTypeAddBlocksEvent.class, e -> e.modify(com.zurrtum.create.AllBlockEntityTypes.GANTRY_SHAFT, GANTRY_SPLIT.get()));
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(GANTRY_SPLIT_ITEM.get());
        });
    }
}
