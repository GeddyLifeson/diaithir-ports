package net.diaithir.bosses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The boss framework for one creature. It works on any Mob (our own bodies from BossMobs or another mod's entity that a profile
 * names), is driven from the entity tick event, and keeps its durable state in scoreboard tags so it survives a reload:
 * dbboss, dbscaled, dbphase:N, dbarena:x,y,z, dbaltar:x,y,z.
 * Per tick: boss bar, phase changes, arena lock-in and leash, then the attack machine (pick, telegraph, fire, cool down).
 */
public final class BossController {
    public static final Map<UUID, BossController> ALL = new ConcurrentHashMap<>();
    public static final String TAG = "dbboss", T_SCALED = "dbscaled", T_PHASE = "dbphase:", T_ARENA = "dbarena:", T_ALTAR = "dbaltar:", T_MINION = "dbminion:", T_DIV = "dbdiv:";
    private static final Identifier HP_MOD = id("scale_hp"), DMG_MOD = id("scale_damage"), SPD_PHASE = id("phase_speed"), DMG_PHASE = id("phase_damage");

    private static Identifier id(String p) { return Identifier.fromNamespaceAndPath(DiaithirBosses.ID, p); }

    public final Mob mob;
    public final ServerLevel level;
    public Data.Profile prof;
    public final ServerBossEvent bar;
    public int phase;
    public int invuln;
    public String current;
    public int telegraph, active, cooldown = 60, idle, ticks;
    public BlockPos arena;
    public boolean dead;
    public final List<ServerPlayer> testPlayers = new ArrayList<>();
    /** Number of attacks fired, by type; read by the self test. */
    public final Map<String, Integer> fired = new ConcurrentHashMap<>();
    private final RandomSource rnd = RandomSource.create();
    private Vec3 aim = Vec3.ZERO;
    private final Set<UUID> hit = new HashSet<>();
    private final List<Vec3> marks = new ArrayList<>();
    private final List<Zone> zones = new ArrayList<>();
    private int airborne;
    private String last = "";
    private double dmgScale = 1.0;
    /** Health beyond the game's attribute ceiling (1024) is paid for by dividing incoming damage. */
    public float damageDivisor = 1.0F;

    private record Zone(Vec3 pos, double r, int[] left, String effect, int ticks, int amp) {}

    public BossController(Mob mob, ServerLevel level, Data.Profile prof) {
        this.mob = mob;
        this.level = level;
        this.prof = prof;
        this.bar = new ServerBossEvent(mob.getUUID(), title(), color(prof.bar.color), overlay(prof.bar.overlay));
        this.bar.setDarkenScreen(false);
        init();
    }

    // ------------------------------------------------------------------ setup
    private Component title() {
        Component name = Component.translatable("boss." + DiaithirBosses.ID + "." + prof.id);
        return phase > 0 ? Component.translatable("boss." + DiaithirBosses.ID + ".phase", name, prof.phases.get(Math.min(phase, prof.phases.size() - 1)).name) : name;
    }

    private static BossEvent.BossBarColor color(String s) {
        try { return BossEvent.BossBarColor.valueOf(s.toUpperCase(java.util.Locale.ROOT)); } catch (Exception e) { return BossEvent.BossBarColor.RED; }
    }

    private static BossEvent.BossBarOverlay overlay(String s) {
        try { return BossEvent.BossBarOverlay.valueOf(s.toUpperCase(java.util.Locale.ROOT)); } catch (Exception e) { return BossEvent.BossBarOverlay.NOTCHED_10; }
    }

