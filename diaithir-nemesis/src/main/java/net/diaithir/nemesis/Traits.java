package net.diaithir.nemesis;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** What rank and traits do to the mob carrying a nemesis. */
final class Traits {
    private Traits() {}

    /** Names, keeps and strengthens the mob; safe to call again after a rank or trait change. */
    static void apply(Mob mob, Nemesis n, Rules rules) {
        mob.setCustomName(Component.literal(n.fullName()));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.addTag(n.tag());
        modify(mob, Attributes.MAX_HEALTH, "rank_health", 0.25 * n.rank + (has(n, "hardy") ? 0.5 : 0), Operation.ADD_MULTIPLIED_BASE);
        modify(mob, Attributes.ATTACK_DAMAGE, "rank_damage", n.rank + (has(n, "brutal") ? 3 : 0), Operation.ADD_VALUE);
        modify(mob, Attributes.ARMOR, "thick_hide", has(n, "thick_hide") ? 6 : 0, Operation.ADD_VALUE);
        modify(mob, Attributes.MOVEMENT_SPEED, "swift", has(n, "swift") ? 0.2 : 0, Operation.ADD_MULTIPLIED_BASE);
        mob.setHealth(mob.getMaxHealth());
    }

    static void onHit(Mob attacker, LivingEntity target, Nemesis n) {
        if (has(n, "venomous")) target.addEffect(new MobEffectInstance(MobEffects.POISON, 100), attacker);
        if (has(n, "frostbite")) target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1), attacker);
        if (has(n, "fire_thief")) target.igniteForSeconds(4);
        if (has(n, "bloodfed")) attacker.heal(3);
    }

    static boolean has(Nemesis n, String trait) { return n.traits.contains(trait); }

    private static void modify(Mob mob, Holder<Attribute> attribute, String id, double amount, Operation op) {
        AttributeInstance a = mob.getAttribute(attribute);
        if (a == null) return;
        Identifier key = Identifier.fromNamespaceAndPath(DiaithirNemesis.ID, id);
        a.removeModifier(key);
        if (amount != 0) a.addPermanentModifier(new AttributeModifier(key, amount, op));
    }
}
