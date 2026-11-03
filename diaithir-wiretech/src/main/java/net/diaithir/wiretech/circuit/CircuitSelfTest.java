package net.diaithir.wiretech.circuit;

import net.diaithir.wiretech.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/** Builds a real circuit high above spawn (called from /wiretech selftest): constant to arithmetic to decider to gate, a lamp, a reader on a chest, wires, saving, errors. */
public final class CircuitSelfTest {
    private CircuitSelfTest() {}

    private static BlockState block(String id) {
        return Machines.CIRCUIT_BLOCKS.get(id).get().defaultBlockState();
    }

    private static CircuitNodeBE node(ServerLevel level, BlockPos p) {
        return level.getBlockEntity(p) instanceof CircuitNodeBE be ? be : null;
    }

    private static void ticks(ServerLevel level, int n) {
        for (int i = 0; i < n; i++) CircuitGrid.tick(level);
    }

    public static List<String> run(ServerLevel level, BlockPos base) {
        List<String> fails = new ArrayList<>();
        BlockPos pConst = base.south(30), pArith = pConst.east(2), pDec = pArith.east(2), pGate = pDec.east(2), pLamp = pArith.south(2),
                pChest = pLamp.south(6), pReader = pChest.north(), pDec2 = pReader.east(2), pMech = pLamp.south(3);
        for (BlockPos p : new BlockPos[] {pConst, pArith, pDec, pGate, pLamp, pReader, pDec2, pChest, pMech, pGate.east()}) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pConst, block("electronic_constant_combinator"), 3);
        level.setBlock(pArith, block("electronic_arithmetic_combinator"), 3);
        level.setBlock(pDec, block("electronic_decider_combinator"), 3);
        level.setBlock(pGate, block("electronic_circuit_gate"), 3);
        level.setBlock(pLamp, block("electronic_circuit_lamp"), 3);
        level.setBlock(pChest, Blocks.CHEST.defaultBlockState(), 3);
        level.setBlock(pReader, block("electronic_circuit_reader"), 3);
        level.setBlock(pDec2, block("electronic_decider_combinator"), 3);
        level.setBlock(pMech, block("mechanical_arithmetic_combinator"), 3);
        CircuitNodeBE c = node(level, pConst), ar = node(level, pArith), de = node(level, pDec), ga = node(level, pGate), la = node(level, pLamp), de2 = node(level, pDec2);
        if (c == null || ar == null || de == null || ga == null || la == null || de2 == null || node(level, pReader) == null) return List.of("circuit: block entities missing");
        if (CircuitGrid.size(level) < 8) fails.add("circuit: grid holds " + CircuitGrid.size(level) + " nodes, want at least 8");
        try {
            c.setProgram("const a=10 b=3");
            ar.setProgram("arith each * 2 -> each");
            de.setProgram("decide a > 15 -> g one");
            ga.setProgram("when g > 0");
            la.setProgram("when b > 5");
            de2.setProgram("decide minecraft:iron_ingot > 4 -> r one");
            Tier E = Tier.ELECTRONIC;
            String[] errs = {
                    CircuitNodeBE.connect(level, pConst, true, pArith, false, Color.RED, E),
                    CircuitNodeBE.connect(level, pArith, true, pDec, false, Color.RED, E),
                    CircuitNodeBE.connect(level, pArith, true, pLamp, false, Color.GREEN, E),
                    CircuitNodeBE.connect(level, pDec, true, pGate, false, Color.RED, E),
                    CircuitNodeBE.connect(level, pReader, true, pDec2, false, Color.RED, E)};
            for (String e : errs) if (e != null) fails.add("circuit: wiring failed: " + e);
            if (CircuitNodeBE.connect(level, pConst, false, pArith, false, Color.RED, E) == null) fails.add("circuit: constant has no input but accepted a wire");
            if (CircuitNodeBE.connect(level, pConst, true, pArith, false, Color.RED, E) == null) fails.add("circuit: duplicate wire accepted");
            if (CircuitNodeBE.connect(level, pConst, true, pMech, false, Color.RED, Tier.MECHANICAL) == null) {
                fails.add("circuit: mechanical wire accepted on an electronic block");
            }
            if (CircuitNodeBE.connect(level, pConst, true, pArith.east(40), false, Color.RED, E) == null) fails.add("circuit: out of range wire accepted");
            try (Transaction tx = Transaction.openRoot()) {
                var h = level.getCapability(Capabilities.Item.BLOCK, pChest, Direction.UP);
                if (h == null || h.insert(ItemResource.of(Items.IRON_INGOT), 5, tx) != 5) fails.add("circuit: could not fill the chest");
                tx.commit();
            }
            ticks(level, 8);
            if (ar.out.get("virtual:a") != 20 || ar.out.get("virtual:b") != 6) fails.add("circuit: arithmetic said " + ar.out + ", want a=20 b=6");
            if (de.out.get("virtual:g") != 1) fails.add("circuit: decider said " + de.out);
            if (!ga.getBlockState().getValue(CircuitBlock.LIT)) fails.add("circuit: gate stayed off");
            if (!la.getBlockState().getValue(CircuitBlock.LIT)) fails.add("circuit: lamp stayed off (b=6 > 5)");
            if (!CircuitBlock.gatesAllow(level, pGate.east())) fails.add("circuit: a lit gate blocked its neighbour");
            if (de2.out.get("virtual:r") != 1) fails.add("circuit: reader did not see 5 iron ingots: " + node(level, pReader).out + " decider " + de2.out);
            // change the program: lamp off at b > 9, source down so the gate closes
            la.setProgram("when b > 9");
            c.setProgram("const a=0 b=3");
            ticks(level, 8);
            if (la.getBlockState().getValue(CircuitBlock.LIT)) fails.add("circuit: lamp stayed on after program change");
            if (ga.getBlockState().getValue(CircuitBlock.LIT)) fails.add("circuit: gate stayed on after the signal dropped");
            if (CircuitBlock.gatesAllow(level, pGate.east())) fails.add("circuit: an unlit gate did not block its neighbour");
            // direct readers: comparator, block state, rails and carts
            BlockPos pRail = pReader.east(6), pRead2 = pRail.south();
            level.setBlock(pRail.below(), Blocks.STONE.defaultBlockState(), 2);
            level.setBlock(pRail, Blocks.POWERED_RAIL.defaultBlockState().setValue(net.minecraft.world.level.block.PoweredRailBlock.POWERED, true), 3);
            level.setBlock(pRead2, block("electronic_circuit_reader").setValue(CircuitBlock.FACING, Direction.SOUTH), 3);
            var cart = net.minecraft.world.entity.EntityType.CHEST_MINECART.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (cart == null) fails.add("circuit: could not make a cart");
            else {
                cart.setCustomName(net.minecraft.network.chat.Component.literal("Alpha 1"));
                cart.setPos(pRail.getX() + 0.5, pRail.getY() + 0.1, pRail.getZ() + 0.5);
                cart.setItem(0, new net.minecraft.world.item.ItemStack(Items.COAL, 7));
                level.addFreshEntity(cart);
            }
            CircuitNodeBE rd = node(level, pRead2);
            ticks(level, 3);
            if (rd == null || rd.out.get("si:vehicles") != 1 || rd.out.get("minecraft:coal") != 7 || rd.out.get("cart:alpha_1") != 1) {
                fails.add("circuit: rail reader said " + (rd == null ? "null" : rd.out) + ", want vehicles=1 coal=7");
            }
            BlockPos pRep = pRead2.east(2), pRead3 = pRep.south();
            level.setBlock(pRep.below(), Blocks.STONE.defaultBlockState(), 2);
            level.setBlock(pRep, Blocks.REPEATER.defaultBlockState().setValue(net.minecraft.world.level.block.RepeaterBlock.DELAY, 3), 3);
            level.setBlock(pRead3, block("electronic_circuit_reader").setValue(CircuitBlock.FACING, Direction.SOUTH), 3);
            ticks(level, 3);
            if (node(level, pRead3) == null || node(level, pRead3).out.get("state:delay") != 3) {
                fails.add("circuit: block state reader said " + (node(level, pRead3) == null ? "null" : node(level, pRead3).out) + ", want state:delay=3");
            }
            for (BlockPos q : new BlockPos[] {pRead3, pRep, pRep.below()}) level.setBlock(q, Blocks.AIR.defaultBlockState(), 2 | 16 | 64);
            if (node(level, pReader).out.get("si:analog") < 1) fails.add("circuit: chest reader gave no comparator level: " + node(level, pReader).out);
            if (cart != null) cart.discard();
            level.setBlock(pRead2, Blocks.AIR.defaultBlockState(), 2 | 16 | 64);
            level.setBlock(pRail, Blocks.AIR.defaultBlockState(), 2 | 16 | 64);
            level.setBlock(pRail.below(), Blocks.AIR.defaultBlockState(), 2 | 16 | 64);
            // belt contents from a chute save (shape taken from the belt fork's saveAdditional)
            Signals belt = new Signals();
            BeltReading.read(BeltReading.sample(new String[] {"minecraft:coal", "minecraft:iron_ingot", "minecraft:coal"}, new int[] {1, 1, 1}, new byte[] {0, 1, 1}), belt);
            if (belt.get("minecraft:coal") != 2 || belt.get("si:belt_items") != 3 || belt.get("si:belt_lane0") != 1 || belt.get("si:belt_lane1") != 2) {
                fails.add("circuit: belt reader said " + belt);
            }
            // errors and saving
            try {
                c.setProgram("arith a + 1 -> b");
                fails.add("circuit: a constant combinator accepted an arithmetic program");
            } catch (IllegalArgumentException expected) {
                // right
            }
            String saved = ar.saveWithoutMetadata(level.registryAccess()).toString();
            if (!saved.contains("links") || !saved.contains("each * 2")) fails.add("circuit: save lost links or program: " + saved);
            // breaking a block drops its wires and removes the far link
            level.setBlock(pArith, Blocks.AIR.defaultBlockState(), 3);
            if (!de.links().isEmpty() && de.links().stream().anyMatch(l -> l.other().equals(pArith))) fails.add("circuit: far end kept a link to a broken block");
        } catch (RuntimeException e) {
            fails.add("circuit: exception " + e);
        } finally {
            for (BlockPos p : new BlockPos[] {pConst, pArith, pDec, pGate, pLamp, pChest, pReader, pDec2, pMech}) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16 | 64);
        }
        return fails;
    }
}
