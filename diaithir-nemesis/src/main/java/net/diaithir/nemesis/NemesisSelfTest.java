package net.diaithir.nemesis;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * /nemesis selftest: the whole loop on a headless server with no players. A fake player stands in for the victim; its death
 * event is posted by hand because fake players never die. Returns the failed checks; empty means PASS.
 */
final class NemesisSelfTest {
    private NemesisSelfTest() {}

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.overworld();
        NemesisData data = NemesisData.get(server);
        if (!data.nemeses.isEmpty()) return List.of("this world already has nemeses; run the self test on a test world");
        FakePlayer victim = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "nemesis_selftest"));
        BlockPos at = dryLand(level);
        victim.snapTo(at.getX(), at.getY(), at.getZ(), 0F, 0F);
        victim.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        data.houses.put(victim.getStringUUID(), "Selftest House");

        Mob zombie = EntityType.ZOMBIE.spawn(level, at.east(2), EntitySpawnReason.COMMAND);
        double baseHealth = zombie.getAttributeBaseValue(Attributes.MAX_HEALTH);

        NeoForge.EVENT_BUS.post(new LivingDeathEvent(victim, level.damageSources().mobAttack(zombie)));
        Nemesis n = DiaithirNemesis.find(data, zombie);
        check(fails, n != null && data.nemeses.size() == 1, "kill promotes the zombie");
        if (n == null) return fails;
        check(fails, zombie.hasCustomName() && zombie.isPersistenceRequired(), "nemesis is named and kept");
        check(fails, zombie.getMaxHealth() > baseHealth, "rank raises max health");
        check(fails, n.houses.contains("Selftest House"), "grudge holds the House");
        check(fails, data.shaken.get(victim.getStringUUID()) == n.id && !n.seen.isEmpty(), "victim is Shaken and the nemesis was seen");
        check(fails, victim.getMainHandItem().isEmpty() && zombie.getOffhandItem().is(Items.STONE_AXE), "nemesis takes the weapon");
        var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        NemesisData copy = NemesisData.CODEC.parse(ops, NemesisData.CODEC.encodeStart(ops, data).getOrThrow()).getOrThrow();
        check(fails, copy.byId(n.id) != null && copy.trophy(copy.byId(n.id)).is(Items.STONE_AXE), "save keeps the nemesis and its trophy");

        NeoForge.EVENT_BUS.post(new LivingDeathEvent(victim, level.damageSources().mobAttack(zombie)));
        check(fails, n.rank == 2 && n.kills == 2, "second kill ranks up");

        for (int round = 0; round < 2 && data.byId(n.id) != null; round++) {
            String was = n.entity;
            check(fails, DiaithirNemesis.ambush(server, data, n, victim), "ambush " + round + " spawns");
            check(fails, !n.entity.equals(was) && DiaithirNemesis.loaded(server, n) instanceof Mob, "ambush " + round + " carries the nemesis");
            check(fails, zombie.isRemoved() || round > 0, "old body is removed on ambush");
            check(fails, DiaithirNemesis.loaded(server, n) instanceof Mob m && m.getOffhandItem().is(Items.STONE_AXE), "ambush " + round + " carries the trophy");
            Entity body = DiaithirNemesis.loaded(server, n);
            if (body instanceof Mob m) m.hurtServer(level, level.damageSources().playerAttack(victim), 100_000F);
        }
        check(fails, data.byId(n.id) == null, "nemesis falls at the latest on its second death");
        check(fails, !data.shaken.containsKey(victim.getStringUUID()), "its fall frees the Shaken");

        NeoForge.EVENT_BUS.post(new LivingDeathEvent(victim, level.damageSources().mobAttack(EntityType.SKELETON.spawn(level, at.west(2), EntitySpawnReason.COMMAND))));
        Nemesis sire = data.nemeses.getLast();
        String oldAge = data.age;
        int heirs = DiaithirNemesis.succession(server, "II");
        Nemesis heir = data.nemeses.getLast();
        check(fails, data.trophies.isEmpty() || !data.trophy(heir).isEmpty(), "trophy passes to the heir");
        check(fails, data.shaken.get(victim.getStringUUID()) == heir.id, "the heir keeps them Shaken");
        check(fails, heirs == 1 && heir.sire.equals(sire.fullName()) && heir.generation == sire.generation + 1
                && heir.players.equals(sire.players) && "II".equals(data.age), "Age Turn passes the grudge to an heir");

        Mob ravager = EntityType.RAVAGER.spawn(level, at.north(3), EntitySpawnReason.COMMAND);
        for (int i = 0; i < 3; i++) {
            ravager.invulnerableTime = 0;
            ravager.hurtServer(level, level.damageSources().playerAttack(victim), 7F);
        }
        Nemesis bearer = DiaithirNemesis.find(data, ravager);
        check(fails, bearer != null && bearer.kills == 0 && ravager.isAlive(), "a grudge without a kill makes a nemesis");

        int told = data.chronicle.size();
        boolean clashed = false;
        for (int i = 0; i < 8 && !clashed; i++) {
            Nemesis rival = DiaithirNemesis.RULES.promote(data.nextId++, "minecraft:zombie", data.age, victim.getStringUUID(), "", new java.util.Random(i));
            data.nemeses.add(rival);
            clashed = DiaithirNemesis.feud(server, data);
        }
        check(fails, clashed && data.chronicle.size() > told && (data.chronicle.getLast().contains(" beat ")
                || data.chronicle.getLast().contains(" now leads ") || data.chronicle.getLast().contains(" is dead.")), "warbands feud and the chronicle records it");
        Nemesis any = data.nemeses.getFirst();
        check(fails, DiaithirNemesis.interrogate(data, any).contains(any.warband), "interrogation names the warband");

        data.nemeses.clear();
        data.trophies.clear();
        data.shaken.clear();
        data.unshake.remove(victim.getStringUUID());
        data.chronicle.clear();
        data.age = oldAge;
        data.houses.remove(victim.getStringUUID());
        data.setDirty();
        return fails;
    }

    // ambushes need dry ground past the safe radius; on the Earth map a fixed spot can be open sea
    private static BlockPos dryLand(ServerLevel level) {
        BlockPos spawn = level.getRespawnData().pos();
        for (int deg = 0; deg < 360; deg += 30) {
            double a = Math.toRadians(deg);
            BlockPos at = surface(level, spawn.getX() + (int) (1000 * Math.cos(a)), spawn.getZ() + (int) (1000 * Math.sin(a)));
            if (level.getFluidState(at.below()).isEmpty()) return at;
        }
        return surface(level, spawn.getX() + 1000, spawn.getZ());
    }

    // Loads the chunks an ambush can land in, as a real player's view distance would.
    private static BlockPos surface(ServerLevel level, int x, int z) {
        for (int cx = -2; cx <= 2; cx++) for (int cz = -2; cz <= 2; cz++) level.getChunk((x >> 4) + cx, (z >> 4) + cz);
        return new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
    }

    private static void check(List<String> fails, boolean ok, String what) {
        if (!ok) fails.add(what);
    }
}
