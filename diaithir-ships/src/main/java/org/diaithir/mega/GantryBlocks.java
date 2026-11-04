package org.diaithir.mega;

import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.diaithir.mega.gantry.GantryAssembly;
import org.diaithir.mega.gantry.GantryAssembly.Kind;
import org.diaithir.mega.gantry.GantryAssembly.Pos;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;

/** Gantry shaft, gantry carriage and the assembler that lifts a connected gantry into a VS2 ship (rules: mega.gantry.GantryAssembly). */
public final class GantryBlocks {
    private GantryBlocks() {}

    public static final int LIMIT = 4096;
    private static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    /** Directional block used for both shaft and carriage. */
    public static class Directional extends Block {
        public static final MapCodec<Directional> CODEC = simpleCodec(Directional::new);
        public Directional(Properties p) {
            super(p);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        }
        @Override protected MapCodec<? extends Block> codec() { return CODEC; }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
        @Override public BlockState getStateForPlacement(BlockPlaceContext c) {
            return defaultBlockState().setValue(FACING, c.getNearestLookingDirection().getOpposite());
        }
    }

    public static class Assembler extends Block {
        public static final MapCodec<Assembler> CODEC = simpleCodec(Assembler::new);
        public Assembler(Properties p) { super(p); }
        @Override protected MapCodec<? extends Block> codec() { return CODEC; }

        @Override
        protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            int n = assemble((ServerLevel) level, pos);
            player.sendOverlayMessage(Component.translatable(n > 0 ? "megamachines.gantry.assembled" : "megamachines.gantry.nothing", n));
            return InteractionResult.CONSUME;
        }
    }

    /** Collects the gantry touching {@code assembler} and assembles it into a ship. Returns blocks moved, 0 if none or over the cap. */
    public static int assemble(ServerLevel level, BlockPos assembler) {
        GantryAssembly.Grid grid = grid(level);
        Set<BlockPos> blocks = new HashSet<>();
        for (Direction d : Direction.values()) {
            BlockPos n = assembler.relative(d);
            if (grid.kind(new Pos(n.getX(), n.getY(), n.getZ())) == Kind.AIR) continue;
            Set<Pos> got = GantryAssembly.collect(grid, new Pos(n.getX(), n.getY(), n.getZ()), LIMIT);
            if (got == null) return 0;
            for (Pos p : got) blocks.add(new BlockPos(p.x(), p.y(), p.z()));
        }
        blocks.remove(assembler);
        if (blocks.isEmpty()) return 0;
        ShipAssembler.INSTANCE.assembleToShip(level, blocks, 1.0);
        return blocks.size();
    }

    private static int dirIndex(Direction d) {
        return switch (d) { case WEST -> 0; case EAST -> 1; case DOWN -> 2; case UP -> 3; case NORTH -> 4; case SOUTH -> 5; };
    }

    private static GantryAssembly.Grid grid(Level level) {
        return new GantryAssembly.Grid() {
            private BlockState st(Pos p) { return level.getBlockState(new BlockPos(p.x(), p.y(), p.z())); }
            public Kind kind(Pos p) {
                BlockState s = st(p);
                if (s.isAir()) return Kind.AIR;
                if (s.is(MegaBlocks.GANTRY_SHAFT.get())) return Kind.SHAFT;
                if (s.is(MegaBlocks.GANTRY_CARRIAGE.get())) return Kind.CARRIAGE;
                return Kind.OTHER;
            }
            public int facing(Pos p) {
                BlockState s = st(p);
                return s.hasProperty(FACING) ? dirIndex(s.getValue(FACING)) : 0;
            }
            public int axis(Pos p) { return facing(p) / 2; }
        };
    }
}
