package net.diaithir.bridges;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/** Two mods, one status effect: an effect applied under the second mod's id turns into the kept effect (same time, level and flags). */
final class EffectMerge {
    private static final Map<String, String> KEPT = Map.of(
            "tconstruct:bleeding", "apothic_attributes:bleeding",
            "vampirism:bleeding", "apothic_attributes:bleeding",
            "neovitae:flight", "reliquary:flight",
            "occultism:double_jump", "tconstruct:double_jump",
            "tconstruct:venom", "aether_ii:venom",
            "spell_engine:stun", "aether_ii:stun",
            "skill_tree_rpgs:vitality", "apothic_attributes:vitality",
            "auroral:frostbite", "aether_ii:frostbite");

    static void register() {
        NeoForge.EVENT_BUS.addListener(MobEffectEvent.Applicable.class, e -> {
            Holder<MobEffect> alias = e.getEffectInstance().getEffect();
            String to = alias.unwrapKey().map(k -> KEPT.get(k.identifier().toString())).orElse(null);
            if (to == null) {
                return;
            }
            BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(to)).ifPresent(kept -> {
                MobEffectInstance old = e.getEffectInstance();
                var entity = e.getEntity();
                e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                var swap = new MobEffectInstance(kept, old.getDuration(), old.getAmplifier(), old.isAmbient(), old.isVisible(), old.showIcon());
                entity.level().getServer().execute(() -> entity.addEffect(swap));
            });
        });
    }

    private EffectMerge() {
    }
}
