package net.diaithir.ctm;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(DiaithirCtm.ID)
public final class DiaithirCtm {
    public static final String ID = "diaithirctm";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    public static final DeferredBlock<Block> GLASS = BLOCKS.registerBlock("connected_glass", Block::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.NONE).sound(SoundType.GLASS).strength(0.3F).noOcclusion().isValidSpawn((s, l, p, t) -> false)
            .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false));
    public static final DeferredBlock<Block> PLATING = BLOCKS.registerBlock("connected_plating", Block::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.0F, 6.0F).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> GLASS_ITEM = ITEMS.registerSimpleBlockItem(GLASS);
    public static final DeferredItem<BlockItem> PLATING_ITEM = ITEMS.registerSimpleBlockItem(PLATING);

    public DiaithirCtm(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        if (FMLEnvironment.getDist().isClient()) {
            ClientSetup.init(bus, container);
        }
    }
}
