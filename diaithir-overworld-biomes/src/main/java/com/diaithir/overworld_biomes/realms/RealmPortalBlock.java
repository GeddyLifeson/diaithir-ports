package com.diaithir.overworld_biomes.realms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Doorway between the overworld (or {@code back}) and a realm dimension. Same x and z on both sides.
 * The exit gets a 3x3 pad and a portal column of the same block, so the trip can be reversed.
 */
public class RealmPortalBlock extends Block implements Portal {
    private final ResourceKey<Level> target;
    private final ResourceKey<Level> back;
    private final Identifier padBlock;

    public RealmPortalBlock(Properties props, String target, String back, String padBlock) {
        super(props);
        this.target = ResourceKey.create(Registries.DIMENSION, Identifier.parse(target));
        this.back = ResourceKey.create(Registries.DIMENSION, Identifier.parse(back));
        this.padBlock = Identifier.parse(padBlock);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean precise) {
        if (entity.canUsePortal(false)) entity.setAsInsidePortal(this, pos);
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return 60;
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerLevel from, Entity entity, BlockPos entry) {
        ResourceKey<Level> to = from.dimension().equals(target) ? back : target;
        ServerLevel dest = from.getServer().getLevel(to);
        if (dest == null) return null;
        BlockPos spot = findSpot(dest, entry.getX(), entry.getZ(), from.dimension().equals(target));
        build(dest, spot);
        return new TeleportTransition(dest, Vec3.atBottomCenterOf(spot), Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    /** lowest standing spot with two air blocks above a solid floor, scanning up from y=8 (or down from the surface when leaving a realm). */
    private static BlockPos findSpot(ServerLevel level, int x, int z, boolean surface) {
        int lo = level.getMinY() + 8, hi = level.getMinY() + level.getHeight() - 8;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x, 0, z);
        if (surface) {
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            return new BlockPos(x, Math.max(y, lo), z);
        }
        for (int y = lo; y < hi; y++) {
            p.setY(y);
            if (!level.getBlockState(p.below()).isAir() && level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()
                    && level.getBlockState(p.below()).getFluidState().isEmpty()) return p.immutable();
        }
        return new BlockPos(x, (lo + hi) / 2, z);
    }

    private void build(ServerLevel level, BlockPos spot) {
        BlockState pad = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(padBlock).defaultBlockState();
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlockAndUpdate(spot.offset(dx, -1, dz), pad);
                if (dx != 0 || dz != 0) for (int dy = 0; dy < 3; dy++) level.setBlockAndUpdate(spot.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
            }
        level.setBlockAndUpdate(spot, defaultBlockState());
        level.setBlockAndUpdate(spot.above(), defaultBlockState());
    }
}
