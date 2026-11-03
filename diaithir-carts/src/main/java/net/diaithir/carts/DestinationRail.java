package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * A destination rail: a steel-speed detector rail with a channel (0 = any cart, 1 to 16 = a dye colour). It gives a redstone signal while a cart whose
 * label matches the channel is on it, and ignores other carts, so a row of them sorts a mixed line: wire each one to a powered rail or junction.
 * Use a dye on it to set the channel, an empty hand to clear it. Clean-room: written from the behaviour "a detector that reads a label".
 */
public class DestinationRail extends DetectorRailBlock implements RailSpeed {
    public static final MapCodec<DetectorRailBlock> CODEC = simpleCodec(DestinationRail::new);
    public static final IntegerProperty CHANNEL = IntegerProperty.create("channel", 0, 16);

    public DestinationRail(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<DetectorRailBlock> codec() {
        return CODEC;
    }

    @Override
    protected void registerDefaultState() {
        registerDefaultState(stateDefinition.any().setValue(POWERED, false).setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, false).setValue(CHANNEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHANNEL);
    }

    @Override
    public double maxSpeed() {
        return Tier.STEEL.maxSpeed;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean precise) {
        if (!level.isClientSide() && !state.getValue(POWERED)) check(level, pos, state);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) check(level, pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!old.is(state.getBlock())) check(level, pos, updateState(state, level, pos, moved));
    }

    /** The matching cart on this rail, as the detector sees it. */
    private static List<AbstractMinecart> carts(Level level, BlockPos pos, int channel) {
        AABB box = new AABB(pos.getX() + 0.2, pos.getY(), pos.getZ() + 0.2, pos.getX() + 0.8, pos.getY() + 0.8, pos.getZ() + 0.8);
        return level.getEntitiesOfClass(AbstractMinecart.class, box, c -> Destination.matches(channel, Destination.labelOf(c)));
    }

    private void check(Level level, BlockPos pos, BlockState state) {
        if (!canSurvive(state, level, pos)) return;
        boolean was = state.getValue(POWERED);
        boolean now = !carts(level, pos, state.getValue(CHANNEL)).isEmpty();
        if (now != was) {
            BlockState next = state.setValue(POWERED, now);
            level.setBlock(pos, next, 3);
            updatePowerToConnected(level, pos, next, now);
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
            level.setBlocksDirty(pos, state, next);
        }
        if (now) level.scheduleTick(pos, this, 5);
        level.updateNeighbourForOutputSignal(pos, this);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return false;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        DyeColor dye = DyeColor.getColor(stack);
        if (dye == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(CHANNEL, dye.getId() + 1), 3);
            level.scheduleTick(pos, this, 1);
            player.sendOverlayMessage(Component.literal("Destination: " + Destination.describe(dye.getId() + 1)));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (state.getValue(CHANNEL) != 0) {
                level.setBlock(pos, state.setValue(CHANNEL, 0), 3);
                level.scheduleTick(pos, this, 1);
            }
            player.sendOverlayMessage(Component.literal("Destination: any cart. Use a dye on it to choose a destination."));
        }
        return InteractionResult.SUCCESS;
    }
}