    private void init() {
        mob.addTag(TAG);
        mob.setPersistenceRequired();
        for (String t : mob.entityTags()) {
            if (t.startsWith(T_PHASE)) phase = Integer.parseInt(t.substring(T_PHASE.length()));
            else if (t.startsWith(T_ARENA)) arena = parse(t.substring(T_ARENA.length()));
            else if (t.startsWith(T_DIV)) damageDivisor = Float.parseFloat(t.substring(T_DIV.length()));
        }
        if (arena == null) {
            arena = mob.blockPosition();
            mob.addTag(T_ARENA + arena.getX() + "," + arena.getY() + "," + arena.getZ());
        }
        if (mob instanceof Zombie z && z.isBaby()) z.setBaby(false);
        if (!mob.entityTags().contains(T_SCALED)) {
            mob.addTag(T_SCALED);
            for (Map.Entry<String, String> e : prof.equipment.entrySet()) {
                EquipmentSlot slot = switch (e.getKey()) {
                    case "mainhand" -> EquipmentSlot.MAINHAND;
                    case "offhand" -> EquipmentSlot.OFFHAND;
                    case "head" -> EquipmentSlot.HEAD;
                    case "chest" -> EquipmentSlot.CHEST;
                    case "legs" -> EquipmentSlot.LEGS;
                    default -> EquipmentSlot.FEET;
                };
                BuiltInRegistries.ITEM.getOptional(Identifier.parse(e.getValue())).ifPresent(i -> mob.setItemSlot(slot, new ItemStack(i)));
            }
            scaleOnSpawn();
        }
        dmgScale = ageScale(false);
        applyPhaseMods();
        bar.setName(title());
    }

    private static BlockPos parse(String s) {
        String[] p = s.split(",");
        return new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
    }

    private int ageSteps() {
        return Math.clamp(Ages.current(level.getServer()) - prof.age, 0, prof.scaling.max_age_steps);
    }

    private double ageScale(boolean hp) {
        return 1.0 + (hp ? prof.scaling.hp_per_age : prof.scaling.damage_per_age) * ageSteps();
    }

