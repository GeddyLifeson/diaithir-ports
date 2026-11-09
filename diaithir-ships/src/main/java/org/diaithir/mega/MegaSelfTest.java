package org.diaithir.mega;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.diaithir.mega.site.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dev-only headless check (-Dmegamachines.selftest=true): the cab crew/autonomy rule. */
public final class MegaSelfTest {
    private static final Logger LOG = LoggerFactory.getLogger("Megamachines");
    private int fails;

    @SubscribeEvent
    public void started(ServerStartedEvent e) {
        ServerLevel level = e.getServer().overworld();
        BlockPos pos = new BlockPos(0, 200, 0);
        level.setChunkForced(0, 0, true);
        for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) for (int y = 198; y <= 204; y++)
            level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pos, MegaMod.CAB.get().defaultBlockState(), 2);
        var cab = (MegaCabBlockEntity) level.getBlockEntity(pos);
        check("cab block entity exists", cab != null);
        if (cab == null) { finish(level); return; }

        cab.setSpec("megamachines:bucket_wheel_excavator");
        check("incapable type: not capable", !cab.capable());
        check("incapable type: module refused", !cab.fitModule());
        check("no crew: UNCREWED, may not run", cab.status() == MegaCabBlockEntity.Status.UNCREWED && !cab.mayRun());

        var villager = EntityType.VILLAGER.spawn(level, pos.above(), EntitySpawnReason.COMMAND);
        check("crew nearby: CREWED, may run", villager != null && cab.status() == MegaCabBlockEntity.Status.CREWED && cab.mayRun());
        if (villager != null) villager.discard();

        cab.setSpec("megamachines:autonomous_haul_fleet");
        check("capable type: capable", cab.capable());
        check("capable but no module and no crew: UNCREWED", cab.status() == MegaCabBlockEntity.Status.UNCREWED);
        check("capable type: module fits", cab.fitModule());
        check("module twice refused", !cab.fitModule());
        check("capable + module: AUTONOMOUS, runs crewless", cab.status() == MegaCabBlockEntity.Status.AUTONOMOUS && cab.mayRun());
        check("wages: operator job automated", MegaCabBlockEntity.autonomousCrew("machine_operator", cab));
        check("wages: other jobs untouched", !MegaCabBlockEntity.autonomousCrew("cook", cab));

        check("engines: haul truck takes arcane_motor and diesel, not steam", cab.acceptsEngine("arcane_motor") && !cab.acceptsEngine("steam_piston"));
        cab.setSpec("megamachines:steam_navvy");
        check("engines: Age V steam navvy has no arcane_motor", !cab.acceptsEngine("arcane_motor") && cab.acceptsEngine("steam_piston"));
        check("catalog has 12 vehicles", MegaCatalog.ids().size() == 12);
        physics();
        digHead(level);
        site(level);
        finish(level);
    }

    private void physics() {
        for (String id : MegaCatalog.ids()) {
            MegaSpec sp = MegaSpecs.get(id);
            if (sp.digM3s() > 0) check("catalog rate matches physics: " + id, Math.abs(sp.digM3s() - sp.maxRate()) <= sp.digM3s() * 0.25);
        }
        MegaSpec bwe = MegaSpecs.get("megamachines:bucket_wheel_excavator");
        Dig dig = new Dig(bwe);
        Dig.Deposit d = new Dig.Deposit(100);
        int total = 0;
        for (int t = 0; t < 200; t++) total += dig.tick(bwe.powerMW() * 1e6, 1000, d);
        check("10 s at full power digs rate x time", Math.abs(total - bwe.maxRate() * 10) <= 1);
        check("deposit depleted by the volume dug", Math.abs(d.left() - (100 - total)) < 1e-9 && d.fraction() < 1);
        Dig d2 = new Dig(bwe);
        Dig.Deposit small = new Dig.Deposit(5);
        check("full hopper digs nothing", d2.tick(bwe.powerMW() * 1e6, 0, small) == 0);
        int got = 0;
        for (int t = 0; t < 400; t++) got += d2.tick(bwe.powerMW() * 1e6, 1000, small);
        check("cannot dig more than the deposit holds", got == 5 && small.left() == 0);
        Assay.Reading rich = new Assay.Reading(3, 1.0, "A"), poor = new Assay.Reading(3, 0.0, "A");
        check("assay: richer reading yields more", Assay.apply(10, rich, 1) > Assay.apply(10, poor, 1));
        check("assay: worked-out deposit yields less", Assay.apply(10, rich, 0.1) < Assay.apply(10, rich, 1));
        check("assay: unassayed item still yields", Assay.apply(1, Assay.read("minecraft:stone"), 0.0) >= 1);
    }

    private void digHead(ServerLevel level) {
        BlockPos head = new BlockPos(0, 200, 0), chest = head.west(), cabPos = head.above(2);
        for (int i = 1; i <= 3; i++) level.setBlock(head.east(i), Blocks.STONE.defaultBlockState(), 2);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(head, MegaMod.DIG_HEAD.get().defaultBlockState().setValue(DigHeadBlock.FACING, net.minecraft.core.Direction.EAST), 2);
        var be = (DigHeadBlockEntity) level.getBlockEntity(head);
        check("dig head block entity exists", be != null);
        if (be == null) return;

        for (int t = 0; t < 5; t++) tickHead(level, head, be);
        check("no cab: NO_CAB, nothing dug", be.status() == DigHeadBlockEntity.Status.NO_CAB && be.dugTotal() == 0);

        level.setBlock(cabPos, MegaMod.CAB.get().defaultBlockState(), 2);
        var cab = (MegaCabBlockEntity) level.getBlockEntity(cabPos);
        cab.setSpec("megamachines:bucket_wheel_excavator");
        be.setPower(() -> 1e12);
        be.link(cabPos);
        for (int t = 0; t < 5; t++) tickHead(level, head, be);
        check("cab with no crew: HALTED, nothing dug", be.status() == DigHeadBlockEntity.Status.HALTED && be.dugTotal() == 0);

        for (var stray : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(head).inflate(8))) stray.discard();   // drops left by earlier blocks' checks
        var crew = EntityType.VILLAGER.spawn(level, cabPos.above(), EntitySpawnReason.COMMAND);
        for (int t = 0; t < 400; t++) tickHead(level, head, be);
        int inChest = count(level, chest, Items.COBBLESTONE);
        check("crewed: the stone in reach was dug (2 of 3, reach is 2)", level.getBlockState(head.east(1)).isAir() && level.getBlockState(head.east(2)).isAir() && be.dugTotal() == 2);
        check("drops went into the adjacent chest", inChest >= 2);
        check("nothing spilled into the world", level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(head).inflate(8)).isEmpty());

        // Fill the chest, put fresh stone ahead: the head must stall and leave the block alone.
        var cbe = (net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(chest);
        for (int i = 0; i < cbe.getContainerSize(); i++) cbe.setItem(i, new net.minecraft.world.item.ItemStack(Items.STONE_BRICKS, 64));
        level.setBlock(head.east(1), Blocks.STONE.defaultBlockState(), 2);
        long before = be.dugTotal();
        for (int t = 0; t < 200; t++) tickHead(level, head, be);
        check("full chest: BLOCKED, block untouched", be.status() == DigHeadBlockEntity.Status.BLOCKED && be.dugTotal() == before && level.getBlockState(head.east(1)).is(Blocks.STONE));
        check("full chest: nothing spilled", level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(head).inflate(8)).isEmpty());
        if (crew != null) crew.discard();
    }

    private static void tickHead(ServerLevel level, BlockPos pos, DigHeadBlockEntity be) {
        DigHeadBlockEntity.tick(level, pos, level.getBlockState(pos), be);
    }

    private static int count(ServerLevel level, BlockPos pos, net.minecraft.world.item.Item item) {
        var c = (net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(pos);
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (c.getItem(i).is(item)) n += c.getItem(i).getCount();
        return n;
    }

    /** Site office, depot, stage engine and layout. Needs -Dmegamachines.timescale high (set by MegaMod when the selftest is on). */
    private void site(ServerLevel level) {
        for (int cx = -2; cx <= 2; cx++) for (int cz = -2; cz <= 2; cz++) { level.setChunkForced(cx, cz, true); level.getChunk(cx, cz); }
        BlockPos op = new BlockPos(1, 201, 1), dp = op.east();
        for (int x = 0; x <= 30; x++) for (int z = -14; z <= 14; z++) for (int y = 201; y <= 206; y++)
            level.setBlock(new BlockPos(x + 1, y, z + 1), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(op, MegaMod.OFFICE.get().defaultBlockState(), 2);
        level.setBlock(dp, MegaMod.DEPOT.get().defaultBlockState(), 2);
        var office = (SiteOfficeBlockEntity) level.getBlockEntity(op);
        var depot = (DepotBlockEntity) level.getBlockEntity(dp);
        check("site: office and depot exist", office != null && depot != null);
        if (office == null || depot == null) return;
        check("site: blueprints loaded (7)", Blueprints.ids().size() == 7);

        check("site: dam refused at Age III office", !office.start("megamachines:great_dam"));
        check("site: office never goes back an Age", office.setTier("V") && !office.setTier("III"));
        check("site: unknown id refused", !office.start("megamachines:nothing"));
        check("site: aqueduct starts at Age V office", office.start("megamachines:aqueduct_arcade"));
        check("site: second start refused while running", !office.start("megamachines:aqueduct_arcade"));

        var intake = level.getCapability(Capabilities.Item.BLOCK, dp, null);
        check("site: depot exposes an item handler", intake != null);
        if (intake != null) {
            try (Transaction tx = Transaction.openRoot()) { intake.insert(ItemResource.of(Items.STONE_BRICKS), 5, tx); }   // not committed
            check("site: uncommitted delivery rolls back", depot.ledger.get("c:stone/cut") == 0);
            try (Transaction tx = Transaction.openRoot()) { check("site: bricks accepted", intake.insert(ItemResource.of(Items.STONE_BRICKS), 5, tx) == 5); tx.commit(); }
            check("site: 5 pallets = 5 t of c:stone/cut", depot.ledger.get("c:stone/cut") == 5.0);
            try (Transaction tx = Transaction.openRoot()) { check("site: dirt refused", intake.insert(ItemResource.of(Items.DIRT), 5, tx) == 0); }
        }

        // in-world: fill the depot, run ticks, stage 1 (piers, 1400 blocks) must complete and consume 2700 t
        depot.ledger.set(new java.util.HashMap<>(java.util.Map.of("c:stone/cut", 2430.0, "c:timber/beam", 270.0)));
        office.setBuilders(1);
        office.setPaid(false);
        for (int i = 0; i < 20; i++) SiteOfficeBlockEntity.tick(level, op, level.getBlockState(op), office);
        check("site: unpaid crew strikes, nothing placed", office.placed() == 0 && office.statusName().equals("unpaid"));
        office.setPaid(true);
        for (int i = 0; i < 400 && office.engine().stageIndex() == 0; i++) { SiteOfficeBlockEntity.tick(level, op, level.getBlockState(op), office); }
        check("site: stage 1 finished (stage " + office.engine().stageIndex() + ", done " + office.engine().done() + ", status " + office.statusName() + ", ledger " + depot.ledger.view() + ")", office.engine().stageIndex() == 1);
        check("site: depot drained by stage 1", depot.ledger.get("c:stone/cut") < 1e-3 && depot.ledger.get("c:timber/beam") < 1e-3);
        int bricks = 0;
        for (int x = 0; x <= 30; x++) for (int z = -14; z <= 14; z++) for (int y = 201; y <= 206; y++)
            if (level.getBlockState(new BlockPos(x + 1, y, z + 1)).is(Blocks.STONE_BRICKS)) bricks++;
        check("site: 1400 blocks placed in the world (" + bricks + ")", bricks == 1400);
        for (int i = 0; i < 100; i++) SiteOfficeBlockEntity.tick(level, op, level.getBlockState(op), office);
        check("site: empty depot pauses stage 2", office.statusName().equals("no_material") && office.engine().stageIndex() == 1);

        // pure engine against the generator: total fleet hours must match blueprints.json (S6917)
        for (var e : java.util.Map.of("megamachines:aqueduct_arcade", 153.3, "megamachines:ship_lock_flight", 651.5, "megamachines:great_dam", 770.7,
                "megamachines:arcology_tower", 863.8, "megamachines:space_elevator", 444.0, "megamachines:orbital_ring", 412.6, "megamachines:dyson_swarm", 347.2).entrySet()) {
            var st = Blueprints.get(e.getKey());
            var eng = new StageEngine(st, 0, 0, 64);
            Ledger rich = new Ledger();
            for (String t : Blueprints.allTags()) rich.add(t, 1e12);
            double seconds = 0;
            for (int i = 0; i < 4_000_000 && !eng.finished(); i++, seconds += 30) eng.tick(30, true, rich);
            check("engine: " + e.getKey() + " builds in " + Math.round(seconds / 3600) + " h (generator " + e.getValue() + " h)",
                    eng.finished() && Math.abs(seconds / 3600 - e.getValue()) / e.getValue() < 0.02);
        }
        var dam = new StageEngine(Blueprints.get("megamachines:great_dam"), 0, 0, 64);
        Ledger rich = new Ledger();
        for (String t : Blueprints.allTags()) rich.add(t, 1e12);
        check("engine: no builders, no work", new StageEngine(Blueprints.get("megamachines:great_dam"), 0, 0, 0).tick(30, true, rich) == StageEngine.Block.NO_BUILDERS);
        check("engine: dam makes no power before the turbine stage", dam.watts() == 0);
        while (dam.stageIndex() < 5) dam.tick(600, true, rich);
        double w0 = dam.watts();
        while (!dam.finished()) dam.tick(600, true, rich);
        check("engine: dam power rises through the turbine stage, 500 MW when done", w0 == 0 && Math.abs(dam.watts() - 500e6) < 1);
    }

    private void check(String name, boolean ok) {
        if (!ok) fails++;
        LOG.info("[selftest] {} {}", ok ? "PASS" : "FAIL", name);
    }

    private void finish(ServerLevel level) {
        LOG.info("[selftest] DONE fails={}", fails);
        level.getServer().halt(false);
    }
}
