package org.diaithir.archimedes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Dev-only headless check (-Darchimedes.selftest=true): builds a boat, floats it, drives it, disassembles it. */
public final class ArchimedesSelfTest {
    private ServerLevel level;
    private ShipEntity ship;
    private int tick;
    private double startX, startZ;
    private BlockPos origin;
    private int fails;
    private net.minecraft.world.entity.decoration.ArmorStand rider;
    private net.minecraft.world.phys.Vec3 riderLocal;

    @SubscribeEvent
    public void started(ServerStartedEvent e) {
        level = e.getServer().overworld();
        origin = new BlockPos(0, 64, 0);
        for (int cx = -1; cx <= 1; cx++) for (int cz = -1; cz <= 1; cz++) level.setChunkForced(cx, cz, true);
        // water pool 20x20, 6 deep, on stone
        for (int x = -10; x < 10; x++) for (int z = -10; z < 10; z++) {
            for (int y = 58; y < 70; y++) level.setBlock(new BlockPos(x, y, z), y < 58 ? Blocks.STONE.defaultBlockState() : (y < 64 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState()), 2);
            level.setBlock(new BlockPos(x, 57, z), Blocks.STONE.defaultBlockState(), 2);
        }
        // boat 5x1x3: planks hull with floaters, helm, crate, engine
        for (int x = 0; x < 5; x++) for (int z = 0; z < 3; z++) level.setBlock(origin.offset(x, -1, z), Blocks.OAK_PLANKS.defaultBlockState(), 2);
        level.setBlock(origin.offset(1, -1, 1), ArchimedesMod.FLOATER.get().defaultBlockState(), 2);
        level.setBlock(origin.offset(2, -1, 1), ArchimedesMod.FLOATER.get().defaultBlockState(), 2);
        level.setBlock(origin.offset(3, 0, 1), ArchimedesMod.ENGINE.get().defaultBlockState(), 2);
        level.setBlock(origin.offset(2, 0, 2), ArchimedesMod.CRATE.get().defaultBlockState(), 2);
        level.setBlock(origin.offset(1, 0, 1), ArchimedesMod.MARKER.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.EAST), 2);
        var r = ShipAssembler.assemble(level, origin.offset(1, 0, 1));
        check("assemble", r.code() == ShipAssembler.Code.OK);
        ship = r.ship();
        if (ship == null) { finish(); return; }
        check("blocks", ship.chunk().blocks.size() == 18);
        check("world cleared", level.getBlockState(origin.offset(1, 0, 1)).isAir());
        var boxes = DeckColliders.colliders(ship.chunk());
        check("deck colliders merge (" + boxes.size() + " boxes for " + ship.chunk().blocks.size() + " blocks)", !boxes.isEmpty() && boxes.size() < ship.chunk().blocks.size());
        check("floor under a plank cell is its top", Math.abs(DeckColliders.floorAt(ship.chunk(), 0.5, 0.5, 1.0, DeckWalk.STEP_UP) - 1.0) < 1e-6);
        check("no floor outside the hull", Double.isNaN(DeckColliders.floorAt(ship.chunk(), -1.5, 0.5, 1.0, DeckWalk.STEP_UP)));
        ship.testInput = new Input(false, false, false, false, false, false, false);
        startX = ship.getX(); startZ = ship.getZ();
        ArchimedesMod.LOG.info("[selftest] assembled {} blocks mass {} at {}", ship.chunk().blocks.size(), ship.chunk().mass, ship.position());
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post e) {
        if (ship == null) return;
        if (ship.isRemoved() && tick % 50 == 0) ArchimedesMod.LOG.info("[selftest] ship removed: {}", ship.getRemovalReason());
        if (ship.isRemoved()) { tick++; return; }
        tick++;
        if (tick == 100) {
            ArchimedesMod.LOG.info("[selftest] y after settle {}", ship.getY());
            check("floats (y>56.5 and above floor)", ship.getY() > 58 && ship.getY() < 66);
            ship.testInput = new Input(true, false, false, false, false, false, false);
            riderLocal = new net.minecraft.world.phys.Vec3(0.5, 1.0, 1.5);
            rider = new net.minecraft.world.entity.decoration.ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, level);
            rider.setNoGravity(true);
            rider.setPos(ship.toWorldPoint(riderLocal));
            level.addFreshEntity(rider);
        }
        if (tick == 124 && rider != null) {
            var now = ship.toLocalPoint(rider.position());
            ArchimedesMod.LOG.info("[selftest] deck rider local {} (started {}), world {}", now, riderLocal, rider.position());
            check("deck rider stayed on the deck while the ship moved", now.distanceTo(riderLocal) < 0.35 && Math.hypot(rider.getX() - startX, rider.getZ() - startZ) > 1.0);
            rider.discard();
        }
        if (tick == 125) {
            double d = Math.hypot(ship.getX() - startX, ship.getZ() - startZ);
            ArchimedesMod.LOG.info("[selftest] moved {} blocks", d);
            check("drives forward", d > 1.0);
            ship.testInput = null;
            ship.disassemble(level);
            ArchimedesMod.LOG.info("[selftest] disassembled; ship removed={}", ship.isRemoved());
            int n = 0;
            for (int x = -10; x < 10; x++) for (int z = -10; z < 10; z++) for (int y = 58; y < 72; y++) {
                var s = level.getBlockState(new BlockPos(x, y, z));
                if (s.is(ArchimedesMod.MARKER.get()) || s.is(ArchimedesMod.CRATE.get()) || s.is(ArchimedesMod.ENGINE.get())) n++;
            }
            check("blocks restored (marker+crate+engine = 3)", n == 3);
            finish();
        }
    }

    private void check(String name, boolean ok) {
        if (!ok) fails++;
        ArchimedesMod.LOG.info("[selftest] {} {}", ok ? "PASS" : "FAIL", name);
    }

    private void finish() {
        ArchimedesMod.LOG.info("[selftest] DONE fails={}", fails);
        ship = null;
        level.getServer().halt(false);
    }
}
