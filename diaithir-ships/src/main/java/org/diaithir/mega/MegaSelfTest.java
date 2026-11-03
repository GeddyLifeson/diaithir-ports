package org.diaithir.mega;

import net.minecraft.core.BlockPos;
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

        check("catalog has 12 vehicles", MegaCatalog.ids().size() == 12);
        physics();
        digHead(level);
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

    private void check(String name, boolean ok) {
        if (!ok) fails++;
        LOG.info("[selftest] {} {}", ok ? "PASS" : "FAIL", name);
    }

    private void finish(ServerLevel level) {
        LOG.info("[selftest] DONE fails={}", fails);
        level.getServer().halt(false);
    }
}
