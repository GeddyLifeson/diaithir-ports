package net.diaithir.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Server-side combat: arc hit detection, combo state, stamina, parry and dodge. Everything here runs on the logical server. */
public final class Engine {
    /** Per player state. Not persisted. */
    public static final class State {
        public float stamina = -1;
        public long lastSpend, lastSwing = -100000, parryStart = -1000, dodgeUntil = -1000, dodgeReady;
        public int combo, lastCooldown, lastWindow;
        public int parryWindow;
        boolean wasGuarding, wasSneaking;
        float lastShown = -1;
    }

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    public static State state(Player p) {
        State s = STATES.computeIfAbsent(p.getUUID(), k -> new State());
        if (s.stamina < 0) s.stamina = (float) CombatConfig.STAMINA_MAX.getAsDouble();
        return s;
    }

    public static void forget(Player p) { STATES.remove(p.getUUID()); }

    static long now(Player p) { return p.level().getGameTime(); }

    // ---- Age ----
    public static int ageOf(Player p) {
        int age = CombatConfig.DEFAULT_AGE.getAsInt();
        try {
            var sb = p.level().getScoreboard();
            var obj = sb.getObjective(CombatConfig.AGE_OBJECTIVE.get());
            if (obj != null) {
                var info = sb.getPlayerScoreInfo(p, obj);
                if (info != null && info.value() > 0) age = info.value();
            }
        } catch (RuntimeException ignored) { }
        return Mth.clamp(age, 1, 9);
    }

    public static float ageSpeed(int age) {
        double f = 1.0 + CombatConfig.AGE_SPEED_PER_AGE.getAsDouble() * (age - 1);
        return (float) Math.min(f, CombatConfig.AGE_SPEED_CAP.getAsDouble());
    }

    public static int cooldownTicks(Cat.Step st, int age) {
        return Math.max(2, Math.round(st.cooldown() / ageSpeed(age)));
    }

    // ---- stamina ----
    public static void tick(ServerPlayer p) {
        State s = state(p);
        long t = now(p);
        float max = (float) CombatConfig.STAMINA_MAX.getAsDouble();
        if (s.stamina < max && t - s.lastSpend > CombatConfig.STAMINA_DELAY.getAsInt()) {
            s.stamina = Math.min(max, s.stamina + (float) CombatConfig.STAMINA_REGEN.getAsDouble());
        }
        // parry window bookkeeping
        boolean guard = p.isUsingItem() && p.getUseItem().has(DataComponents.BLOCKS_ATTACKS);
        if (guard && !s.wasGuarding) { s.parryStart = t; s.parryWindow = CombatConfig.SHIELD_PARRY_WINDOW.getAsInt(); }
        s.wasGuarding = guard;
        boolean sneak = p.isShiftKeyDown();
        if (sneak && !s.wasSneaking && !guard) {
            Cat c = Cat.melee(p.getMainHandItem());
            if (c != null && c.parryWindow > 0) { s.parryStart = t; s.parryWindow = c.parryWindow; }
        }
        s.wasSneaking = sneak;
        if (CombatConfig.STAMINA_ON.getAsBoolean() && s.stamina < max - 0.01f && t % 4 == 0 && Math.abs(s.stamina - s.lastShown) > 0.5f) {
            s.lastShown = s.stamina;
            int n = Math.round(10 * s.stamina / max);
            p.sendSystemMessage(Component.translatable("diaithircombat.stamina").append(": " + "|".repeat(n) + ".".repeat(10 - n)), true);
        }
    }

    /** Spends stamina; returns false when there was not enough (stamina drops to zero). */
    static boolean spend(Player p, float cost) {
        if (!CombatConfig.STAMINA_ON.getAsBoolean() || cost <= 0) return true;
        State s = state(p);
        cost *= (float) CombatConfig.STAMINA_COST_MULT.getAsDouble();
        s.lastSpend = now(p);
        if (s.stamina >= cost) { s.stamina -= cost; return true; }
        s.stamina = 0;
        return false;
    }

