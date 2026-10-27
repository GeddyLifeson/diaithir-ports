package net.diaithir.voiceinteraction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Turns reported speech into a game event for sculk and wardens, and into small reactions from creatures. */
public final class Noticing {
    private static final Map<UUID, Hearing.Throttle> THROTTLES = new ConcurrentHashMap<>();
    private static volatile Map<String, Hearing.Reaction> rules;
    public static long noticed;

    private Noticing() {}

    public static void speech(ServerPlayer p, float levelDb, boolean whisper, boolean group) {
        if (p == null || p.isSpectator() || p.isRemoved()) return;
        if (whisper && !InteractionConfig.WHISPER_COUNTS.get()) return;
        if (group && !InteractionConfig.GROUP_COUNTS.get()) return;
        if (InteractionConfig.SNEAK_QUIETENS.get() && p.isShiftKeyDown()) return;
        double threshold = InteractionConfig.THRESHOLD_DB.get();
        double radius = Hearing.radius(levelDb, whisper, threshold, InteractionConfig.LOUD_DB.get(), InteractionConfig.NOTICE_RADIUS.get());
        if (radius <= 0) return;
        p.level().getServer().execute(() -> {
            if (p.isRemoved()) return;
            if (!THROTTLES.computeIfAbsent(p.getUUID(), k -> new Hearing.Throttle()).allow(p.level().getGameTime(), InteractionConfig.INTERVAL_TICKS.get())) return;
            if (noticed++ % 200 == 0) DiaithirVoiceInteraction.LOG.info("{} was heard at {} dB (radius {} blocks), {} times so far", p.getName().getString(), Math.round(levelDb), Math.round(radius), noticed);
            if (InteractionConfig.VIBRATIONS.get()) {
                p.level().gameEvent(p, DiaithirVoiceInteraction.SPEECH, p.position());
            }
            if (InteractionConfig.CREATURES.get()) react((ServerLevel) p.level(), p, radius);
        });
    }

    private static Map<String, Hearing.Reaction> rules() {
        Map<String, Hearing.Reaction> r = rules;
        if (r == null) {
            r = new HashMap<>();
            for (String line : InteractionConfig.RULES.get()) {
                var e = Hearing.parseRule(line);
                if (e != null) r.put(e.getKey(), e.getValue());
            }
            rules = r;
        }
        return r;
    }

    public static void reloadRules() {
        rules = null;
    }

    private static void react(ServerLevel level, ServerPlayer p, double radius) {
        Vec3 at = p.position();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(at, at).inflate(radius), m -> m.isAlive() && m.distanceToSqr(at) <= radius * radius)) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
            Hearing.Reaction r = rules().getOrDefault(id.toString(), Hearing.Reaction.NONE);
            switch (r) {
                case LOOK -> mob.getLookControl().setLookAt(p, 30f, 30f);
                case FLEE -> {
                    Vec3 away = mob.position().subtract(at).multiply(1, 0, 1);
                    if (away.lengthSqr() < 1e-4) away = new Vec3(1, 0, 0);
                    Vec3 dest = mob.position().add(away.normalize().scale(6));
                    mob.getNavigation().moveTo(dest.x, dest.y, dest.z, 1.25);
                }
                case NONE -> { }
            }
        }
    }

    public static void forget(UUID id) {
        THROTTLES.remove(id);
    }
}
