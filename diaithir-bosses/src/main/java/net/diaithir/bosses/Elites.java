package net.diaithir.bosses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Champion elites: ordinary monsters that spawned stronger. 'Kuraid' plus one to three affixes chosen from the monster's entry in
 * elites.json; the number of affixes and the chance rise with the Age. The numbers live in the table, the mechanics here.
 */
public final class Elites {
    public static final String TAG = "dbelite", T_AFFIX = "dbaffix:", T_USED = "dbbanded", T_GUARD_DUE = "dbguard_due";
    static final Identifier AGE_HP = Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "elite_age_hp");

    /**
     * Keeps the champion's Age health bonus from multiplying with the health changes other mods make to ordinary mobs
     * (Dwarfs and Giants' random size, -40 to +100 percent; Remnants' level and rank). Those mods act after the spawn event, so this
     * runs on the first ticks and again now and then. Their variety is kept: the total is base x (their factor + our Age bonus)
     * instead of base x their factor x (1 + our bonus), and never above max_combined_health_multiplier.
     * Returns the multiplier the champion ends at (max health over base), or 0 when nothing was adjusted.
     */
    public static double normaliseHealth(Mob mob) {
        Data.Elites t = Data.elites();
        AttributeInstance mh = mob.getAttribute(Attributes.MAX_HEALTH);
        if (mh == null || !isElite(mob)) return 0;
        AttributeModifier ours = mh.getModifier(AGE_HP);
        if (ours == null) return 0;
        double want = t.extra_hp_per_age * Math.max(0, (int) ageOf(mob));
        double base = mh.getBaseValue();
        mh.removeModifier(AGE_HP);
        double others = mh.getValue();
        double factor = base > 0 ? others / base : 1;
        double total = t.additive_stacking ? factor + want : factor * (1 + want);
        if (t.max_combined_health_multiplier > 0) total = Math.min(total, Math.max(factor, t.max_combined_health_multiplier));
        double amount = others > 0 ? Math.max(0, base * total / others - 1) : 0;
        boolean full = mob.getHealth() >= mob.getMaxHealth() - 0.01F;
        mh.addPermanentModifier(new AttributeModifier(AGE_HP, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (full) mob.setHealth(mob.getMaxHealth()); else mob.setHealth(Math.min(mob.getHealth(), mob.getMaxHealth()));
        return mob.getMaxHealth() / base;
    }

    /** The Age bonus the champion was made with, kept as a tag because the modifier itself gets rewritten. */
    private static double ageOf(Mob mob) {
        for (String tag : mob.entityTags()) if (tag.startsWith(T_AGE)) return Integer.parseInt(tag.substring(T_AGE.length()));
        return 0;
    }
    static final String T_AGE = "dbage:";

    public static boolean isElite(LivingEntity e) { return e.entityTags().contains(TAG); }

    public static boolean has(LivingEntity e, String affix) { return e.entityTags().contains(T_AFFIX + affix); }

    /** Rolls the elite chance for a fresh spawn; returns true when the mob became a champion. */
    public static boolean roll(Mob mob, int age, RandomSource r) {
        Data.Elites t = Data.elites();
        Data.EliteBase base = t.bases.get(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString());
        if (base == null || isElite(mob) || mob.entityTags().contains(BossController.TAG)) return false;
        double chance = t.chance_by_age[Math.clamp(age - 1, 0, t.chance_by_age.length - 1)] * base.weight / 10.0;
        if (r.nextDouble() >= chance) return false;
        make(mob, age, r, null);
        return true;
    }

    /** Makes the mob a champion. forced: affix ids to use instead of drawing them. */
    public static List<String> make(Mob mob, int age, RandomSource r, List<String> forced) {
        Data.Elites t = Data.elites();
        Data.EliteBase base = t.bases.get(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString());
        List<String> pool = new ArrayList<>(base != null ? base.affixes : t.affixes.keySet());
        List<String> chosen = new ArrayList<>();
        if (forced != null) chosen.addAll(forced);
        int n = t.affixes_by_age[Math.clamp(age - 1, 0, t.affixes_by_age.length - 1)];
        while (forced == null && chosen.size() < n && !pool.isEmpty()) chosen.add(pool.remove(r.nextInt(pool.size())));
        mob.addTag(TAG);
        mob.addTag(T_AGE + Math.max(0, age - 1));
        double hp = t.extra_hp_per_age * Math.max(0, age - 1);
        for (String id : chosen) {
            Data.Affix a = t.affixes.get(id);
            if (a == null) continue;
            mob.addTag(T_AFFIX + id);
            if (a.attributes != null) {
                for (Map.Entry<String, List<Object>> e : a.attributes.entrySet()) {
                    var attr = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(e.getKey()));
                    if (attr.isEmpty()) continue;
                    AttributeInstance inst = mob.getAttribute(attr.get());
                    if (inst == null) continue;
                    double amt = ((Number) e.getValue().get(0)).doubleValue();
                    AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(String.valueOf(e.getValue().get(1)).toUpperCase(java.util.Locale.ROOT));
                    Identifier mid = Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "elite_" + id + "_" + attr.get().key().identifier().getPath());
                    inst.removeModifier(mid);
                    inst.addPermanentModifier(new AttributeModifier(mid, amt, op));
                }
            }
            if (a.absorption != null) mob.setAbsorptionAmount(a.absorption.floatValue());
            if (a.fire_immune) mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, false, false));
        }
        AttributeInstance mh = mob.getAttribute(Attributes.MAX_HEALTH);
        if (mh != null && hp > 0) {
            mh.removeModifier(Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "elite_age_hp"));
            mh.addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "elite_age_hp"), hp, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        mob.setHealth(mob.getMaxHealth());
        mob.addTag(T_GUARD_DUE);
        MutableComponent names = Component.empty();
        for (int i = 0; i < chosen.size(); i++) {
            if (i > 0) names.append(" ");
            names.append(Component.translatable("elite." + DiaithirBosses.ID + ".affix." + chosen.get(i)));
        }
        mob.setCustomName(Component.translatable("elite." + DiaithirBosses.ID + ".name", names, mob.getType().getDescription()));
        mob.setCustomNameVisible(true);
        return chosen;
    }

    /** After the victim took damage from an attacker. */
    public static void onDamaged(LivingEntity victim, DamageSource src, float amount) {
        if (!(victim.level() instanceof ServerLevel sl)) return;
        Data.Elites t = Data.elites();
        LivingEntity attacker = src.getEntity() instanceof LivingEntity le ? le : null;
        if (attacker != null && isElite(attacker)) {
            for (String tag : attacker.entityTags()) {
                if (!tag.startsWith(T_AFFIX)) continue;
                Data.Affix a = t.affixes.get(tag.substring(T_AFFIX.length()));
                if (a == null) continue;
                if (a.on_hit != null) {
                    Object eff = a.on_hit.get("effect");
                    if (eff != null) {
                        int ticks = ((Number) a.on_hit.getOrDefault("ticks", 100)).intValue(), amp = ((Number) a.on_hit.getOrDefault("amp", 0)).intValue();
                        BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(String.valueOf(eff))).ifPresent(h -> victim.addEffect(new MobEffectInstance(h, ticks, amp)));
                    }
                    Object fire = a.on_hit.get("fire");
                    if (fire != null) victim.igniteForSeconds(((Number) fire).floatValue());
                }
                if (a.leech != null) attacker.heal((float) (amount * a.leech));
            }
        }
        if (isElite(victim)) {
            for (String tag : victim.entityTags()) {
                if (!tag.startsWith(T_AFFIX)) continue;
                String id = tag.substring(T_AFFIX.length());
                Data.Affix a = t.affixes.get(id);
                if (a == null) continue;
                if (a.reflect != null && attacker != null && src.getDirectEntity() == attacker) {
                    attacker.hurtServer(sl, sl.damageSources().thorns(victim), (float) (amount * a.reflect));
                }
                if (a.summon_on_hurt != null && !victim.entityTags().contains(T_USED) && victim instanceof Mob m) {
                    victim.addTag(T_USED);
                    int n = ((Number) a.summon_on_hurt.getOrDefault("count", 2)).intValue();
                    for (int i = 0; i < n; i++) {
                        var e = m.getType().create(sl, EntitySpawnReason.MOB_SUMMONED);
                        if (e == null) continue;
                        e.snapTo(m.getX() + (i - 0.5) * 1.5, m.getY(), m.getZ() + 0.7, 0, 0);
                        if (e instanceof Mob mm && attacker != null) mm.setTarget(attacker);
                        sl.addFreshEntity(e);
                    }
                }
            }
        }
    }

    public static void onDeath(LivingEntity e) {
        if (!(e.level() instanceof ServerLevel sl) || !isElite(e)) return;
        Data.Elites t = Data.elites();
        for (String tag : e.entityTags()) {
            if (!tag.startsWith(T_AFFIX)) continue;
            Data.Affix a = t.affixes.get(tag.substring(T_AFFIX.length()));
            if (a != null && a.blast_on_death != null) {
                float p = ((Number) a.blast_on_death.getOrDefault("power", 3.0)).floatValue();
                sl.explode(e, e.getX(), e.getY(), e.getZ(), p, Level.ExplosionInteraction.MOB);
            }
        }
        ExperienceOrb.award(sl, e.position(), (int) (10 * (t.xp_multiplier - 1)));
        if (t.token != null && sl.getRandom().nextDouble() < t.token.chance && Ages.current(sl.getServer()) >= t.token.min_age) {
            BuiltInRegistries.ITEM.getOptional(Identifier.parse(t.token.item)).ifPresent(i -> sl.addFreshEntity(new ItemEntity(sl, e.getX(), e.getY() + 0.3, e.getZ(), new ItemStack(i))));
        }
    }

    private Elites() {}
}