    // ---- hit detection ----
    public static float baseDamage(Player p, ItemStack stack) {
        if (p instanceof net.neoforged.neoforge.common.util.FakePlayer) {
            double[] d = {p.getAttributeBaseValue(Attributes.ATTACK_DAMAGE)};
            stack.forEachModifier(EquipmentSlot.MAINHAND, (attr, mod) -> {
                if (attr.is(Attributes.ATTACK_DAMAGE) && mod.operation() == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE) d[0] += mod.amount();
            });
            return (float) d[0];
        }
        return (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    static double distToBox(AABB b, Vec3 q) {
        double dx = Math.max(Math.max(b.minX - q.x, 0), q.x - b.maxX);
        double dy = Math.max(Math.max(b.minY - q.y, 0), q.y - b.maxY);
        double dz = Math.max(Math.max(b.minZ - q.z, 0), q.z - b.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    static Vec3 nearest(AABB b, Vec3 q) {
        return new Vec3(Mth.clamp(q.x, b.minX, b.maxX), Mth.clamp(q.y, b.minY, b.maxY), Mth.clamp(q.z, b.minZ, b.maxZ));
    }

    public static float stepRange(Cat c, Cat.Step st) { return c.range * st.rangeMult() * (float) CombatConfig.RANGE_MULT.getAsDouble(); }
    public static float stepArc(Cat c, Cat.Step st) { return Math.min(360f, c.arc * st.arcMult()); }

    /** Living entities this swing would hit, nearest first, at most max_targets. */
    public static List<LivingEntity> findTargets(ServerPlayer p, Cat c, Cat.Step st) {
        ServerLevel level = p.level();
        float range = stepRange(c, st);
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        flat = flat.lengthSqr() < 1e-6 ? new Vec3(0, 0, 1) : flat.normalize();
        boolean thrust = st.pattern().equals("thrust") || st.pattern().equals("stab");
        double halfArc = Math.toRadians(stepArc(c, st) / 2.0);
        List<double[]> scored = new ArrayList<>();
        List<LivingEntity> cand = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(range + 1.5))) {
            if (e == p || !e.isAlive() || e.isSpectator() || e.isPassengerOfSameVehicle(p) || !p.canAttack(e)) continue;
            AABB box = e.getBoundingBox();
            double dist;
            if (thrust) {
                Vec3 to = box.getCenter().subtract(eye);
                double t = Mth.clamp(to.dot(look), 0, range);
                Vec3 q = eye.add(look.scale(t));
                if (distToBox(box, q) > c.width) continue;
                dist = distToBox(box, eye);
            } else {
                Vec3 near = nearest(box, eye);
                dist = near.distanceTo(eye);
                if (dist > range) continue;
                Vec3 h = new Vec3(near.x - eye.x, 0, near.z - eye.z);
                if (h.lengthSqr() > 1e-4) {
                    double ang = Math.acos(Mth.clamp(flat.dot(h.normalize()), -1, 1));
                    if (ang > halfArc) continue;
                }
            }
            if (dist > range) continue;
            Vec3 pt = nearest(box, eye);
            if (level.clip(new ClipContext(eye, pt, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p)).getType() != HitResult.Type.MISS
                    && dist > 0.6) {
                // something solid in the way; allow when the block is beyond the target
                Vec3 hit = level.clip(new ClipContext(eye, pt, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p)).getLocation();
                if (hit.distanceTo(eye) < dist - 0.05) continue;
            }
            scored.add(new double[]{dist, cand.size()});
            cand.add(e);
        }
        scored.sort(Comparator.comparingDouble(a -> a[0]));
        List<LivingEntity> out = new ArrayList<>();
        for (double[] a : scored) {
            if (out.size() >= c.maxTargets) break;
            out.add(cand.get((int) a[1]));
        }
        return out;
    }

    public record Result(boolean swung, int step, List<LivingEntity> hits, float damage, boolean exhausted) {}
    static final Result NO = new Result(false, -1, List.of(), 0, false);

    /** A swing request from player p (client packet or vanilla attack). */
    public static Result swing(ServerPlayer p) { return swing(p, false); }

    public static Result swing(ServerPlayer p, boolean ignoreCooldown) {
        if (!CombatConfig.ENABLED.getAsBoolean()) return NO;
        ItemStack stack = p.getMainHandItem();
        Cat c = Cat.melee(stack);
        if (c == null || c.combo.isEmpty() || p.isSpectator()) return NO;
        State s = state(p);
        long t = now(p);
        int age = ageOf(p);
        long elapsed = t - s.lastSwing;
        if (!ignoreCooldown && elapsed < s.lastCooldown) return NO;
        int idx = (elapsed <= s.lastCooldown + s.lastWindow && s.combo < c.combo.size()) ? s.combo : 0;
        Cat.Step st = c.combo.get(idx);
        boolean ok = spend(p, st.stamina());
        float mult = st.damage() * (float) CombatConfig.DAMAGE_MULT.getAsDouble() * (ok ? 1f : (float) CombatConfig.EXHAUSTED_DAMAGE.getAsDouble());
        s.lastSwing = t;
        s.lastCooldown = cooldownTicks(st, age);
        s.lastWindow = Math.round(c.comboWindow / ageSpeed(age));
        s.combo = (idx + 1) % c.combo.size();
        p.swing(InteractionHand.MAIN_HAND, true);
        ServerLevel level = p.level();
        List<LivingEntity> hits = findTargets(p, c, st);
        float base = baseDamage(p, stack);
        float total = 0;
        double yaw = Math.toRadians(p.getYRot());
        for (LivingEntity e : hits) {
            DamageSource src = level.damageSources().playerAttack(p);
            float dmg = base * mult;
            dmg = EnchantmentHelper.modifyDamage(level, stack, e, src, dmg);
            e.invulnerableTime = 0;
            if (e.hurtServer(level, src, dmg)) {
                total += dmg;
                e.knockback(0.4 * c.knockback * st.kbMult() * 2.5 + 0.0, Math.sin(yaw), -Math.cos(yaw));
            }
        }
        if (!hits.isEmpty() && !stack.isEmpty()) stack.hurtAndBreak(1, p, EquipmentSlot.MAINHAND);
        effects(p, c, st, level, !hits.isEmpty());
        return new Result(true, idx, hits, total, !ok);
    }

    private static void effects(ServerPlayer p, Cat c, Cat.Step st, ServerLevel level, boolean hit) {
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        float range = stepRange(c, st);
        boolean thrust = st.pattern().equals("thrust") || st.pattern().equals("stab");
        if (thrust) {
            for (int i = 1; i <= 3; i++) {
                Vec3 q = eye.add(look.scale(range * i / 3.0));
                level.sendParticles(ParticleTypes.CRIT, q.x, q.y - 0.2, q.z, 1, 0, 0, 0, 0);
            }
        } else {
            Vec3 q = eye.add(look.scale(Math.min(range, 2.2) * 0.8));
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, q.x, q.y - 0.3, q.z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, p.getX(), p.getY(), p.getZ(), hit ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1f, 1f);
    }

    // ---- parry ----
    /** True when victim's guard was raised within its parry window and the hit comes from the front. */
    public static boolean parryCheck(Player victim, DamageSource src) {
        if (!CombatConfig.PARRY_ON.getAsBoolean() || src.is(DamageTypeTags.BYPASSES_SHIELD)) return false;
        State s = state(victim);
        long t = now(victim);
        if (s.parryWindow <= 0 || t - s.parryStart > s.parryWindow || t < s.parryStart) return false;
        Vec3 from = src.getSourcePosition();
        if (from == null) return false;
        Vec3 look = victim.getLookAngle();
        Vec3 to = from.subtract(victim.position());
        Vec3 h = new Vec3(to.x, 0, to.z);
        if (h.lengthSqr() < 1e-6) return true;
        Vec3 lh = new Vec3(look.x, 0, look.z).normalize();
        return Math.acos(Mth.clamp(lh.dot(h.normalize()), -1, 1)) <= Math.toRadians(100);
    }

    /** Applies the parry reward: attacker stagger, swing reset for the defender. */
    public static void parried(Player victim, DamageSource src) {
        State s = state(victim);
        s.lastSwing = -1000;
        s.combo = 0;
        s.parryStart = -1000;
        ServerLevel level = (ServerLevel) victim.level();
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.2f, 1.6f);
        level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getEyeY() - 0.4, victim.getZ(), 6, 0.3, 0.2, 0.3, 0.2);
        if (src.getEntity() instanceof LivingEntity a) {
            a.knockback(0.7, victim.getX() - a.getX(), victim.getZ() - a.getZ());
            int stun = CombatConfig.PARRY_STUN.getAsInt();
            if (stun > 0) a.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, stun, 2));
        }
    }

    public static boolean dodging(Player p, DamageSource src) {
        if (src.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        return now(p) <= state(p).dodgeUntil;
    }

    // ---- dodge ----
    /** dir: 0 forward, 1 back, 2 left, 3 right. */
    public static boolean dodge(ServerPlayer p, int dir, boolean force) {
        if (!CombatConfig.DODGE_ON.getAsBoolean() || p.isSpectator()) return false;
        State s = state(p);
        long t = now(p);
        if (t < s.dodgeReady) return false;
        if (!force && (!p.onGround() || p.isPassenger() || p.isSwimming())) return false;
        if (!spend(p, (float) CombatConfig.DODGE_COST.getAsDouble())) return false;
        double yaw = Math.toRadians(p.getYRot());
        Vec3 fwd = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 right = new Vec3(-fwd.z, 0, fwd.x);
        Vec3 v = switch (dir) { case 1 -> fwd.scale(-1); case 2 -> right; case 3 -> right.scale(-1); default -> fwd; };
        double str = CombatConfig.DODGE_STRENGTH.getAsDouble() * (1 + 0.02 * (ageOf(p) - 1));
        p.setDeltaMovement(v.x * str, 0.25, v.z * str);
        p.hurtMarked = true;
        p.fallDistance = 0;
        s.dodgeUntil = t + CombatConfig.DODGE_IFRAMES.getAsInt();
        s.dodgeReady = t + CombatConfig.DODGE_COOLDOWN.getAsInt();
        ServerLevel level = p.level();
        level.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.1, p.getZ(), 6, 0.25, 0.05, 0.25, 0.02);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 0.8f, 1.4f);
        return true;
    }

    // ---- ranged ----
    public static int drawTicks(Cat c, int age) {
        double t = c.drawTicks * CombatConfig.DRAW_TIME_MULT.getAsDouble() / Math.max(1.0, 1 + 0.5 * (ageSpeed(age) - 1));
        return Math.max(4, (int) Math.round(t));
    }

    /** Charge handed to the vanilla bow code, scaled so a full draw happens after drawTicks. */
    public static int adjustCharge(Cat c, int age, int charge) {
        return Math.round(charge * 20f / drawTicks(c, age));
    }

    public static float dropReduction(Cat c) {
        return Mth.clamp(c.dropReduction * (float) CombatConfig.DROP_MULT.getAsDouble(), 0f, 1f);
    }

    private Engine() {}
}
