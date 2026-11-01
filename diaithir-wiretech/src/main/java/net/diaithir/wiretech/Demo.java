package net.diaithir.wiretech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** /wiretech demo: a permanent showcase south of spawn (wires in all three tiers, generators, machines, building blocks). For screenshots and play. */
final class Demo {
    private Demo() {}

    private static void put(ServerLevel l, BlockPos p, Block b) {
        l.setBlock(p, b.defaultBlockState(), 3);
    }

    private static void pillar(ServerLevel l, BlockPos top) {
        BlockPos q = top.below();
        while (l.getBlockState(q).isAir() && q.getY() > l.getMinY()) {
            put(l, q, Blocks.STONE_BRICKS);
            q = q.below();
        }
    }

    private static void pillarConnector(ServerLevel l, BlockPos p, WireType w) {
        pillar(l, p);
        l.setBlock(p, Machines.CONNECTORS.get(w).get().defaultBlockState().setValue(ConnectorBlock.FACING, Direction.UP), 3);
    }

    /** Dev aid, only with -Dwiretech.demo=true: builds the demo and puts the joining player in front of it, facing it. */
    static void show(net.minecraft.server.level.ServerPlayer p) {
        ServerLevel l = p.level();
        build(l.getServer());
        BlockPos sp = l.getRespawnData().pos();
        int ys = l.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, sp).getY();
        l.getServer().execute(() -> l.getServer().execute(() -> {
        p.teleportTo(l, sp.getX() + 6.5, ys + 8.0, sp.getZ() - 4.0, java.util.Set.of(), 0.0F, 22.0F, true);
        p.getAbilities().flying = true;
        p.onUpdateAbilities();
        }));
    }

    static BlockPos build(MinecraftServer server) {
        ServerLevel l = server.getLevel(Level.OVERWORLD);
        BlockPos sp = l.getRespawnData().pos();
        int ys = l.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, sp).getY();
        int x0 = sp.getX() - 8, z0 = sp.getZ() + 6;
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(x0 - 3, ys - 1, z0 - 3), new BlockPos(x0 + 26, ys + 10, z0 + 10))) {
            l.setBlock(p, p.getY() == ys - 1 ? Blocks.POLISHED_ANDESITE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2 | 16);
        }
        int y = ys + 2;
        // generators: windmill and crank on a dynamo
        put(l, new BlockPos(x0, ys, z0), Blocks.STONE_BRICKS);
        put(l, new BlockPos(x0, ys + 1, z0), Blocks.STONE_BRICKS);
        put(l, new BlockPos(x0 + 1, ys, z0), Blocks.STONE_BRICKS);
        put(l, new BlockPos(x0 + 1, ys + 1, z0), Blocks.STONE_BRICKS);
        l.setBlock(new BlockPos(x0, y, z0), Machines.ROTORS.get(RotorKind.WINDMILL).get().defaultBlockState().setValue(RotorBlock.FACING, Direction.NORTH), 3);
        put(l, new BlockPos(x0 + 1, y, z0), Machines.DYNAMO.get());
        put(l, new BlockPos(x0 + 1, y + 1, z0), Machines.ROTORS.get(RotorKind.HAND_CRANK).get());
        BlockPos a = new BlockPos(x0 + 2, y, z0), b = new BlockPos(x0 + 12, y, z0);
        pillarConnector(l, a, WireType.COPPER);
        pillarConnector(l, b, WireType.COPPER);
        ConnectorBlockEntity.connect(l, a, b, WireType.COPPER);
        pillar(l, b.east());
        put(l, b.east(), Machines.ALL_BLOCKS.get(Kind.CRUSHER.ordinal()).get());
        pillar(l, b.south());
        l.setBlock(b.south(), Machines.ALL_BLOCKS.get(Kind.ARC_FURNACE.ordinal()).get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH), 3);
        // electrum and steel spans in front
        for (WireType w : new WireType[] {WireType.ELECTRUM, WireType.STEEL}) {
            int dz = w == WireType.ELECTRUM ? 4 : 8;
            BlockPos p = new BlockPos(x0 + 2, y, z0 + dz), q = new BlockPos(x0 + 2 + (w == WireType.ELECTRUM ? 20 : 30), y, z0 + dz);
            pillarConnector(l, p, w);
            pillarConnector(l, q, w);
            ConnectorBlockEntity.connect(l, p, q, w);
        }
        // diesel generator, transformer, coke oven, blast furnace
        BlockPos g = new BlockPos(x0 + 14, y, z0);
        pillar(l, g);
        put(l, g, Machines.DIESEL_GENERATOR.get());
        pillar(l, g.east());
        l.setBlock(g.east(), Machines.TRANSFORMERS.get(WireType.ELECTRUM).get().defaultBlockState().setValue(TransformerBlock.FACING, Direction.EAST), 3);
        BlockPos k = new BlockPos(x0 + 18, y, z0);
        pillar(l, k);
        l.setBlock(k, Machines.ALL_BLOCKS.get(Kind.COKE_OVEN.ordinal()).get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH), 3);
        BlockPos f = new BlockPos(x0 + 20, y, z0);
        pillar(l, f);
        l.setBlock(f, Machines.ALL_BLOCKS.get(Kind.BLAST_FURNACE.ordinal()).get().defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH), 3);
        BlockState sheet = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse(DiaithirWiretech.ID + ":sheetmetal_iron")).defaultBlockState();
        for (Direction d : new Direction[] {Direction.UP, Direction.EAST, Direction.WEST, Direction.NORTH}) l.setBlock(f.relative(d), sheet, 3);
        // building blocks
        String[] sm = Machines.SHEETMETAL;
        for (int i = 0; i < sm.length; i++) {
            Block blk = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse(DiaithirWiretech.ID + ":sheetmetal_" + sm[i]));
            put(l, new BlockPos(x0 + 22 + i, ys, z0 + 1), blk);
        }
        for (int i = 0; i < 4; i++) {
            put(l, new BlockPos(x0 + 22, ys + i, z0 + 3), net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse(DiaithirWiretech.ID + ":iron_scaffolding")));
            put(l, new BlockPos(x0 + 23, ys + i, z0 + 3), net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse(DiaithirWiretech.ID + ":steel_scaffolding")));
        }
        return new BlockPos(x0, ys, z0);
    }
}
