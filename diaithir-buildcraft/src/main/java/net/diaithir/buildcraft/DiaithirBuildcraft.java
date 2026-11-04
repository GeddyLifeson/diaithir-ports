package net.diaithir.buildcraft;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(DiaithirBuildcraft.ID)
public final class DiaithirBuildcraft {
    public static final String ID = "diaithirbuildcraft";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    static {
        for (Materials.Mat m : Materials.WOODS) {
            var base = blockOf(m.block());
            add(m.id() + "_roof", p -> new StairBlock(base.defaultBlockState(), p), BlockBehaviour.Properties.ofFullCopy(base));
            add(m.id() + "_paving", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(base));
            BlockSetType set = setOf(m.setType());
            add(m.id() + "_shutter", p -> new TrapDoorBlock(set, p), BlockBehaviour.Properties.ofFullCopy(base).noOcclusion());
        }
        for (Materials.Mat m : Materials.STONES) {
            var base = blockOf(m.block());
            add(m.id() + "_roof", p -> new StairBlock(base.defaultBlockState(), p), BlockBehaviour.Properties.ofFullCopy(base));
            add(m.id() + "_paving", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(base));
        }
    }

    private static net.minecraft.world.level.block.Block blockOf(String field) {
        try {
            return (net.minecraft.world.level.block.Block) Blocks.class.getField(field).get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(field, e);
        }
    }

    private static BlockSetType setOf(String field) {
        try {
            return (BlockSetType) BlockSetType.class.getField(field).get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(field, e);
        }
    }

    private static void add(String name, java.util.function.Function<BlockBehaviour.Properties, net.minecraft.world.level.block.Block> make, BlockBehaviour.Properties props) {
        var b = BLOCKS.registerBlock(name, make, () -> props);
        ITEMS.registerSimpleBlockItem(b);
    }

    public DiaithirBuildcraft(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
