package dev.shadowsoffire.apothic_enchanting.objects;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apothic_enchanting.api.EnchantmentStatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class GeodeShelfBlock extends HorizontalDirectionalBlock implements EnchantmentStatBlock {

    public GeodeShelfBlock(Properties pProperties) {
        super(pProperties);
    }

    public float getEnchantPowerBonus(BlockState state, BlockGetter level, BlockPos pos) {
        return 5;
    }

    @Override
    public float getMaxEnchantingPower(BlockState state, BlockGetter level, BlockPos pos) {
        return 60;
    }

    @Override
    public float getQuantaBonus(BlockState state, BlockGetter level, BlockPos pos) {
        return -15F;
    }

    @Override
    public boolean providesStability(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

}
