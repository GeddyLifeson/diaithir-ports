package net.diaithir.sharedspace;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jspecify.annotations.Nullable;

/** Placing a small block against another small block shares the space; breaking takes one block out at a time. */
@EventBusSubscriber(modid = SharedSpace.ID)
public final class Placement {
    private static final double EPS = 1.0E-4;

    private Placement() {
    }

    /** True when the block fits inside one quarter cell once centred: it is no wider than half a block either way. */
    public static boolean isSmall(BlockState state) {
        if (state.isAir() || state.is(SharedSpace.CLUSTER.get()) || state.is(SharedSpace.DENIED) || !state.getFluidState().isEmpty()) {
            return false;
        }
        if (state.hasBlockEntity()) {
            return false;
        }
        if (state.is(SharedSpace.ALLOWED)) {
            return true;
        }
        try {
            VoxelShape s = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (s.isEmpty()) {
                return false;
            }
            return s.min(Direction.Axis.X) >= 0.25 - EPS && s.max(Direction.Axis.X) <= 0.75 + EPS
                && s.min(Direction.Axis.Z) >= 0.25 - EPS && s.max(Direction.Axis.Z) <= 0.75 + EPS;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** The placement context for putting the held block into the space at {@code target} instead of beside it. */
    private static final class SharedContext extends BlockPlaceContext {
        private final BlockPos target;

        SharedContext(Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, BlockPos target) {
            super(level, player, hand, stack, hit);
            this.target = target;
            this.replaceClicked = false;
        }

        @Override
        public BlockPos getClickedPos() {
            return target;
        }

        @Override
        public boolean canPlace() {
            return true;
        }
    }

    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.isSpectator() || player.isShiftKeyDown() || !(event.getItemStack().getItem() instanceof BlockItem item)) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState clicked = level.getBlockState(pos);
        boolean cluster = clicked.is(SharedSpace.CLUSTER.get());
        if (!cluster && !isSmall(clicked)) {
            return;
        }
        BlockHitResult hit = event.getHitVec();
        ItemStack stack = event.getItemStack();
        BlockPlaceContext ctx = new SharedContext(level, player, event.getHand(), stack, hit, pos);
        BlockState placed = item.getBlock().getStateForPlacement(ctx);
        if (placed != null && !isSmall(placed)) {
            placed = standUp(placed);
        }
        if (placed == null || !isSmall(placed)) {
            return;
        }
        Vec3 at = hit.getLocation();
        double lx = at.x - pos.getX();
        double lz = at.z - pos.getZ();
        Direction face = hit.getDirection();
        // clicking a side face puts the new block on that side of the space
        if (face.getAxis() == Direction.Axis.X) {
            lx = face.getStepX() > 0 ? 0.75 : 0.25;
        } else if (face.getAxis() == Direction.Axis.Z) {
            lz = face.getStepZ() > 0 ? 0.75 : 0.25;
        }
        ClusterBlockEntity be = cluster ? (level.getBlockEntity(pos) instanceof ClusterBlockEntity c ? c : null) : null;
        if (cluster && be == null) {
            return;
        }
        boolean[] taken = cluster ? be.taken() : new boolean[Cells.COUNT];
        int newCell;
        if (cluster) {
            newCell = Cells.nearestFree(taken, lx, lz);
            if (newCell < 0) {
                return;
            }
        } else {
            newCell = Cells.at(lx, lz);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (level.isClientSide()) {
            return;
        }
        if (!cluster) {
            level.setBlock(pos, SharedSpace.CLUSTER.get().defaultBlockState(), Block.UPDATE_ALL);
            be = (ClusterBlockEntity) level.getBlockEntity(pos);
            be.set(Cells.opposite(newCell), clicked);
        }
        be.set(newCell, placed);
        refreshLight(level, pos, be);
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        var sound = placed.getSoundType(level, pos, player);
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, placed));
    }

    /** A chain, rod or similar placed from a side face lies on its side and spans the whole space; stand it upright so it fits a quarter. */
    private static @Nullable BlockState standUp(BlockState state) {
        if (state.hasProperty(BlockStateProperties.AXIS) && state.getValue(BlockStateProperties.AXIS) != Direction.Axis.Y) {
            BlockState up = state.setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
            return isSmall(up) ? up : null;
        }
        if (state.hasProperty(BlockStateProperties.FACING) && state.getValue(BlockStateProperties.FACING).getAxis() != Direction.Axis.Y) {
            BlockState up = state.setValue(BlockStateProperties.FACING, Direction.UP);
            return isSmall(up) ? up : null;
        }
        return null;
    }

    static void refreshLight(Level level, BlockPos pos, ClusterBlockEntity be) {
        BlockState current = level.getBlockState(pos);
        if (current.is(SharedSpace.CLUSTER.get())) {
            int light = be.light();
            if (current.getValue(ClusterBlock.LIGHT) != light) {
                level.setBlock(pos, current.setValue(ClusterBlock.LIGHT, light), Block.UPDATE_ALL);
            }
        }
    }

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (!event.getState().is(SharedSpace.CLUSTER.get())) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            event.setCanceled(true);
            return;
        }
        BlockPos pos = event.getPos();
        if (!(level.getBlockEntity(pos) instanceof ClusterBlockEntity be)) {
            return;
        }
        Player player = event.getPlayer();
        event.setCanceled(true);
        event.setNotifyClient(true);
        int cell = pick(be, level, pos, player);
        if (cell < 0) {
            return;
        }
        BlockState member = be.get(cell);
        be.set(cell, null);
        if (!player.isCreative()) {
            List<ItemStack> drops = Block.getDrops(member, level, pos, null, player, player.getMainHandItem());
            for (ItemStack drop : drops) {
                Block.popResource(level, pos, drop);
            }
        }
        level.levelEvent(2001, pos, Block.getId(member));
        int left = be.count();
        if (left == 1) {
            BlockState only = null;
            for (int i = 0; i < Cells.COUNT; i++) {
                if (be.get(i) != null) {
                    only = be.get(i);
                }
            }
            level.setBlock(pos, only, Block.UPDATE_ALL);
        } else if (left == 0) {
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            refreshLight(level, pos, be);
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);
        }
    }

    /** The member under the crosshair, or the nearest one to the player when the ray misses all of them. */
    static int pick(ClusterBlockEntity be, Level level, BlockPos pos, Player player) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getLookAngle().scale(8.0));
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < Cells.COUNT; i++) {
            if (be.get(i) == null) {
                continue;
            }
            VoxelShape s = ClusterBlock.memberShape(be, i, level, pos, false, CollisionContext.empty());
            BlockHitResult r = s.clip(from, to, pos);
            double d;
            if (r != null) {
                d = r.getLocation().distanceToSqr(from);
            } else {
                d = 1000.0 + Vec3.atCenterOf(pos).add(Cells.offsetX(i), 0, Cells.offsetZ(i)).distanceToSqr(from);
            }
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }
}
