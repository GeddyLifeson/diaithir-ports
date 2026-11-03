package net.diaithir.aurorian;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Tier abilities of the Aurorian gear, ported from upstream (config values are upstream's defaults):
 * moonstone durability, aurorian steel levelling enchantments, spectral cleanse, umbra thorns and greatsword, crystalline shield repair.
 */
public final class Abilities {
    private static final double MOONSTONE_SAVE_CHANCE = 0.5;
    private static final int STEEL_BASE_XP = 100;
    private static final double STEEL_XP_GROWTH = 1.25;
    private static final double SPECTRAL_CLEANSE_PER_PIECE = 0.06;

    public static String idOf(ItemStack s) {
        return BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();
    }

    /** Item damage hook: returns the damage to apply. */
    public static int damage(ItemStack stack, int amount, LivingEntity user, String id) {
        if (id.startsWith("moonstone_")) {
            if (user.getRandom().nextDouble() < MOONSTONE_SAVE_CHANCE) {
                amount--;
            }
            if (user.level().isBrightOutside()) {
                amount++;
            }
            return Math.max(0, amount);
        }
        if (id.startsWith("aurorian_steel_")) {
            steelXp(stack, user);
            return amount;
        }
        if (id.equals("crystalline_shield") && user instanceof Player p) {
            ItemStack main = p.getMainHandItem();
            if (p.getOffhandItem() == stack && main.isDamageableItem() && main.isDamaged() && !main.is(stack.getItem())
                && main.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirAurorian.ID, "crystalline_shield_repairable")))) {
                main.setDamageValue(main.getDamageValue() - 1);
                return amount + 1;
            }
        }
        return amount;
    }

    private static void steelXp(ItemStack stack, LivingEntity user) {
        ItemEnchantments ench = stack.getEnchantments();
        Holder<Enchantment> pick = null;
        int level = 0;
        for (var e : ench.entrySet()) {
            if (e.getIntValue() < e.getKey().value().getMaxLevel()) {
                pick = e.getKey();
                level = e.getIntValue();
                break;
            }
        }
        if (pick == null) {
            return;
        }
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        int xp = cd.copyTag().getIntOr("xp", 0) + 1;
        float mult = Math.max(1F, cd.copyTag().getFloatOr("multiplier", 1F));
        if (xp < STEEL_BASE_XP * mult) {
            final int fxp = xp;
            CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putInt("xp", fxp));
            return;
        }
        final Holder<Enchantment> h = pick;
        final int next = level + 1;
        EnchantmentHelper.updateEnchantments(stack, m -> m.set(h, next));
        final float nm = (float) (mult * STEEL_XP_GROWTH);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> {
            t.putInt("xp", 0);
            t.putFloat("multiplier", nm);
        });
        user.level().playSound(null, user, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1F, 1F);
    }

    /** Umbra greatsword: slow but resistant while held (called from inventory tick). */
    public static void greatswordTick(ItemStack stack, net.minecraft.world.entity.Entity e) {
        if (e instanceof Player p && (p.getMainHandItem() == stack || p.getOffhandItem() == stack)) {
            p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 1, false, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 35, 0, false, false, true));
        }
    }

    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent e) {
        LivingEntity victim = e.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        // spectral armour: attacking players cleanse a harmful effect
        if (e.getSource().getEntity() instanceof Player p) {
            int pieces = 0;
            for (EquipmentSlot s : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                if (idOf(p.getItemBySlot(s)).startsWith("spectral_")) {
                    pieces++;
                }
            }
            if (pieces > 0 && p.getRandom().nextDouble() < pieces * SPECTRAL_CLEANSE_PER_PIECE) {
                List<Holder<MobEffect>> bad = new ArrayList<>();
                for (MobEffectInstance i : p.getActiveEffects()) {
                    if (!i.getEffect().value().isBeneficial()) {
                        bad.add(i.getEffect());
                    }
                }
                if (!bad.isEmpty()) {
                    p.removeEffect(bad.get(0));
                }
            }
        }
        // umbra chestplate: crouching grants thorns III
        if (victim instanceof Player v && v.isCrouching() && idOf(v.getItemBySlot(EquipmentSlot.CHEST)).equals("umbra_chestplate")
            && e.getSource().getEntity() instanceof LivingEntity attacker && v.getRandom().nextFloat() < 0.45F) {
            attacker.hurt(victim.damageSources().thorns(victim), 1 + v.getRandom().nextInt(4));
        }
    }

    private Abilities() {}
}
