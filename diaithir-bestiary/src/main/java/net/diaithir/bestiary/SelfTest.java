package net.diaithir.bestiary;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * /bestiary selftest: spawns every creature of this mod's own datapack on a forced patch of the overworld, lets them live for
 * two seconds, then checks that each ticked and takes damage, that riders (a fake player) can mount and steer mechs and flyers,
 * the avatar's boss bar and second phase, the stalker's gaze rule, loot and spawn rules. Prints PASS or the failures.
 */
final class SelfTest {
    private static final Logger LOG = LogUtils.getLogger();
    private static final int CX = 3000, CZ = 3000;
    private static @Nullable Run run;

    private SelfTest() {}

    /** A fake player that, unlike NeoForge's FakePlayer, may mount: it is never added to the level and its connection goes nowhere. */
    private static final class Rider extends ServerPlayer {
        Rider(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), "bestiary_selftest"), ClientInformation.createDefault());
            new ServerGamePacketListenerImpl(level.getServer(), new net.minecraft.network.Connection(PacketFlow.SERVERBOUND), this,
                    CommonListenerCookie.createInitial(getGameProfile(), false));
        }
    }

    private static final class Run {
        final CommandSourceStack source;
        final ServerLevel level;
        final ServerPlayer fake;
        final Map<Identifier, Creature> creatures = new LinkedHashMap<>();
        final List<String> fails = new ArrayList<>();
        final int start;
        int stage;

        Run(CommandSourceStack source, ServerLevel level, ServerPlayer fake, int start) {
            this.source = source;
            this.level = level;
            this.fake = fake;
            this.start = start;
        }
    }

    static void start(CommandSourceStack source) {
        if (run != null) {
            source.sendFailure(Component.literal("bestiary selftest is already running"));
            return;
        }
        MinecraftServer server = source.getServer();
        ServerLevel level = server.overworld();
        forceChunks(level, true);
        ServerPlayer fake = new Rider(level);
        Run r = new Run(source, level, fake, server.getTickCount());
        int i = 0;
        for (var entry : DiaithirBestiary.creatures(level).entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (!id.getNamespace().equals(DiaithirBestiary.ID)) continue;
            Creature c = DiaithirBestiary.spawn(level, id, surface(level, CX + 8 * i++, CZ), EntitySpawnReason.COMMAND);
            check(r, c != null, id + " spawns");
            if (c == null) continue;
            r.creatures.put(id, c);
            CreatureDef def = entry.getValue();
            check(r, c.def() == def && c.getType() == DiaithirBestiary.typeOf(def.preset()), id + " has its kind and preset type");
            check(r, Math.abs(c.getBbWidth() - def.width()) < 1e-3 && Math.abs(c.getBbHeight() - def.height()) < 1e-3, id + " has its size");
            check(r, c.getMaxHealth() == def.stats().health() && c.getHealth() == c.getMaxHealth(), id + " has its health");
            def.spawn().ifPresent(s -> {
                var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME).getTagOrEmpty(s.biomes()).iterator();
                Holder<Biome> biome = biomes.hasNext() ? biomes.next() : null;
                check(r, biome != null && Spawner.candidates(DiaithirBestiary.creatures(level), biome, 0, false).stream().anyMatch(e -> e.getKey().equals(id)),
                        id + " is a spawn candidate in its biome tag");
            });
        }
        check(r, r.creatures.size() >= 5, "at least five example creatures load (found " + r.creatures.size() + ")");
        run = r;
        source.sendSuccess(() -> Component.literal("bestiary selftest running with " + r.creatures.size() + " creatures"), false);
    }

    static void tick(MinecraftServer server) {
        Run r = run;
        if (r == null) return;
        int age = server.getTickCount() - r.start;
        if (r.stage == 0 && age >= 40) {
            r.stage = 1;
            r.creatures.forEach((id, c) -> firstChecks(r, id, c));
        } else if (r.stage == 1 && age >= 60) {
            r.creatures.forEach((id, c) -> lastChecks(r, id, c));
            finish(r);
        }
    }

    private static void firstChecks(Run r, Identifier id, Creature c) {
        check(r, c.isAlive() && c.tickCount >= 35, id + " ticks (" + c.tickCount + " ticks)");
        float before = c.getHealth();
        c.invulnerableTime = 0;
        c.hurtServer(r.level, r.level.damageSources().playerAttack(r.fake), 2F);
        check(r, c.getHealth() < before, id + " takes damage");
        if (c.def().isFlying()) check(r, c.isNoGravity() && c.getNavigation().getClass().getSimpleName().contains("Flying"), id + " flies");
        if (c.def().isRideable()) rideChecks(r, id, c);
        if (c instanceof Avatar a) {
            check(r, a.bossEvent().isVisible() && a.phase() == 1, id + " has a boss bar and starts in phase one");
            a.invulnerableTime = 0;
            a.hurtServer(r.level, r.level.damageSources().playerAttack(r.fake), a.getHealth() - a.getMaxHealth() * 0.3F);
        }
        if (c instanceof Stalker s) {
            Vec3 eye = s.position().add(0, s.getBbHeight() * 0.5, 0);
            r.fake.snapTo(s.getX() + 8, s.getY(), s.getZ(), 90F, 0F);
            r.fake.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, eye);
            boolean looking = s.isWatchedBy(r.fake);
            r.fake.setYHeadRot(r.fake.getYHeadRot() + 180F);
            check(r, looking && !s.isWatchedBy(r.fake), id + " knows when it is watched");
        }
    }

    private static void rideChecks(Run r, Identifier id, Creature c) {
        ServerPlayer fake = r.fake;
        fake.snapTo(c.getX() + 1, c.getY(), c.getZ(), 0F, 0F);
        fake.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        if (!(c instanceof Mech)) {
            c.interact(fake, InteractionHand.MAIN_HAND, Vec3.ZERO);
            check(r, fake.getVehicle() == null, id + " refuses a stranger");
            c.tame(fake);
        }
        c.interact(fake, InteractionHand.MAIN_HAND, Vec3.ZERO);
        check(r, fake.getVehicle() == c && c.getControllingPassenger() == fake, id + " takes a fake player as pilot");
        fake.zza = 1F;
        if (c instanceof Mech m && m.needsFuel()) {
            check(r, m.getRiddenInput(fake, Vec3.ZERO).lengthSqr() == 0, id + " will not move without fuel");
            fake.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(m.def().fuel().get().item()));
            m.interact(fake, InteractionHand.MAIN_HAND, Vec3.ZERO);
            check(r, m.fuel() > 0, id + " takes fuel");
        }
        check(r, c.getRiddenInput(fake, Vec3.ZERO).z > 0 && c.getRiddenSpeed(fake) > 0, id + " is steered by its pilot");
        fake.zza = 0F;
        fake.stopRiding();
    }

    private static void lastChecks(Run r, Identifier id, Creature c) {
        if (c instanceof Avatar a) check(r, a.phase() == 2, id + " enters phase two");
        if (c instanceof Mech m && m.needsFuel()) check(r, !m.isVehicle() && m.fuel() > 0, id + " keeps its fuel when empty");
        if (c.def().lootTable().isPresent()) {
            c.invulnerableTime = 0;
            c.hurtServer(r.level, r.level.damageSources().playerAttack(r.fake), Float.MAX_VALUE);
            check(r, !c.isAlive() && !r.level.getEntitiesOfClass(ItemEntity.class, c.getBoundingBox().inflate(3)).isEmpty(), id + " drops its loot");
        }
    }

    private static void finish(Run r) {
        r.creatures.values().forEach(c -> c.discard());
        r.level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(CX - 8, r.level.getMinY(), CZ - 8, CX + 64, r.level.getMaxY(), CZ + 8)).forEach(ItemEntity::discard);
        forceChunks(r.level, false);
        run = null;
        String result = r.fails.isEmpty() ? "bestiary selftest PASS (" + r.creatures.size() + " creatures)" : "bestiary selftest FAIL: " + String.join("; ", r.fails);
        LOG.info(result);
        if (r.fails.isEmpty()) r.source.sendSuccess(() -> Component.literal(result), false);
        else r.source.sendFailure(Component.literal(result));
    }

    private static void forceChunks(ServerLevel level, boolean on) {
        for (int cx = (CX >> 4) - 1; cx <= (CX + 64 >> 4) + 1; cx++)
            for (int cz = (CZ >> 4) - 1; cz <= (CZ >> 4) + 1; cz++) level.setChunkForced(cx, cz, on);
    }

    private static Vec3 surface(ServerLevel level, int x, int z) {
        return Vec3.atBottomCenterOf(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)));
    }

    private static void check(Run r, boolean ok, String what) {
        if (!ok) r.fails.add(what);
    }
}
