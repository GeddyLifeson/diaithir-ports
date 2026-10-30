package net.diaithir.wiretech;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * Builds a real wire chain high above spawn: crank, dynamo, two connectors joined with a coil item used through a fake player,
 * a crusher and an arc furnace on the far end. Then checks range, tier and loss limits, generators, the machines, wire removal and the age gate.
 */
final class SelfTest {
    private SelfTest() {}

    private static void set(ServerLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, 3);
    }

    private static BlockState connector(WireType w) {
        return Machines.CONNECTORS.get(w).get().defaultBlockState().setValue(ConnectorBlock.FACING, Direction.UP);
    }

    private static int insert(ServerLevel level, BlockPos pos, Item item, int count) {
        ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, pos, Direction.UP);
        if (h == null) return -1;
        try (Transaction tx = Transaction.openRoot()) {
            int n = h.insert(ItemResource.of(item), count, tx);
            tx.commit();
            return n;
        }
    }

    private static int feInsert(ServerLevel level, BlockPos pos, int amount) {
        var cap = level.getCapability(Capabilities.Energy.BLOCK, pos, Direction.UP);
        if (cap == null) return -1;
        try (Transaction tx = Transaction.openRoot()) {
            int n = cap.insert(amount, tx);
            tx.commit();
            return n;
        }
    }

    private static void tick(ServerLevel level, BlockEntity be) {
        if (be instanceof RotorBlockEntity r) r.serverTick(level);
        else if (be instanceof DynamoBlockEntity d) d.serverTick(level);
        else if (be instanceof ConnectorBlockEntity c) c.serverTick(level);
        else if (be instanceof GeneratorBlockEntity g) g.serverTick(level);
        else if (be instanceof TransformerBlockEntity t) t.serverTick(level);
        else if (be instanceof MachineBlockEntity m) m.serverTick(level);
    }

    private static int run(ServerLevel level, int max, List<BlockEntity> bes, Runnable each, BooleanSupplier done) {
        for (int i = 1; i <= max; i++) {
            each.run();
            for (BlockEntity be : bes) tick(level, be);
            if (done.getAsBoolean()) return i;
        }
        return -1;
    }

    private static boolean has(MachineBlockEntity be, int slot, Item item, int count) {
        ItemStack s = be.getItem(slot);
        return s.is(item) && s.getCount() >= count;
    }

    private static InteractionResult useCoil(ServerLevel level, FakePlayer player, ItemStack coil, BlockPos at) {
        UseOnContext ctx = new UseOnContext(level, player, InteractionHand.MAIN_HAND, coil, new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false));
        return coil.getItem().useOn(ctx);
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos spawn = level.getRespawnData().pos();
        BlockPos base = new BlockPos(spawn.getX(), 240, spawn.getZ());
        clear(level, base);
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("wt-test".getBytes()), "wt_test"));
        try {
            // ---- the main chain: crank -> dynamo -> A ~~wire~~ B -> crusher and arc furnace -------------------------------------
            BlockPos pCrank = base, pDyn = base.east(), pA = base.east(2), pB = base.east(14), pCrusher = base.east(15), pArc = base.east(14).south();
            for (BlockPos p : new BlockPos[] {pA, pB}) set(level, p.below(), Blocks.STONE.defaultBlockState());
            set(level, pCrank, Machines.ROTORS.get(RotorKind.HAND_CRANK).get().defaultBlockState());
            set(level, pDyn, Machines.DYNAMO.get().defaultBlockState());
            set(level, pA, connector(WireType.COPPER));
            set(level, pB, connector(WireType.COPPER));
            set(level, pCrusher, Machines.ALL_BLOCKS.get(Kind.CRUSHER.ordinal()).get().defaultBlockState());
            set(level, pArc, Machines.ALL_BLOCKS.get(Kind.ARC_FURNACE.ordinal()).get().defaultBlockState());
            if (!(level.getBlockEntity(pCrank) instanceof RotorBlockEntity crank) || !(level.getBlockEntity(pDyn) instanceof DynamoBlockEntity dyn)
                    || !(level.getBlockEntity(pA) instanceof ConnectorBlockEntity a) || !(level.getBlockEntity(pB) instanceof ConnectorBlockEntity b)
                    || !(level.getBlockEntity(pCrusher) instanceof MachineBlockEntity crusher) || !(level.getBlockEntity(pArc) instanceof MachineBlockEntity arc)) {
                fails.add("a block entity is missing in the main chain (connector may have popped off its support)");
                return fails;
            }
            if (level.getCapability(Capabilities.Energy.BLOCK, pA, Direction.NORTH) == null) fails.add("connector has no energy capability");
            if (level.getCapability(Capabilities.Energy.BLOCK, pDyn, Direction.NORTH) == null) fails.add("dynamo has no energy capability");

            // wiring with a real coil item
            ItemStack coil = new ItemStack(Machines.COILS.get(WireType.COPPER).get(), 3);
            if (useCoil(level, player, coil, pA) != InteractionResult.SUCCESS) fails.add("coil on first connector did not succeed");
            if (coil.get(Machines.WIRE_FROM.get()) == null) fails.add("coil did not remember the first connector");
            if (useCoil(level, player, coil, pB) != InteractionResult.SUCCESS) fails.add("coil on second connector did not succeed");
            if (coil.getCount() != 2) fails.add("coil not consumed: " + coil.getCount());
            if (!a.links().contains(pB) || !b.links().contains(pA)) fails.add("wire not stored on both connectors");
            if (ConnectorBlockEntity.connect(level, pA, pB, WireType.COPPER) == null) fails.add("duplicate wire accepted");

            // limits: range, tier, self
            BlockPos pFar = base.east(2 + 17).south(3);
            set(level, pFar.below(), Blocks.STONE.defaultBlockState());
            set(level, pFar, connector(WireType.COPPER));
            String e1 = ConnectorBlockEntity.connect(level, pA, pFar, WireType.COPPER);
            if (e1 == null || !e1.startsWith("Too far")) fails.add("copper wire past 16 blocks was not refused: " + e1);
            if (ConnectorBlockEntity.connect(level, pA, pFar, WireType.STEEL) == null) fails.add("steel wire joined copper connectors");
            if (ConnectorBlockEntity.connect(level, pA, pA, WireType.COPPER) == null) fails.add("connector wired to itself");

            // generators and machines
            insert(level, pCrusher, Items.IRON_ORE, 2);
            insert(level, pArc, Items.IRON_INGOT, 1);
            insert(level, pArc, Machines.MATERIALS.get(3).get(), 1); // coal dust
            List<BlockEntity> chain = List.of(crank, dyn, a, b, crusher, arc);
            int[] n = {0};
            Item ironDust = Machines.MATERIALS.get(0).get(), steel = Machines.MATERIALS.get(7).get();
            int ticks = run(level, 4000, chain, () -> {
                if (n[0]++ % 8 == 0) crank.crank();
            }, () -> has(crusher, Kind.OUT, ironDust, 2) && has(arc, Kind.OUT, steel, 1));
            if (ticks < 0) {
                fails.add("crank power did not finish both jobs: crusher " + crusher.debug() + " | arc " + arc.debug());
            }
            if (!has(crusher, Kind.OUT, ironDust, 2)) fails.add("crusher produced no iron dust");
            if (!has(arc, Kind.OUT, steel, 1)) fails.add("arc furnace made no steel");
            if (b.energy.amount() == 0 && a.energy.amount() == 0 && ticks < 0) fails.add("no FE reached the connectors");

            // loss and flow over a copper pair 12 blocks apart
            BlockPos pP = base.south(6), pQ = base.south(6).east(12);
            set(level, pP.below(), Blocks.STONE.defaultBlockState());
            set(level, pQ.below(), Blocks.STONE.defaultBlockState());
            set(level, pP, connector(WireType.COPPER));
            set(level, pQ, connector(WireType.COPPER));
            if (ConnectorBlockEntity.connect(level, pP, pQ, WireType.COPPER) != null) fails.add("12 block copper wire refused");
            else if (level.getBlockEntity(pP) instanceof ConnectorBlockEntity p && level.getBlockEntity(pQ) instanceof ConnectorBlockEntity q) {
                for (int i = 0; i < 4; i++) feInsert(level, pP, 250);
                if (p.energy.amount() != 1000) fails.add("connector took " + p.energy.amount() + " of 1000 FE");
                run(level, 400, List.of(p, q), () -> {}, () -> false);
                int total = p.energy.amount() + q.energy.amount();
                if (q.energy.amount() <= 0) fails.add("no FE crossed the wire");
                if (total >= 1000) fails.add("wire lost nothing: " + total);
                if (total < 500) fails.add("wire lost too much: " + total);
                double eff = WireType.COPPER.efficiency(12);
                if (Math.abs(eff - 0.82) > 0.001) fails.add("copper efficiency at 12 blocks is " + eff);
            }

            // steel reaches further than copper
            BlockPos pS1 = base.south(10), pS2 = base.south(10).east(30), pS3 = base.south(10).east(34);
            for (BlockPos p : new BlockPos[] {pS1, pS2, pS3}) {
                set(level, p.below(), Blocks.STONE.defaultBlockState());
                set(level, p, connector(WireType.STEEL));
            }
            if (ConnectorBlockEntity.connect(level, pS1, pS2, WireType.STEEL) != null) fails.add("30 block steel wire refused");
            if (ConnectorBlockEntity.connect(level, pS1, pS3, WireType.STEEL) == null) fails.add("34 block steel wire accepted");

            // wire removal: the far end loses its link and the coil drops
            level.destroyBlock(pB, false);
            if (!a.links().isEmpty()) fails.add("removing a connector left a dangling wire");
            if (level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(pB).inflate(3)).stream()
                    .noneMatch(e -> e.getItem().is(Machines.COILS.get(WireType.COPPER).get()))) fails.add("removed wire dropped no coil");

            // windmill: open frame gives torque, a blocked frame gives none; dynamo reads it
            BlockPos pW = base.south(16), pWd = pW.east();
            set(level, pW, Machines.ROTORS.get(RotorKind.WINDMILL).get().defaultBlockState());
            set(level, pWd, Machines.DYNAMO.get().defaultBlockState());
            if (level.getBlockEntity(pW) instanceof RotorBlockEntity w && level.getBlockEntity(pWd) instanceof DynamoBlockEntity d2) {
                w.recompute(level);
                if (w.torque() < 6) fails.add("open windmill torque " + w.torque());
                d2.serverTick(level);
                if (d2.energy.amount() != w.torque() * Config.FE_PER_TORQUE.get()) fails.add("dynamo made " + d2.energy.amount() + " FE from torque " + w.torque());
                Direction right = level.getBlockState(pW).getValue(RotorBlock.FACING).getClockWise();
                for (int j = 1; j <= 2; j++) for (int i = -2; i <= 2; i++) set(level, pW.relative(right, i).above(j), Blocks.STONE.defaultBlockState());
                w.recompute(level);
                if (w.torque() != 0) fails.add("blocked windmill still turns: " + w.torque());
            } else fails.add("windmill or dynamo missing");

            // water wheel: torque per side touching flowing water
            BlockPos pWw = base.south(22);
            set(level, pWw, Machines.ROTORS.get(RotorKind.WATER_WHEEL).get().defaultBlockState());
            set(level, pWw.north(), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2));
            set(level, pWw.south(), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2));
            set(level, pWw.east(), Blocks.WATER.defaultBlockState());
            if (level.getBlockEntity(pWw) instanceof RotorBlockEntity ww) {
                ww.recompute(level);
                if (ww.torque() != 2 * Config.WATERWHEEL_TORQUE.get()) fails.add("water wheel torque " + ww.torque());
            } else fails.add("water wheel missing");

            // hand crank dies down
            crank.crank();
            if (crank.torque() <= 0) fails.add("crank gives no torque");
            for (int i = 0; i < Config.CRANK_TICKS.get() + 2; i++) crank.serverTick(level);
            if (crank.torque() != 0) fails.add("crank keeps turning");

            // diesel generator feeds a connector beside it and burns fuel
            BlockPos pG = base.south(30), pGc = pG.east();
            set(level, pGc.below(), Blocks.STONE.defaultBlockState());
            set(level, pG, Machines.DIESEL_GENERATOR.get().defaultBlockState());
            set(level, pGc, connector(WireType.COPPER));
            if (level.getBlockEntity(pG) instanceof GeneratorBlockEntity gen && level.getBlockEntity(pGc) instanceof ConnectorBlockEntity gc) {
                if (!gen.refuel(new ItemStack(Machines.DIESEL_CANISTER.get()))) fails.add("generator refused diesel");
                if (gen.refuel(new ItemStack(Machines.BIODIESEL_CANISTER.get()))) fails.add("generator mixed fuels");
                int before = gen.fuelLeft();
                run(level, 20, List.of(gen, gc), () -> {}, () -> false);
                if (gen.fuelLeft() >= before) fails.add("generator burned no fuel");
                if (gc.energy.amount() <= 0) fails.add("generator gave no FE to the connector");
            } else fails.add("generator or connector missing");

            // transformer: steel in at the back, copper out at the front, with a loss, and nothing flows back
            BlockPos pT1 = base.south(34), pT = pT1.east(), pT2 = pT1.east(2);
            set(level, pT1.below(), Blocks.STONE.defaultBlockState());
            set(level, pT2.below(), Blocks.STONE.defaultBlockState());
            set(level, pT1, connector(WireType.STEEL));
            set(level, pT, Machines.TRANSFORMERS.get(WireType.COPPER).get().defaultBlockState().setValue(TransformerBlock.FACING, Direction.EAST));
            set(level, pT2, connector(WireType.COPPER));
            if (level.getBlockEntity(pT1) instanceof ConnectorBlockEntity s1 && level.getBlockEntity(pT) instanceof TransformerBlockEntity tr
                    && level.getBlockEntity(pT2) instanceof ConnectorBlockEntity c2) {
                for (int i = 0; i < 4; i++) feInsert(level, pT1, 1000);
                int start = s1.energy.amount();
                run(level, 60, List.of(s1, tr, c2), () -> {}, () -> false);
                int moved = start - s1.energy.amount() - tr.energy.amount();
                if (c2.energy.amount() <= 0) fails.add("transformer passed no FE");
                if (c2.energy.amount() >= moved) fails.add("transformer lost nothing: " + c2.energy.amount() + " of " + moved);
                if (level.getCapability(Capabilities.Energy.BLOCK, pT, Direction.EAST) != null) fails.add("transformer front accepts FE");
            } else fails.add("transformer chain missing");

            // coke oven
            BlockPos pK = base.south(38);
            set(level, pK, Machines.ALL_BLOCKS.get(Kind.COKE_OVEN.ordinal()).get().defaultBlockState());
            if (level.getBlockEntity(pK) instanceof MachineBlockEntity ko) {
                insert(level, pK, Items.COAL, 1);
                Item coke = Machines.MATERIALS.get(4).get();
                if (run(level, 1500, List.of(ko), () -> ko.energy.set(20000), () -> has(ko, Kind.OUT, coke, 1)) < 0) fails.add("coke oven made no coke: " + ko.debug());
            } else fails.add("coke oven missing");

            // blast furnace: no walls, no work; with sheetmetal above and left, right and behind, steel
            BlockPos pBf = base.south(42).east(4);
            set(level, pBf, Machines.ALL_BLOCKS.get(Kind.BLAST_FURNACE.ordinal()).get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
            if (level.getBlockEntity(pBf) instanceof MachineBlockEntity bf) {
                Item steelIngot = Machines.MATERIALS.get(7).get();
                insert(level, pBf, Items.IRON_INGOT, 2);
                insert(level, pBf, Machines.MATERIALS.get(4).get(), 1);
                if (bf.checkStructure(level)) fails.add("bare blast furnace passed its structure check");
                run(level, 40, List.of(bf), () -> bf.energy.set(20000), () -> false);
                if (bf.progress() != 0) fails.add("blast furnace ran without its structure");
                BlockState sheet = BuiltInRegistries.BLOCK.getValue(Identifier.parse(DiaithirWiretech.ID + ":sheetmetal_iron")).defaultBlockState();
                for (Direction d : new Direction[] {Direction.UP, Direction.EAST, Direction.WEST, Direction.SOUTH}) set(level, pBf.relative(d), sheet);
                if (!bf.checkStructure(level)) fails.add("built blast furnace failed its structure check");
                if (run(level, 1500, List.of(bf), () -> bf.energy.set(20000), () -> has(bf, Kind.OUT, steelIngot, 2)) < 0) fails.add("blast furnace made no steel: " + bf.debug());
            } else fails.add("blast furnace missing");

            // menu of a machine
            AbstractContainerMenu menu = crusher.createMenu(7, player.getInventory(), player);
            if (menu == null || menu.slots.size() != 5 + 36 - 1) fails.add("menu slot count " + (menu == null ? -1 : menu.slots.size()));

            // saved connector keeps its wires
            level.setBlock(pA, connector(WireType.COPPER), 3);
            if (level.getBlockEntity(pA) instanceof ConnectorBlockEntity a2) {
                BlockPos fake = pA.east(3);
                set(level, fake.below(), Blocks.STONE.defaultBlockState());
                set(level, fake, connector(WireType.COPPER));
                if (ConnectorBlockEntity.connect(level, pA, fake, WireType.COPPER) != null) fails.add("could not rewire for save test");
                else if (!a2.saveWithoutMetadata(server.registryAccess()).toString().contains("links")) fails.add("links not saved");
            }

            // age gate lists real items only
            try (var in = SelfTest.class.getResourceAsStream("/data/" + DiaithirWiretech.ID + "/age_gate.json")) {
                if (in == null) fails.add("age_gate.json missing");
                else {
                    JsonObject gate = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                    int count = 0;
                    for (var tier : gate.entrySet()) {
                        if (!tier.getValue().getAsJsonObject().get("age").isJsonPrimitive() || !tier.getValue().getAsJsonObject().get("age").getAsJsonPrimitive().isNumber()) {
                            fails.add("age gate age is not numeric");
                        }
                        for (var el : tier.getValue().getAsJsonObject().getAsJsonArray("items")) {
                            count++;
                            if (!BuiltInRegistries.ITEM.containsKey(Identifier.parse(el.getAsString()))) fails.add("age gate names unknown item " + el.getAsString());
                        }
                    }
                    if (count < 20) fails.add("age gate lists only " + count + " items");
                }
            } catch (java.io.IOException e) {
                fails.add("age gate unreadable: " + e);
            }
        } finally {
            clear(level, base);
        }
        return fails;
    }

    private static void clear(ServerLevel level, BlockPos base) {
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-3, -2, -4), base.offset(45, 6, 44))) {
            if (level.getBlockEntity(p) instanceof MachineBlockEntity be) be.clearContent();
            if (!level.getBlockState(p).isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16 | 64);
        }
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(base).inflate(60, 10, 60))) e.discard();
    }
}
