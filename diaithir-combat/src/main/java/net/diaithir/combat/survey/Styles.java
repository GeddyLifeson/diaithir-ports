package net.diaithir.combat.survey;

import net.diaithir.combat.DiaithirCombat;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Critical strikes and weapon styles (survey part 2: Critical Strike and the weapon-category part of Epic Fight, both rewritten from
 * behaviour with no upstream code).
 * Critical strike: two player attributes, crit_chance (default 5 percent) and crit_damage (default x1.5). A hit that rolls under the chance
 * multiplies damage and shows crit particles.
 * Styles (by weapon name): dagger hits from behind do x1.5; rapier adds 10 percent crit chance; katana fills bleeding; warhammer fills
 * stagger; glaive and halberd carry half of the blow through to one more foe behind the first.
 */
public final class Styles {
    public static final DeferredRegister<Attribute> ATTRS = DeferredRegister.create(Registries.ATTRIBUTE, DiaithirCombat.ID);
    public static final DeferredHolder<Attribute, Attribute> CRIT_CHANCE = ATTRS.register("crit_chance",
            () -> new RangedAttribute("attribute.name.diaithircombat.crit_chance", 0.05, 0.0, 1.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> CRIT_DAMAGE = ATTRS.register("crit_damage",
            () -> new RangedAttribute("attribute.name.diaithircombat.crit_damage", 1.5, 1.0, 10.0).setSyncable(true));

    public static final float BACKSTAB = 1.5F;
    public static final float CLEAVE_SHARE = 0.5F;
    private static boolean cleaving;

    public static void register(IEventBus bus) {
        ATTRS.register(bus);
        bus.addListener(EntityAttributeModificationEvent.class, e -> {
            e.add(EntityType.PLAYER, CRIT_CHANCE);
            e.add(EntityType.PLAYER, CRIT_DAMAGE);
        });
    }

    /** Pure: damage multiplier for a roll in [0,1) against a chance. */
    public static float critMultiplier(double chance, double mult, double roll) { return roll < chance ? (float) mult : 1.0F; }

    /** Pure: true when the attacker stands in the rear 120 degree arc of a target facing yaw (degrees, Minecraft convention). */
    public static boolean isBehind(float targetYawDeg, double dx, double dz) {
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1e-6) return false;
        double yaw = Math.toRadians(targetYawDeg);
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        // dx,dz points from attacker to target, so a target facing away from the attacker has forward parallel to it
        return (dx / len) * fx + (dz / len) * fz > 0.5;
    }

    static String name(ItemStack s) { return s.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(s.getItem()).getPath(); }

    @SubscribeEvent
    public void incoming(LivingIncomingDamageEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer p) || !(e.getSource().getDirectEntity() == p)) return;
        LivingEntity t = e.getEntity();
        String w = name(p.getMainHandItem());
        float amount = e.getAmount();
        if (w.contains("dagger") && isBehind(t.getYRot(), t.getX() - p.getX(), t.getZ() - p.getZ())) amount *= BACKSTAB;
        double chance = p.getAttributeValue(CRIT_CHANCE) + (w.contains("rapier") ? 0.10 : 0.0);
        float m = critMultiplier(chance, p.getAttributeValue(CRIT_DAMAGE), p.getRandom().nextDouble());
        if (m > 1.0F && p.level() instanceof ServerLevel sl) {
            amount *= m;
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, t.getX(), t.getEyeY() - 0.3, t.getZ(), 10, 0.3, 0.2, 0.3, 0.3);
        }
        e.setAmount(amount);
    }

    @SubscribeEvent
    public void damaged(LivingDamageEvent.Post e) {
        if (cleaving || !(e.getSource().getEntity() instanceof ServerPlayer p) || e.getSource().getDirectEntity() != p) return;
        if (!(p.level() instanceof ServerLevel sl)) return;
        LivingEntity t = e.getEntity();
        String w = name(p.getMainHandItem());
        if (w.contains("katana")) Buildup.add(t, Buildup.Kind.BLEEDING, 20);
        if (w.contains("warhammer")) Buildup.add(t, Buildup.Kind.STAGGER, 30);
        if (w.contains("glaive") || w.contains("halberd")) {
            Vec3 dir = t.position().subtract(p.position()).multiply(1, 0, 1);
            if (dir.lengthSqr() < 1e-6) return;
            dir = dir.normalize();
            Vec3 c = t.position().add(dir.scale(1.5));
            LivingEntity best = null; double bd = 1e9;
            for (LivingEntity o : sl.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(1.5), x -> x != p && x != t && x.isAlive())) {
                double d = o.distanceToSqr(c);
                if (d < bd) { bd = d; best = o; }
            }
            if (best != null) {
                cleaving = true;
                try { best.hurtServer(sl, e.getSource(), e.getInflictedDamage() * CLEAVE_SHARE); } finally { cleaving = false; }
            }
        }
    }

    public Styles() {}
}
