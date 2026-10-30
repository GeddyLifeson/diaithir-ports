package net.diaithir.forcefield;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Places a projector high above spawn, powers it through the energy capability and checks the shell, owners, teams and intruders. */
final class FieldSelfTest {
    private FieldSelfTest() {}

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        // clear the test volume so terrain cannot make the shell count differ
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-12, -12, -12), at.offset(12, 12, 12))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);

        FakePlayer owner = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("ff-owner".getBytes()), "ff_owner"));
        FakePlayer stranger = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("ff-stranger".getBytes()), "ff_stranger"));
        FakePlayer mate = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("ff-mate".getBytes()), "ff_mate"));
        PlayerTeam team = server.getScoreboard().addPlayerTeam("ff_selftest_team");
        server.getScoreboard().addPlayerToTeam("ff_owner", team);
        server.getScoreboard().addPlayerToTeam("ff_mate", team);
        Zombie zombie = null;
        try {
            level.setBlock(at, DiaithirForcefield.PROJECTOR.get().defaultBlockState().setValue(ProjectorBlock.FACING, Direction.NORTH), 3);
            if (!(level.getBlockEntity(at) instanceof ProjectorBlockEntity be)) return List.of("projector has no block entity");
            be.setOwner(owner);

            java.util.List<net.minecraft.world.entity.LivingEntity> all = new ArrayList<>(List.of(owner, stranger, mate));

            // power through the energy capability
            var cap = level.getCapability(Capabilities.Energy.BLOCK, at, null);
            if (cap == null) return List.of("no energy capability on the projector");
            int in;
            try (Transaction tx = Transaction.openRoot()) {
                in = cap.insert(1000000, tx);
                tx.commit();
            }
            if (in <= 0) fails.add("capability accepted no FE");
            int fe0 = be.energy.getAmountAsInt();

            // shell exists (cube, base range)
            be.setShape(Geometry.Shape.CUBE);
            for (int i = 0; i < 40 && !be.built(); i++) be.pass(level);
            int r = be.radius();
            if (!be.built()) fails.add("shell did not finish");
            if (!level.getBlockState(at.offset(r, 0, 0)).is(DiaithirForcefield.FIELD.get())) fails.add("no shell block at +radius");
            if (!level.getBlockState(at.offset(0, -r, 0)).is(DiaithirForcefield.FIELD.get())) fails.add("no shell block under");
            if (level.getBlockState(at.offset(r - 1, 0, 0)).is(DiaithirForcefield.FIELD.get())) fails.add("interior block is field");
            int counted = 0;
            int[] off = Geometry.shell(Geometry.Shape.CUBE, r, be.thickness(), 0, -1);
            for (int i = 0; i < off.length; i += 3) if (level.getBlockState(at.offset(off[i], off[i + 1], off[i + 2])).is(DiaithirForcefield.FIELD.get())) counted++;
            if (counted != off.length / 3) fails.add("shell has " + counted + " of " + off.length / 3 + " blocks");
            if (be.energy.getAmountAsInt() >= fe0) fails.add("no upkeep drawn");

            // the stranger standing in the shell is held out; owner and team mate are not
            double ex = at.getX() + 0.5 + r;
            place(stranger, ex, at.getY(), at.getZ() + 0.5);
            place(owner, ex, at.getY(), at.getZ() + 0.5);
            place(mate, ex, at.getY(), at.getZ() + 0.5);
            be.pass(level, all);
            if (stranger.getX() < at.getX() + 0.5 + r + 1.0) fails.add("stranger not held out, x=" + stranger.getX());
            if (Math.abs(owner.getX() - ex) > 0.01) fails.add("owner was moved");
            if (Math.abs(mate.getX() - ex) > 0.01) fails.add("team mate was moved");

            // team access off: mate is held out
            be.setTeamAccess(false);
            place(mate, ex, at.getY(), at.getZ() + 0.5);
            be.pass(level, all);
            if (mate.getX() < ex + 0.5) fails.add("team mate not held out with team access off");

            // guest list module and a guest
            be.install(Module.GUEST_LIST);
            be.addGuest(stranger.getUUID(), "ff_stranger");
            place(stranger, ex, at.getY(), at.getZ() + 0.5);
            be.pass(level, all);
            if (Math.abs(stranger.getX() - ex) > 0.01) fails.add("guest was moved");

            // a hostile mob is held out
            zombie = EntityType.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
            zombie.setPos(ex, at.getY(), at.getZ() + 0.5);
            all.add(zombie); // not added to the level: entity lookup is vanilla, the test exercises the field's own rules
            be.pass(level, all);
            if (zombie.getX() < at.getX() + 0.5 + r + 1.0) fails.add("zombie not held out, x=" + zombie.getX());

            // mob filter lets players through (mate is on no list now)
            be.install(Module.MOB_FILTER);
            place(mate, ex, at.getY(), at.getZ() + 0.5);
            be.pass(level, all);
            if (Math.abs(mate.getX() - ex) > 0.01) fails.add("mob filter still held out a player");
            zombie.setPos(ex, at.getY(), at.getZ() + 0.5);
            be.pass(level, all);
            if (zombie.getX() < at.getX() + 0.5 + r + 1.0) fails.add("mob filter let the zombie in");

            // range and strength modules change the shell
            int before = be.shellBlocks();
            be.install(Module.RANGE);
            be.install(Module.STRENGTH);
            for (int i = 0; i < 60 && !(be.built() && be.radius() > r); i++) be.pass(level, all);
            if (be.radius() <= r) fails.add("range module did not grow the shell");
            if (be.thickness() != 2) fails.add("strength module did not thicken the shell");
            if (be.shellBlocks() <= before) fails.add("shell did not grow");
            if (level.getBlockState(at.offset(r, 0, 0)).is(DiaithirForcefield.FIELD.get()) && be.radius() > r + 1)
                fails.add("old shell block left behind");

            // sting module hurts
            be.install(Module.STING);
            double f = be.radius();
            be.setTeamAccess(false);
            zombie.setPos(at.getX() + 0.5 + f, at.getY(), at.getZ() + 0.5);
            float hp = zombie.getHealth();
            be.pass(level, all);
            if (!(zombie.getHealth() < hp)) fails.add("sting module did no damage");

            // wall shape
            be.setShape(Geometry.Shape.WALL);
            for (int i = 0; i < 10; i++) be.pass(level, all);
            if (!level.getBlockState(at.offset(0, 0, -Geometry.WALL_GAP)).is(DiaithirForcefield.FIELD.get())) fails.add("no wall block in front of projector");
            if (level.getBlockState(at.offset(be.radius(), 0, 0)).is(DiaithirForcefield.FIELD.get())) fails.add("cube block left after switching to wall");

            // out of power: the field collapses
            be.energy.set(0);
            for (int i = 0; i < 60; i++) be.pass(level);
            if (level.getBlockState(at.offset(0, 0, -Geometry.WALL_GAP)).is(DiaithirForcefield.FIELD.get())) fails.add("field stayed up with no power");

            // breaking the projector leaves no field behind
            be.energy.set(be.energy.getCapacityAsInt());
            be.setShape(Geometry.Shape.SPHERE);
            for (int i = 0; i < 10; i++) be.pass(level, all);
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
            int left = 0;
            for (BlockPos p : BlockPos.betweenClosed(at.offset(-20, -20, -20), at.offset(20, 20, 20))) if (level.getBlockState(p).is(DiaithirForcefield.FIELD.get())) left++;
            if (left > 0) fails.add(left + " field blocks left after the projector was removed");
        } catch (Throwable t) {
            fails.add("exception " + t);
            t.printStackTrace();
        } finally {
            if (zombie != null) zombie.discard();
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
            server.getScoreboard().removePlayerTeam(team);
        }
        return fails;
    }

    private static void place(FakePlayer p, double x, double y, double z) {
        p.snapTo(x, y, z);
    }
}
