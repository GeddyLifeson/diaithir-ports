package org.diaithir.archimedes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Flood-fills from the helm and turns the connected blocks into a ShipEntity. */
public final class ShipAssembler {
    public enum Code { OK, BLOCK_OVERFLOW, MISSING_MARKER, ERROR }

    public record Result(Code code, ShipEntity ship) {}

    private ShipAssembler() {}

    public static boolean usable(ServerLevel level, BlockPos pos, BlockState s) {
        if (s.isAir() || s.getBlock() instanceof LiquidBlock || !s.getFluidState().isEmpty() && s.getBlock() == Blocks.WATER) return false;
        if (s.is(ArchimedesMod.BUFFER.get())) return false;
        if (s.getDestroySpeed(level, pos) < 0) return false;
        if (s.hasBlockEntity() && !s.is(ArchimedesMod.CRATE.get())) return false;
        return !s.is(ArchimedesTags.NOT_ON_SHIPS);
    }

    public static Result assemble(ServerLevel level, BlockPos helmPos) {
        BlockState helmState = level.getBlockState(helmPos);
        if (!helmState.is(ArchimedesMod.MARKER.get())) return new Result(Code.MISSING_MARKER, null);
        int max = ArchimedesConfig.MAX_BLOCKS.get();
        Set<BlockPos> seen = new HashSet<>();
        List<BlockPos> found = new ArrayList<>();
        ArrayDeque<BlockPos> open = new ArrayDeque<>();
        open.add(helmPos);
        seen.add(helmPos);
        try {
            while (!open.isEmpty()) {
                BlockPos p = open.poll();
                BlockState s = level.getBlockState(p);
                if (!usable(level, p, s)) continue;
                found.add(p);
                if (found.size() > max) return new Result(Code.BLOCK_OVERFLOW, null);
                for (Direction d : Direction.values()) {
                    BlockPos n = p.relative(d);
                    if (seen.add(n)) open.add(n);
                }
            }
        } catch (RuntimeException e) {
            ArchimedesMod.LOG.error("Ship assembly failed", e);
            return new Result(Code.ERROR, null);
        }
        BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (BlockPos p : found) min.set(Math.min(min.getX(), p.getX()), Math.min(min.getY(), p.getY()), Math.min(min.getZ(), p.getZ()));
        BlockPos origin = min.immutable();

        ShipChunk chunk = new ShipChunk();
        for (BlockPos p : found) {
            BlockState s = level.getBlockState(p);
            BlockPos local = p.subtract(origin);
            chunk.put(local, s);
            if (s.is(ArchimedesMod.CRATE.get()) && level.getBlockEntity(p) instanceof org.diaithir.archimedes.block.CrateBlock.Entity be) {
                chunk.crates.put(local, net.minecraft.core.NonNullList.copyOf(be.items));
                be.items.clear();
            }
        }
        chunk.helm = helmPos.subtract(origin);
        chunk.front = helmState.getValue(HorizontalDirectionalBlock.FACING).get2DDataValue();
        chunk.finish();

        ShipEntity ship = new ShipEntity(ArchimedesMod.SHIP.get(), level);
        ship.setChunk(chunk);
        ship.setPos(origin.getX() + chunk.centerX(), origin.getY(), origin.getZ() + chunk.centerZ());
        ship.setYRot(0);
        for (BlockPos p : found) level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.addFreshEntity(ship);
        return new Result(Code.OK, ship);
    }
}
