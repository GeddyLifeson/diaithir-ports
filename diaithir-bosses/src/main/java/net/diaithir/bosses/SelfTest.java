package net.diaithir.bosses;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * /bossesselftest: for every boss that can be spawned, spawns it on a stone platform with a fake player, runs every attack of its
 * profile through telegraph and fire, forces each phase change, checks Age scaling, Age-gated loot, arena lock-in, the bar, the
 * no-despawn flag and a kill with drops. Then checks the elite system on a zombie, a skeleton and a creeper.
 */
public final class SelfTest {
    private static final List<String> FAILS = new ArrayList<>();
    private static int ok;
    private static final Set<String> TYPES = Set.of("slam", "charge", "leap", "bolt", "summon", "lightning", "cloud", "wave", "heal", "roar");

    private static void check(boolean c, String what) {
        if (c) ok++;
        else {
            FAILS.add(what);
            System.out.println("[bossesselftest] FAIL " + what);
        }
    }

    private static CommandSourceStack pending;
    private static int wait;
    private static boolean listening;

    /**
     * Entry point of the command. Entities only join a chunk once it is entity-ticking, which needs a few ticks after loading when no
     * player is nearby (as on a console or rcon run), so the arena chunks are forced first and the run follows two seconds later.
     */
    static int start(CommandSourceStack src) {
        ServerLevel level = src.getServer().overworld();
        for (int cx = -3; cx <= 2; cx++) for (int cz = -3; cz <= 2; cz++) { level.getChunk(cx, cz); level.setChunkForced(cx, cz, true); }
        if (!listening) {
            listening = true;
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.tick.ServerTickEvent.Post.class, e -> {
                if (pending != null && --wait <= 0) {
                    CommandSourceStack s = pending;
                    pending = null;
                    try { run(s); } finally {
                        ServerLevel lv = s.getServer().overworld();
                        for (int cx = -3; cx <= 2; cx++) for (int cz = -3; cz <= 2; cz++) lv.setChunkForced(cx, cz, false);
                    }
                }
            });
        }
        pending = src;
        wait = 60;
        src.sendSuccess(() -> Component.literal("bosses self test starts in 3 seconds; the total goes to the log"), false);
        return 1;
    }

    static int run(CommandSourceStack src) {
        FAILS.clear();
        ok = 0;
        ServerLevel level = src.getServer().overworld();
        BlockPos c0 = new BlockPos(0, 230, 0);
        for (int x = -34; x <= 34; x++) {
            for (int z = -34; z <= 34; z++) {
                level.getChunk(x >> 4, z >> 4);
                level.setBlock(c0.offset(x, 0, z), Blocks.STONE.defaultBlockState(), 2);
                for (int h = 1; h <= 14; h++) level.setBlock(c0.offset(x, h, z), Blocks.AIR.defaultBlockState(), 2);
            }
        }
        FakePlayer fp = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("00000000-0000-4000-8000-00000000b055"), "[boss-test]"));
        fp.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100000);
        fp.setHealth(100000);
        Integer oldForce = Ages.forced;
        int bosses = 0, skipped = 0;
        try {
            for (Data.Profile p : Data.profiles().values()) {
                check(!p.phases.isEmpty() && p.phases.get(0).at >= 0.999, p.id + ": first phase starts at full health");
                for (Data.Phase ph : p.phases) for (String a : ph.attacks) check(p.attacks.containsKey(a), p.id + ": phase uses known attack " + a);
                for (Data.Attack a : p.attacks.values()) check(TYPES.contains(a.type), p.id + ": attack type " + a.type);
                check(DiaithirBosses.TROPHIES.containsKey(p.id), p.id + ": has a trophy");
                check(p.age >= 1 && p.age <= 7, p.id + ": Age in range");
            }
            for (Data.Profile p : Data.profiles().values()) {
                var type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(p.entity));
                if (type.isEmpty()) {
                    skipped++;
                    System.out.println("[bossesselftest] skip " + p.id + " (" + p.entity + " not installed)");
                    continue;
                }
                bosses++;
                runBoss(level, c0, fp, p);
            }
            attach(level, c0, fp);
            altar(level, c0, fp);
            elites(level, c0);
        } finally {
            Ages.forced = oldForce;
            fp.setPos(0, 300, 0);
        }
        String msg = "bosses tested=" + bosses + " skipped=" + skipped + " ok=" + ok + " bad=" + FAILS.size();
        System.out.println("[bossesselftest] " + msg);
        src.sendSuccess(() -> Component.literal(msg), false);
        return FAILS.isEmpty() ? 1 : 0;
    }

    private static Mob spawnAt(ServerLevel level, Data.Profile p, BlockPos c0) {
        return DiaithirBosses.spawn(level, p, Vec3.atBottomCenterOf(c0.above()), c0);
    }

    private static void runBoss(ServerLevel level, BlockPos c0, FakePlayer fp, Data.Profile p) {
        String n = p.id;
        Ages.forced = p.age;
        fp.setPos(c0.getX() + 7.5, c0.getY() + 1, c0.getZ() + 0.5);
        Mob m = spawnAt(level, p, c0);
        check(m != null, n + ": spawns");
        if (m == null) return;
        BossController c = BossController.ALL.get(m.getUUID());
        check(c != null, n + ": controller attached");
        if (c == null) {
            m.discard();
            return;
        }
        c.testPlayers.add(fp);
        try {
            for (int i = 0; i < 6; i++) c.tick();
            check(c.bar.getPlayers().contains(fp), n + ": boss bar shows to the fighter");
            check(m.isPersistenceRequired(), n + ": does not despawn");
            check(m.entityTags().contains(BossController.TAG), n + ": tagged");
            for (String a : p.attacks.keySet()) {
                fp.setHealth(fp.getMaxHealth());
                fp.setPos(c0.getX() + 5.5, c0.getY() + 1, c0.getZ() + 0.5);
                c.invuln = 0;
                m.setInvulnerable(false);
                c.cooldown = 0;
                c.beginAttack(a, List.of(fp));
                check(c.current != null && c.telegraph > 0, n + ": " + a + " telegraphs first");
                int guard = 0;
                while (c.current != null && guard++ < 600) c.tick();
                check(c.current == null, n + ": " + a + " completes");
                check(c.fired.getOrDefault(p.attacks.get(a).type, 0) > 0, n + ": " + a + " fired");
            }
            for (int i = 1; i < p.phases.size(); i++) {
                m.setHealth(Math.max(1, m.getMaxHealth() * (float) (p.phases.get(i).at - 0.02)));
                c.cooldown = 99999;
                c.tick();
                check(c.phase == i, n + ": enters phase " + i);
                check(m.entityTags().contains(BossController.T_PHASE + i), n + ": phase " + i + " persists in a tag");
                if (p.phases.get(i).invuln > 0) check(m.isInvulnerable(), n + ": phase " + i + " is invulnerable during the change");
                int g = 0;
                while (c.invuln > 0 && g++ < 400) c.tick();
                check(!m.isInvulnerable(), n + ": phase " + i + " ends its shield");
            }
            fp.setPos(c0.getX() + p.arena.radius + 6, c0.getY() + 1, c0.getZ() + 0.5);
            fp.hurtMarked = false;
            float before = fp.getHealth();
            c.cooldown = 99999;
            for (int i = 0; i < 20; i++) c.tick();
            check(fp.hurtMarked || fp.getHealth() < before, n + ": arena pushes a fighter who leaves");
            check(p.max_hit_fraction > 0 && p.max_hit_fraction < 1, n + ": hit cap sane");
            List<ItemStack> low = c.rollLoot(1), high = c.rollLoot(7);
            check(high.stream().anyMatch(s -> s.getItem() == DiaithirBosses.TROPHIES.get(n).get()), n + ": drops its trophy");
            check(p.loot.stream().noneMatch(l -> l.min_age > 1) || low.size() <= high.size(), n + ": loot grows with the Age");
            m.setInvulnerable(false);
            AABB box = m.getBoundingBox().inflate(6);
            m.kill(level);
            check(c.dead, n + ": death hook ran");
            check(!level.getEntitiesOfClass(ItemEntity.class, box).isEmpty(), n + ": drops items on death");
            level.getEntitiesOfClass(ItemEntity.class, new AABB(c0).inflate(40)).forEach(ItemEntity::discard);
        } catch (RuntimeException e) {
            e.printStackTrace();
            check(false, n + ": exception " + e);
        }
        if (m.isAlive()) m.discard();
        Ages.forced = p.age;
        Mob a = spawnAt(level, p, c0);
        Ages.forced = Math.min(8, p.age + 3);
        Mob b = spawnAt(level, p, c0);
        if (a != null && b != null) {
            float ea = a.getMaxHealth() * BossController.ALL.get(a.getUUID()).damageDivisor, eb = b.getMaxHealth() * BossController.ALL.get(b.getUUID()).damageDivisor;
            check(eb > ea, n + ": effective health scales with the Age (" + ea + " -> " + eb + "; hp " + a.getMaxHealth() + "/" + b.getMaxHealth() + " div " + BossController.ALL.get(a.getUUID()).damageDivisor + "/" + BossController.ALL.get(b.getUUID()).damageDivisor + " near " + BossController.ALL.get(a.getUUID()).participants().size() + ")");
        }
        if (a != null) a.discard();
        if (b != null) b.discard();
        level.getEntitiesOfClass(Mob.class, new AABB(c0).inflate(60), e -> e.entityTags().stream().anyMatch(t -> t.startsWith(BossController.T_MINION))).forEach(Mob::discard);
    }

    /** The altar: Age gate, waking, no double wake, and the respawn rule after a kill. */
    private static void altar(ServerLevel level, BlockPos c0, FakePlayer fp) {
        BlockPos pos = c0.offset(-20, 1, 0);
        level.setBlock(pos, DiaithirBosses.ALTAR.get().defaultBlockState(), 3);
        var be = (AltarBlockEntity) level.getBlockEntity(pos);
        check(be != null, "altar: block entity exists");
        if (be == null) return;
        for (String id : new String[] {"tribal_chief", "ceramic_warlord", "stone_colossus"}) {
            Data.Profile p = Data.byId(id);
            be.boss = id;
            be.defeatedAt = -1;
            be.defeats = 0;
            Ages.forced = Math.max(1, p.age - 1);
            if (p.age > 1) check(be.refusal(level, p) != null, "altar " + id + ": sleeps below its Age");
            Ages.forced = p.age;
            check(be.refusal(level, p) == null, "altar " + id + ": ready at its Age");
            be.activate(fp);
            var found = level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(10), m -> m.isAlive() && m.entityTags().contains(AltarBlockEntity.posTag(pos)));
            check(found.size() == 1, "altar " + id + ": wakes the boss");
            check(be.refusal(level, p) != null, "altar " + id + ": will not wake a second one");
            if (!found.isEmpty()) {
                found.get(0).kill(level);
                check(be.defeats == 1, "altar " + id + ": remembers the defeat");
                check(be.refusal(level, p) != null, "altar " + id + ": rests after the fight (" + p.respawn.mode + ")");
                level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(20)).forEach(ItemEntity::discard);
            }
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    /** A profile can turn another mod's creature into a boss: here a vanilla husk stands in for the diaithir_myth entities. */
    private static void attach(ServerLevel level, BlockPos c0, FakePlayer fp) {
        Data.Profile src = Data.byId("tribal_chief");
        Data.Profile p = Data.GSON.fromJson(Data.GSON.toJson(src), Data.Profile.class);
        p.id = "attach_test";
        p.entity = "minecraft:husk";
        p.own.hp = 300;
        Data.attach(p);
        try {
            Ages.forced = 1;
            Mob m = make(level, EntityType.HUSK, c0);
            level.addFreshEntity(m);
            BossController c = BossController.ALL.get(m.getUUID());
            check(c != null, "attach: a profile adopts another mod's entity");
            if (c != null) {
                check(m.getMaxHealth() >= 300, "attach: health lifted to the profile (" + m.getMaxHealth() + ")");
                check(m.isPersistenceRequired(), "attach: attached boss does not despawn");
                c.testPlayers.add(fp);
                for (int i = 0; i < 6; i++) c.tick();
                check(c.bar.getPlayers().contains(fp), "attach: bar shows");
                m.discard();
                check(!BossController.ALL.containsKey(m.getUUID()), "attach: controller released on removal");
            }
        } finally {
            Data.detach("attach_test");
        }
    }

    private static Mob make(ServerLevel level, EntityType<?> t, BlockPos c0) {
        Mob m = (Mob) t.create(level, EntitySpawnReason.NATURAL);
        m.snapTo(c0.getX() + 20.5, c0.getY() + 1, c0.getZ() + 0.5, 0, 0);
        return m;
    }

    private static void elites(ServerLevel level, BlockPos c0) {
        Ages.forced = 7;
        RandomSource r = RandomSource.create();
        for (String id : new String[] {"minecraft:zombie", "minecraft:skeleton", "minecraft:creeper"}) {
            Mob m = make(level, BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(id)).orElseThrow(), c0);
            float base = m.getMaxHealth();
            List<String> got = Elites.make(m, 7, r, null);
            check(Elites.isElite(m), id + ": becomes a champion");
            check(got.size() == 3, id + ": Age VII champion has three affixes");
            check(m.hasCustomName(), id + ": champion is named");
            check(m.getMaxHealth() >= base, id + ": champion is no weaker");
            level.addFreshEntity(m);
            m.discard();
        }
        Mob z = make(level, EntityType.ZOMBIE, c0);
        float base = z.getMaxHealth();
        Elites.make(z, 1, r, List.of("hulking", "shielded"));
        check(z.getMaxHealth() > base * 1.5F, "hulking raises health (" + base + " -> " + z.getMaxHealth() + ")");
        check(z.getAbsorptionAmount() > 0, "shielded gives absorption");
        z.discard();
        int n = 0;
        for (int i = 0; i < 2000; i++) if (Elites.roll(make(level, EntityType.ZOMBIE, c0), 7, r)) n++;
        check(n > 100 && n < 700, "elite roll rate at Age VII is about the table's (" + n + "/2000)");
        n = 0;
        for (int i = 0; i < 2000; i++) if (Elites.roll(make(level, EntityType.ZOMBIE, c0), 1, r)) n++;
        check(n < 200, "elite rolls are rarer at Age I (" + n + "/2000)");
    }

    private SelfTest() {}
}
