package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;

/** A one-way rail: a plain steel-speed rail that stops any cart rolling against its arrow. Clean-room (no other mod's code, art or names). */
public class OneWayRail extends RailBlock implements RailSpeed {
    public static final MapCodec<RailBlock> CODEC = simpleCodec(OneWayRail::new);
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.Plane.HORIZONTAL);

    public OneWayRail(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<RailBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState s = super.getStateForPlacement(ctx);
        return s == null ? null : s.setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public double maxSpeed() {
        return Tier.STEEL.maxSpeed;
    }

    /** True if a cart moving with this velocity is going against the arrow. */
    public static boolean against(BlockState state, Vec3 velocity) {
        Direction f = state.getValue(FACING);
        return velocity.x * f.getStepX() + velocity.z * f.getStepZ() < -0.02;
    }
}
