package net.diaithir.spartan;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** The conditional weapon habits of the original mod (backstab, unarmoured, undead, riding, nausea). */
public final class TraitEvents {
    @SubscribeEvent
    public void onDamage(LivingIncomingDamageEvent e) {
        if (!(e.getSource().getDirectEntity() instanceof LivingEntity attacker) || e.getSource().getEntity() != e.getSource().getDirectEntity()) return;
        var stack = attacker.getMainHandItem();
        if (stack.isEmpty()) return;
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!SpartanWeaponry.ID.equals(key.getNamespace())) return;
        var spec = SpartanWeaponry.BY_ID.get(key.getPath());
        if (spec == null || spec.flags().isEmpty()) return;
        LivingEntity target = e.getEntity();
        float amount = e.getAmount();
        for (String f : spec.flags()) {
            switch (f) {
                case "backstab" -> {
                    float diff = Math.abs(net.minecraft.util.Mth.wrapDegrees(attacker.getYRot() - target.getYRot()));
                    if (diff < 60.0F) amount *= 3.0F;
                }
                case "unarmoured" -> {
                    if (target.getAttributeValue(Attributes.ARMOR) <= 0.0) amount *= 3.0F;
                }
                case "undead" -> {
                    if (target.is(EntityTypeTags.UNDEAD)) amount *= 1.5F;
                }
                case "riding" -> {
                    if (attacker.isPassenger()) amount *= 2.0F;
                }
                case "nausea" -> target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
                default -> {}
            }
        }
        e.setAmount(amount);
    }
}
