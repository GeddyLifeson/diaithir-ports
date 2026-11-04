package net.diaithir.druidry.ars.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

public class ModBlock extends Block {

    public ModBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public ModBlock() {
        super(defaultProperties());
    }

    public static BlockBehaviour.Properties defaultProperties() {
        return net.diaithir.druidry.ars.setup.registry.BlockRegistry.of().sound(SoundType.STONE).strength(2.0f, 6.0f).mapColor(MapColor.STONE);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndLightGetter world, BlockPos pos, FluidState fluidState) {
        return true;
    }
}