    /** Scaling by Age and by the number of fighters present at the moment the boss appears. */
    private void scaleOnSpawn() {
        int near = Math.max(1, participants().size());
        double hpMul = ageScale(true) + prof.scaling.hp_per_extra_player * (near - 1);
        AttributeInstance hp = mob.getAttribute(Attributes.MAX_HEALTH);
        double base = hp != null ? hp.getBaseValue() : 1;
        if (hp != null && !prof.ownsEntity() && prof.own.hp > base) {
            // another mod's creature that a profile makes a boss of: lift it to the profile's strength
            base = prof.own.hp;
            hp.setBaseValue(base);
            AttributeInstance dm0 = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dm0 != null && dm0.getBaseValue() < prof.own.damage) dm0.setBaseValue(prof.own.damage);
        }
        if (hp != null) {
            hp.removeModifier(HP_MOD);
            hp.addPermanentModifier(new AttributeModifier(HP_MOD, hpMul - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        AttributeInstance dm = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dm != null) {
            dm.removeModifier(DMG_MOD);
            dm.addPermanentModifier(new AttributeModifier(DMG_MOD, ageScale(false) - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        mob.setHealth(mob.getMaxHealth());
        float want = (float) (base * hpMul);
        damageDivisor = Math.max(1.0F, want / mob.getMaxHealth());
        mob.addTag(T_DIV + damageDivisor);
    }

    private void applyPhaseMods() {
        Data.Phase ph = prof.phases.get(Math.min(phase, prof.phases.size() - 1));
        AttributeInstance sp = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (sp != null) {
            sp.removeModifier(SPD_PHASE);
            sp.addTransientModifier(new AttributeModifier(SPD_PHASE, ph.speed - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        AttributeInstance dm = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dm != null) {
            dm.removeModifier(DMG_PHASE);
            dm.addTransientModifier(new AttributeModifier(DMG_PHASE, ph.damage - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    // ------------------------------------------------------------------ people
    public List<ServerPlayer> participants() {
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer p : level.players()) {
            if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(mob) < 64 * 64) out.add(p);
        }
        for (ServerPlayer p : testPlayers) if (p.isAlive() && !out.contains(p)) out.add(p);
        return out;
    }

    private static boolean fights(ServerPlayer p) { return !p.isCreative() && !p.isSpectator(); }

    // ------------------------------------------------------------------ tick
    public void tick() {
        if (dead || !mob.isAlive()) return;
        ticks++;
        List<ServerPlayer> ps = participants();
        if (ticks % 5 == 0) refreshBar(ps);
        bar.setProgress(Math.clamp(mob.getHealth() / mob.getMaxHealth(), 0.0F, 1.0F));
        mob.setPersistenceRequired();
        tickZones();
        if (invuln > 0) {
            invuln--;
            if (ticks % 4 == 0) ring(ParticleTypes.END_ROD, mob.position(), 1.5 + mob.getBbWidth(), 12);
            if (invuln == 0) mob.setInvulnerable(false);
            arenaRules(ps);
            return;
        }
        checkPhase();
        if (ps.isEmpty()) {
            if (++idle > 200 && ticks % 20 == 0) mob.heal(mob.getMaxHealth() * 0.02F);
            current = null;
            telegraph = 0;
            return;
        }
        idle = 0;
        LivingEntity tgt = mob.getTarget();
        if (prof.hostile && (tgt == null || !tgt.isAlive() || !ps.contains(tgt))) {
            ServerPlayer best = null;
            double bd = 40 * 40;
            for (ServerPlayer p : ps) {
                if (!fights(p) && !testPlayers.contains(p)) continue;
                double d = p.distanceToSqr(mob);
                if (d < bd) { bd = d; best = p; }
            }
            if (best != null) mob.setTarget(best);
        }
        arenaRules(ps);
        attackMachine(ps);
    }

    private void refreshBar(List<ServerPlayer> ps) {
        for (ServerPlayer p : ps) bar.addPlayer(p);
        for (ServerPlayer p : new ArrayList<>(bar.getPlayers())) if (!ps.contains(p)) bar.removePlayer(p);
    }

    // ------------------------------------------------------------------ phases
    private void checkPhase() {
        float frac = mob.getHealth() / mob.getMaxHealth();
        int next = phase + 1;
        if (next < prof.phases.size() && frac <= prof.phases.get(next).at) enterPhase(next);
    }

    public void enterPhase(int idx) {
        phase = idx;
        mob.entityTags().removeIf(t -> t.startsWith(T_PHASE));
        mob.addTag(T_PHASE + idx);
        Data.Phase ph = prof.phases.get(idx);
        applyPhaseMods();
        bar.setName(title());
        current = null;
        telegraph = 0;
        cooldown = 40;
        if (ph.invuln > 0) {
            invuln = ph.invuln;
            mob.setInvulnerable(true);
        }
        ring(ParticleTypes.EXPLOSION, mob.position().add(0, 0.2, 0), 3.5, 14);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, mob.getX(), mob.getY() + 1, mob.getZ(), 1, 0, 0, 0, 0);
        sound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.7F);
        Component msg = Component.translatable("boss." + DiaithirBosses.ID + ".phase", Component.translatable("boss." + DiaithirBosses.ID + "." + prof.id), ph.name);
        for (ServerPlayer p : participants()) p.sendSystemMessage(msg);
    }

    // ------------------------------------------------------------------ arena
    private void arenaRules(List<ServerPlayer> ps) {
        if (ticks % 10 != 0) return;
        Vec3 c = Vec3.atBottomCenterOf(arena);
        double r = prof.arena.radius;
        if (prof.arena.leash) {
            double d = Math.hypot(mob.getX() - c.x, mob.getZ() - c.z);
            if (d > r + 4) {
                mob.teleportTo(c.x + (mob.getX() - c.x) * (r * 0.5 / d), mob.getY(), c.z + (mob.getZ() - c.z) * (r * 0.5 / d));
                mob.heal(mob.getMaxHealth() * 0.01F);
            } else if (d > r * 0.9 && mob.getTarget() == null) {
                mob.getNavigation().moveTo(c.x, c.y, c.z, 1.0);
            }
        }
        if (!prof.arena.lock) return;
        for (ServerPlayer p : ps) {
            if (!fights(p)) continue;
            double dx = p.getX() - c.x, dz = p.getZ() - c.z, d = Math.hypot(dx, dz);
            if (d > r + 0.5 && d < r + 40) {
                p.setDeltaMovement(-dx / d * 0.7, 0.25, -dz / d * 0.7);
                p.hurtMarked = true;
                if (ticks % 20 == 0 && prof.arena.burn > 0) p.hurtServer(level, level.damageSources().magic(), (float) prof.arena.burn);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 1, p.getZ(), 6, 0.4, 0.6, 0.4, 0.01);
            }
        }
        // a faint ring on the ground every 2 seconds shows where the wall stands
        if (ticks % 40 == 0) ring(ParticleTypes.SOUL_FIRE_FLAME, new Vec3(c.x, mob.getY() + 0.2, c.z), r, 48);
    }

    // ------------------------------------------------------------------ attacks
    private Data.Phase ph() { return prof.phases.get(Math.min(phase, prof.phases.size() - 1)); }

    private void attackMachine(List<ServerPlayer> ps) {
        if (current == null) {
            if (cooldown-- > 0) return;
            List<String> opts = ph().attacks;
            if (opts.isEmpty()) return;
            String pick = opts.get(rnd.nextInt(opts.size()));
            if (opts.size() > 1 && pick.equals(last)) pick = opts.get(rnd.nextInt(opts.size()));
            beginAttack(pick, ps);
            return;
        }
        Data.Attack a = prof.attacks.get(current);
        if (telegraph > 0) {
            telegraph--;
            showTelegraph(a);
            if (!"charge".equals(a.type) && !"leap".equals(a.type)) mob.getNavigation().stop();
            if (telegraph == 0) fire(a, ps);
            return;
        }
        // active phase of a charge or leap
        if ("charge".equals(a.type)) {
            if (active-- > 0) {
                Vec3 dv = mob.getDeltaMovement();
                mob.setDeltaMovement(aim.x * a.speed * 0.9, dv.y, aim.z * a.speed * 0.9);
                for (ServerPlayer p : ps) {
                    if (!hit.contains(p.getUUID()) && p.distanceToSqr(mob) < (a.radius + mob.getBbWidth()) * (a.radius + mob.getBbWidth())) {
                        hit.add(p.getUUID());
                        damage(p, a.damage);
                        p.knockback(1.6, -aim.x, -aim.z);
                    }
                }
                if (ticks % 2 == 0) level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, mob.getX(), mob.getY() + 0.2, mob.getZ(), 2, 0.3, 0.1, 0.3, 0.01);
                return;
            }
            finish(a);
        } else if ("leap".equals(a.type)) {
            airborne++;
            if ((airborne > 6 && mob.onGround()) || airborne > 50) {
                impact(a, ps, a.radius, a.damage, a.knockback);
                finish(a);
            }
        } else {
            finish(a);
        }
    }

    private void finish(Data.Attack a) {
        cooldown = (int) Math.max(10, a.cooldown * ph().cooldown);
        last = current;
        current = null;
        airborne = 0;
    }

    /** Starts an attack by name (also used by the self test to run every attack of a boss). */
    public void beginAttack(String name, List<ServerPlayer> ps) {
        Data.Attack a = prof.attacks.get(name);
        if (a == null) return;
        current = name;
        telegraph = Math.max(1, a.telegraph);
        hit.clear();
        marks.clear();
        LivingEntity t = mob.getTarget();
        Vec3 to = t != null ? t.position().subtract(mob.position()) : (ps.isEmpty() ? mob.getLookAngle() : ps.get(0).position().subtract(mob.position()));
        aim = new Vec3(to.x, 0, to.z).normalize();
        if ("lightning".equals(a.type)) {
            for (int i = 0; i < a.count; i++) {
                ServerPlayer p = ps.isEmpty() ? null : ps.get(i % ps.size());
                Vec3 base = p != null ? p.position() : mob.position();
                marks.add(base.add((rnd.nextDouble() - 0.5) * 5, 0, (rnd.nextDouble() - 0.5) * 5));
            }
        } else if ("summon".equals(a.type)) {
            for (int i = 0; i < a.count; i++) {
                double ang = rnd.nextDouble() * Math.PI * 2, r = 2 + rnd.nextDouble() * a.radius;
                marks.add(mob.position().add(Math.cos(ang) * r, 0, Math.sin(ang) * r));
            }
        }
        sound(SoundEvents.RAVAGER_ROAR, 1.6F, 1.0F);
        fired.merge(a.type + ":begin", 1, Integer::sum);
    }

    private void showTelegraph(Data.Attack a) {
        if (telegraph % 2 != 0) return;
        double k = 1.0 - (double) telegraph / Math.max(1, a.telegraph);
        Vec3 p = mob.position().add(0, 0.15, 0);
        switch (a.type) {
            case "slam", "wave", "leap", "roar", "cloud" -> {
                ring(ParticleTypes.CRIT, p, a.radius, (int) Math.min(64, a.radius * 8));
                ring(ParticleTypes.FLAME, p, a.radius * k, (int) Math.min(40, a.radius * 5));
            }
            case "charge" -> {
                for (int i = 1; i <= 10; i++) level.sendParticles(ParticleTypes.FLAME, p.x + aim.x * i * 1.2, p.y, p.z + aim.z * i * 1.2, 1, 0.1, 0, 0.1, 0);
            }
            case "lightning" -> {
                for (Vec3 m : marks) ring(ParticleTypes.ELECTRIC_SPARK, m.add(0, 0.15, 0), a.radius, 16);
            }
            case "summon" -> {
                for (Vec3 m : marks) level.sendParticles(ParticleTypes.PORTAL, m.x, m.y + 0.5, m.z, 8, 0.3, 0.6, 0.3, 0.2);
            }
            case "bolt" -> level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getEyeY(), mob.getZ(), 4, 0.4, 0.4, 0.4, 0.05);
            default -> level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 2, 0.5, 0.3, 0.5, 0.0);
        }
    }

    private double dmg(double base) { return base * ph().damage * dmgScale; }

    private void damage(LivingEntity t, double base) {
        t.hurtServer(level, level.damageSources().mobAttack(mob), (float) dmg(base));
    }

    private void applyEffect(LivingEntity t, String id, int ticks, int amp) {
        if (id == null) return;
        BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).ifPresent(h -> t.addEffect(new MobEffectInstance(h, ticks, amp)));
    }

    private void impact(Data.Attack a, List<ServerPlayer> ps, double r, double base, double kb) {
        for (ServerPlayer p : ps) {
            double dx = p.getX() - mob.getX(), dz = p.getZ() - mob.getZ();
            if (Math.hypot(dx, dz) <= r && Math.abs(p.getY() - mob.getY()) < 4) {
                damage(p, base);
                p.knockback(kb, -dx, -dz);
                p.hurtMarked = true;
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, mob.getX(), mob.getY() + 0.2, mob.getZ(), 3, r * 0.3, 0.1, r * 0.3, 0);
        ring(ParticleTypes.CAMPFIRE_COSY_SMOKE, mob.position().add(0, 0.1, 0), r, 24);
        sound(SoundEvents.GENERIC_EXPLODE.value(), 1.4F, 0.8F);
    }

    private void fire(Data.Attack a, List<ServerPlayer> ps) {
        fired.merge(a.type, 1, Integer::sum);
        switch (a.type) {
            case "slam", "wave" -> {
                impact(a, ps, a.radius, a.damage, a.knockback);
                finish(a);
            }
            case "charge" -> {
                active = a.active;
                hit.clear();
            }
            case "leap" -> {
                airborne = 0;
                mob.setDeltaMovement(aim.x * 0.9 * a.power, 0.95 * a.power, aim.z * 0.9 * a.power);
                mob.hurtMarked = true;
            }
            case "bolt" -> {
                bolts(a, ps);
                finish(a);
            }
            case "summon" -> {
                summon(a, ps);
                finish(a);
            }
            case "lightning" -> {
                for (Vec3 m : marks) {
                    LightningBolt lb = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                    if (lb != null) {
                        lb.snapTo(m.x, m.y, m.z);
                        lb.setVisualOnly(true);
                        level.addFreshEntity(lb);
                    }
                    for (ServerPlayer p : ps) {
                        if (p.position().distanceTo(m) <= a.radius) p.hurtServer(level, level.damageSources().lightningBolt(), (float) dmg(a.damage));
                    }
                }
                finish(a);
            }
            case "cloud" -> {
                zones.add(new Zone(mob.position(), a.radius, new int[] {a.duration}, a.effect, a.ticks, a.amp));
                finish(a);
            }
            case "heal" -> {
                mob.heal((float) (mob.getMaxHealth() * a.amount));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, mob.getX(), mob.getY() + 1, mob.getZ(), 30, 1, 1, 1, 0.1);
                finish(a);
            }
            case "roar" -> {
                for (ServerPlayer p : ps) {
                    if (p.distanceToSqr(mob) <= a.radius * a.radius) {
                        applyEffect(p, a.effect, a.ticks, a.amp);
                        if (a.damage > 0) damage(p, a.damage);
                        p.knockback(0.8, mob.getX() - p.getX(), mob.getZ() - p.getZ());
                    }
                }
                sound(SoundEvents.WARDEN_SONIC_BOOM, 2.0F, 0.8F);
                finish(a);
            }
            default -> finish(a);
        }
    }

    private void bolts(Data.Attack a, List<ServerPlayer> ps) {
        if (ps.isEmpty()) return;
        Vec3 from = mob.getEyePosition();
        for (int i = 0; i < Math.max(1, a.count); i++) {
            ServerPlayer p = ps.get(i % ps.size());
            Vec3 to = p.getEyePosition().add((rnd.nextDouble() - 0.5) * 1.2, (rnd.nextDouble() - 0.5) * 0.8, (rnd.nextDouble() - 0.5) * 1.2);
            if (from.distanceTo(to) > a.range) continue;
            HitResult hr = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
            boolean blocked = hr.getType() == HitResult.Type.BLOCK;
            Vec3 end = blocked ? hr.getLocation() : to;
            Vec3 d = end.subtract(from);
            int steps = (int) Math.max(2, d.length() * 2);
            ParticleOptions pt = a.fire ? ParticleTypes.FLAME : ParticleTypes.END_ROD;
            for (int s = 1; s < steps; s++) {
                Vec3 q = from.add(d.scale((double) s / steps));
                level.sendParticles(pt, q.x, q.y, q.z, 1, 0, 0, 0, 0);
            }
            if (!blocked && rnd.nextFloat() > 0.2F) {
                damage(p, a.damage);
                if (a.fire) p.igniteForSeconds(4.0F);
                applyEffect(p, a.effect, a.ticks, a.amp);
            }
        }
        sound(SoundEvents.BLAZE_SHOOT, 1.5F, 0.8F);
    }

    private void summon(Data.Attack a, List<ServerPlayer> ps) {
        if (a.minion == null) return;
        var type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(a.minion));
        if (type.isEmpty()) return;
        String tag = T_MINION + mob.getUUID();
        int existing = level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(40), m -> m.entityTags().contains(tag)).size();
        for (int i = 0; i < a.count && existing + i < a.cap; i++) {
            Vec3 m = marks.isEmpty() ? mob.position().add(rnd.nextInt(5) - 2, 0, rnd.nextInt(5) - 2) : marks.get(i % marks.size());
            var e = type.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (e == null) continue;
            e.snapTo(m.x, m.y, m.z, rnd.nextFloat() * 360F, 0);
            e.addTag(tag);
            if (e instanceof Mob mm) {
                try {
                    mm.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(m)), EntitySpawnReason.MOB_SUMMONED, null);
                } catch (RuntimeException ignored) {
                    // a minion without its starting gear is still a minion
                }
                if (!ps.isEmpty()) mm.setTarget(ps.get(rnd.nextInt(ps.size())));
            }
            level.addFreshEntity(e);
            level.sendParticles(ParticleTypes.POOF, m.x, m.y + 0.5, m.z, 8, 0.3, 0.4, 0.3, 0.02);
        }
        sound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.5F, 0.8F);
    }

    private void tickZones() {
        if (zones.isEmpty()) return;
        zones.removeIf(z -> z.left[0] <= 0);
        for (Zone z : zones) {
            z.left[0]--;
            if (z.left[0] % 5 == 0) ring(ParticleTypes.SMOKE, z.pos.add(0, 0.2, 0), z.r * (0.4 + rnd.nextDouble() * 0.6), 6);
            if (z.left[0] % 20 == 0) {
                for (ServerPlayer p : participants()) {
                    if (Math.hypot(p.getX() - z.pos.x, p.getZ() - z.pos.z) <= z.r && Math.abs(p.getY() - z.pos.y) < 3) applyEffect(p, z.effect, z.ticks, z.amp);
                }
            }
        }
    }

    // ------------------------------------------------------------------ helpers
    private void ring(ParticleOptions p, Vec3 c, double r, int n) {
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            level.sendParticles(p, c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }
    }

    private void sound(SoundEvent s, float vol, float pitch) {
        level.playSound(null, mob.blockPosition(), s, SoundSource.HOSTILE, vol, pitch);
    }

    // ------------------------------------------------------------------ end of the fight
    /** Rolls the profile's loot against the world's Age. Public so the self test can check it without a kill. */
    public List<ItemStack> rollLoot(int worldAge) {
        List<ItemStack> out = new ArrayList<>();
        for (Data.Loot l : prof.loot) {
            if (worldAge < l.min_age || rnd.nextDouble() > l.chance) continue;
            var item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(l.item));
            if (item.isEmpty() || item.get() == net.minecraft.world.item.Items.AIR) continue;
            int n = l.min + rnd.nextInt(Math.max(1, l.max - l.min + 1)) + l.per_age * (worldAge - l.min_age) / 2;
            addStacks(out, item.get(), Math.max(1, n));
        }
        var trophy = DiaithirBosses.TROPHIES.get(prof.id);
        if (trophy != null) out.add(new ItemStack(trophy.get()));
        return out;
    }

    private static void addStacks(List<ItemStack> out, Item i, int n) {
        int max = new ItemStack(i).getMaxStackSize();
        while (n > 0) {
            int c = Math.min(max, n);
            out.add(new ItemStack(i, c));
            n -= c;
        }
    }

    public void onDeath(@Nullable DamageSource src) {
        if (dead) return;
        dead = true;
        int worldAge = Ages.current(level.getServer());
        for (ItemStack s : rollLoot(worldAge)) {
            ItemEntity ie = new ItemEntity(level, mob.getX(), mob.getY() + 0.5, mob.getZ(), s);
            ie.setDeltaMovement((rnd.nextDouble() - 0.5) * 0.3, 0.3, (rnd.nextDouble() - 0.5) * 0.3);
            level.addFreshEntity(ie);
        }
        int xp = prof.xp;
        while (xp > 0) {
            int c = Math.min(xp, 400);
            ExperienceOrb.award(level, mob.position(), c);
            xp -= c;
        }
        String tag = T_MINION + mob.getUUID();
        for (Mob m : level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(48), m -> m.entityTags().contains(tag))) m.discard();
        for (String t : mob.entityTags()) {
            if (t.startsWith(T_ALTAR)) {
                BlockPos p = parse(t.substring(T_ALTAR.length()));
                if (level.getBlockEntity(p) instanceof AltarBlockEntity be) be.defeated(level.getGameTime());
            }
        }
        Component msg = Component.translatable("death.attack.generic", Component.translatable("boss." + DiaithirBosses.ID + "." + prof.id));
        for (ServerPlayer p : participants()) p.sendSystemMessage(msg);
        close();
    }

    public void close() {
        bar.removeAllPlayers();
        ALL.remove(mob.getUUID());
    }
}
