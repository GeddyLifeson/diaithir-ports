package mcjty.rftoolspower.modules.powercell;

import mcjty.lib.blocks.RBlock;
import mcjty.lib.modules.IModule;
import mcjty.rftoolspower.modules.powercell.blocks.PowerCellBlock;
import mcjty.rftoolspower.modules.powercell.blocks.PowerCellTileEntity;
import mcjty.rftoolspower.modules.powercell.data.Tier;
import mcjty.rftoolspower.modules.powercell.items.PowerCoreItem;
import mcjty.rftoolspower.setup.Config;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredItem;

import static mcjty.rftoolspower.RFToolsPower.tab;
import static mcjty.rftoolspower.setup.Registration.*;

public class PowerCellModule implements IModule {

    public static final RBlock<PowerCellBlock, BlockItem, PowerCellTileEntity> CELL1 = RBLOCKS.registerBlock("cell1",
            PowerCellTileEntity.class,
            () -> new PowerCellBlock(Tier.TIER1),
            block -> new BlockItem(block.get(), createStandardProperties()),
            (pos, state) -> new PowerCellTileEntity(Tier.TIER1, pos, state)
    );

    public static final RBlock<PowerCellBlock, BlockItem, PowerCellTileEntity> CELL2 = RBLOCKS.registerBlock("cell2",
            PowerCellTileEntity.class,
            () -> new PowerCellBlock(Tier.TIER2),
            block -> new BlockItem(block.get(), createStandardProperties()),
            (pos, state) -> new PowerCellTileEntity(Tier.TIER2, pos, state)
    );

    public static final RBlock<PowerCellBlock, BlockItem, PowerCellTileEntity> CELL3 = RBLOCKS.registerBlock("cell3",
            PowerCellTileEntity.class,
            () -> new PowerCellBlock(Tier.TIER3),
            block -> new BlockItem(block.get(), createStandardProperties()),
            (pos, state) -> new PowerCellTileEntity(Tier.TIER3, pos, state)
    );

    public static final DeferredItem<Item> POWER_CORE1 = ITEMS.register("power_core1", tab(PowerCoreItem::new));
    public static final DeferredItem<Item> POWER_CORE2 = ITEMS.register("power_core2", tab(PowerCoreItem::new));
    public static final DeferredItem<Item> POWER_CORE3 = ITEMS.register("power_core3", tab(PowerCoreItem::new));

    public PowerCellModule(IEventBus bus, Dist dist) {
        if (dist.isClient()) {
        }
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    @Override
    public void initConfig(IEventBus bus) {
        PowerCellConfig.setup(Config.SERVER_BUILDER);
    }

}
