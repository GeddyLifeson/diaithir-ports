package org.diaithir.mega;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.diaithir.mega.cleanroom.DrillHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dev-only headless check (-Dmegamachines.selftest=true) for the quarry, laser drill, gantry assembler and track maths. */
public final class MegaBlocksSelfTest {
    private static final Logger LOG = LoggerFactory.getLogger("Megamachines");
    private int fails;

    @SubscribeEvent
    public void started(ServerStartedEvent e) {
        ServerLevel level = e.getServer().overworld();
        level.setChunkForced(8, 8, true);
        BlockPos base = new BlockPos(128, 220, 128);
        for (int x = -9; x <= 9; x++) for (int z = -9; z <= 9; z++) for (int y = -20; y <= 6; y++)
            level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);

        // quarry: stone floor under it, iron head cannot cut rock, carbide can; output stored, not spilled
        BlockPos q = base;
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) for (int y = -3; y <= -1; y++)
            level.setBlock(q.offset(x, y, z), Blocks.STONE.defaultBlockState(), 2);
        level.setBlock(q, MegaBlocks.QUARRY.get().defaultBlockState(), 2);
        var quarry = (QuarryBlockEntity) level.getBlockEntity(q);
        check("quarry block entity exists", quarry != null);
        if (quarry != null) {
            quarry.setSpec("megamachines:bucket_wheel_excavator");
            quarry.serverTick();
            check("quarry without head: NO_HEAD", quarry.status() == QuarryBlockEntity.Status.NO_HEAD);
            quarry.installHead(DrillHead.IRON);
            for (int i = 0; i < 100; i++) quarry.serverTick();
            check("iron head cannot cut rock", quarry.status() == QuarryBlockEntity.Status.HEAD_TOO_SOFT && quarry.dug() == 0);
            check("stone untouched", level.getBlockState(q.offset(0, -1, 0)).is(Blocks.STONE));
        }
        BlockPos q2 = base.offset(0, 0, 7);
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) for (int y = -3; y <= -1; y++)
            level.setBlock(q2.offset(x, y, z), Blocks.STONE.defaultBlockState(), 2);
        level.setBlock(q2, MegaBlocks.QUARRY.get().defaultBlockState(), 2);       // fresh entity with a carbide head
        var quarry2 = (QuarryBlockEntity) level.getBlockEntity(q2);
        quarry2.setSpec("megamachines:bucket_wheel_excavator");
        quarry2.installHead(DrillHead.CARBIDE);
        for (int i = 0; i < 400; i++) quarry2.serverTick();
        check("carbide head digs rock cells", quarry2.dug() > 0 && level.getBlockState(q2.offset(0, -1, 0)).isAir());
        check("dug blocks are stored, not spilled", quarry2.storedCount() > 0 && level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(q2).inflate(10)).isEmpty());

        // laser drill: depth band and lenses
        BlockPos l = base.offset(6, 0, 0);
        level.setBlock(l, MegaBlocks.LASER_DRILL.get().defaultBlockState(), 2);
        var laser = (LaserDrillBlockEntity) level.getBlockEntity(l);
        check("laser block entity exists", laser != null);
        if (laser != null) {
            for (int i = 0; i < 600; i++) laser.serverTick();
            check("laser at y=220 is above every band: no output", laser.storedCount() == 0);
            laser.setDepthOverride(10);
            for (int i = 0; i < 2000; i++) laser.serverTick();
            check("laser at depth 10 produces ore", laser.storedCount() > 0);
            check("dye lens accepted as catalyst", LaserDrillOres.isCatalyst("minecraft:light_blue_dye") && laser.addLens("minecraft:light_blue_dye"));
        }

        // gantry: 3 shafts east, carriage east, payload; assembler on the west end
        BlockPos g = base.offset(-6, 0, 0);
        BlockState shaft = MegaBlocks.GANTRY_SHAFT.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST);
        BlockState carr = MegaBlocks.GANTRY_CARRIAGE.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST);
        level.setBlock(g, MegaBlocks.GANTRY_ASSEMBLER.get().defaultBlockState(), 2);
        for (int i = 1; i <= 3; i++) level.setBlock(g.offset(i, 0, 0), shaft, 2);
        level.setBlock(g.offset(4, 0, 0), carr, 2);
        level.setBlock(g.offset(5, 0, 0), Blocks.IRON_BLOCK.defaultBlockState(), 2);
        int moved = GantryBlocks.assemble(level, g);
        check("gantry assembly collects 3 shafts + carriage + payload", moved == 5);
        check("gantry blocks left the world for the ship", level.getBlockState(g.offset(2, 0, 0)).isAir());
        check("assembler block stays behind", level.getBlockState(g).is(MegaBlocks.GANTRY_ASSEMBLER.get()));

        // tracks
        var t = MegaBlocks.trackDrive(MegaRates.get("megamachines:crawler_transporter"), 100);
        check("tracks: pressure and speed are finite", t.groundPressureKPa() > 0 && t.topSpeed() > 0);
        LOG.info("[selftest] DONE (blocks) fails={}", fails);
    }

    private void check(String name, boolean ok) {
        if (!ok) fails++;
        LOG.info("[selftest] {} {}", ok ? "PASS" : "FAIL", name);
    }
}
