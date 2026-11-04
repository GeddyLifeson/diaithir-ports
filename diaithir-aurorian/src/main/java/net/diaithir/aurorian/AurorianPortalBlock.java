package net.diaithir.aurorian;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The Aurorian portal sheet; stepping in takes you between the Overworld and The Aurorian at the same x and z. */
public class AurorianPortalBlock extends Block implements Portal {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final ResourceKey<Level> AURORIAN = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(DiaithirAurorian.ID, "the_aurorian"));
    private static final VoxelShape NS = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape EW = Block.box(6, 0, 0, 10, 16, 16);
    private static final int SEARCH = 16;

    public AurorianPortalBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return s.getValue(FACING).getAxis() == Direction.Axis.Z ? NS : EW;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir,
                                     BlockPos npos, BlockState nstate, RandomSource random) {
        Direction.Axis plane = state.getValue(FACING).getClockWise().getAxis();
        boolean inPlane = dir.getAxis() == Direction.Axis.Y || dir.getAxis() == plane;
        if (inPlane && !nstate.is(this) && !nstate.is(DiaithirAurorian.PORTAL_FRAME.get())) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, dir, npos, nstate, random);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean precise) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return entity instanceof net.minecraft.world.entity.player.Player pl && !pl.getAbilities().invulnerable ? 80 : 0;
    }

    @Override
    public TeleportTransition getPortalDestination(ServerLevel from, Entity entity, BlockPos entry) {
        ResourceKey<Level> to = from.dimension().equals(AURORIAN) ? Level.OVERWORLD : AURORIAN;
        ServerLevel dest = from.getServer().getLevel(to);
        if (dest == null) {
            return null;
        }
        BlockPos approx = dest.getWorldBorder().clampToBounds(entity.getX(), entity.getY(), entity.getZ());
        Direction facing = from.getBlockState(entry).getOptionalValue(FACING).orElse(Direction.NORTH);
        BlockPos exit = findPortal(dest, approx).orElseGet(() -> build(dest, approx, facing));
        Direction f = dest.getBlockState(exit).getOptionalValue(FACING).orElse(Direction.NORTH);
        Vec3 pos = Vec3.atBottomCenterOf(exit).relative(f, 1.0);
        float yaw = f.toYRot();
        return new TeleportTransition(dest, pos, Vec3.ZERO, yaw, entity.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    private static Optional<BlockPos> findPortal(ServerLevel level, BlockPos around) {
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(around.offset(-SEARCH, 0, -SEARCH), around.offset(SEARCH, 0, SEARCH))) {
            for (int y = level.getMaxY(); y >= level.getMinY(); y--) {
                BlockPos q = new BlockPos(p.getX(), y, p.getZ());
                if (level.getBlockState(q).is(DiaithirAurorian.PORTAL.get()) && !level.getBlockState(q.below()).is(DiaithirAurorian.PORTAL.get())) {
                    double d = q.distSqr(around);
                    if (d < bestD) {
                        bestD = d;
                        best = q.immutable();
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /** Builds a 2x3 frame on a cleared platform at the surface and returns its lowest interior block. */
    private static BlockPos build(ServerLevel level, BlockPos approx, Direction facing) {
        level.getChunk(approx);
        int y = Math.max(level.getMinY() + 2, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, approx.getX(), approx.getZ()));
        BlockPos base = new BlockPos(approx.getX(), y, approx.getZ());
        Direction along = facing.getClockWise();
        BlockState frame = DiaithirAurorian.PORTAL_FRAME.get().defaultBlockState();
        BlockState portal = DiaithirAurorian.PORTAL.get().defaultBlockState().setValue(FACING, facing);
        for (int i = -1; i <= 2; i++) {
            for (int h = -1; h <= 3; h++) {
                for (int d = -2; d <= 2; d++) {
                    BlockPos p = base.relative(along, i).above(h).relative(facing, d);
                    boolean rim = i == -1 || i == 2 || h == -1 || h == 3;
                    if (d == 0) {
                        level.setBlock(p, rim ? frame : portal, 18);
                    } else if (h >= 0) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 18);
                    } else if (level.getBlockState(p).isAir() || !level.getBlockState(p).getFluidState().isEmpty()) {
                        level.setBlock(p, frame, 18);
                    }
                }
            }
        }
        return base;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(AurorianPortalBlock::new);
    }
}
