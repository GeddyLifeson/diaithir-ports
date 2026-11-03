package org.diaithir.mega;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
        finish(level);
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
