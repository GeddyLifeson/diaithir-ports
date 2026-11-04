package net.diaithir.gear.fold;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Repair kits, a durability pool you spend on other items. Design from "Iron Repair Kits" (MIT, MCreator mod, tiers and pool sizes kept);
 * rewritten without its GUI procedures: hold the kit in one hand and the damaged item in the other, use the kit, and damage moves from the
 * item to the kit point for point. Heavy kits slow the carrier ({@code slow}, {@code fatigue} are the effect amplifiers plus one, 0 = none).
 */
public class RepairKit extends Item {
    final int slow, fatigue;

    public RepairKit(Properties p, int slow, int fatigue) { super(p); this.slow = slow; this.fatigue = fatigue; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack kit = player.getItemInHand(hand);
        ItemStack target = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (target.isEmpty() || !target.isDamaged() || target.getItem() instanceof RepairKit) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        int moved = transfer(kit, target);
        if (moved > 0 && player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("fold.diaithirgear.repaired", moved, target.getHoverName()), true);
            level.levelEvent(1030, player.blockPosition(), 0);
            if (kit.isEmpty() || kit.getDamageValue() >= kit.getMaxDamage()) kit.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    /** Move damage from target to kit, at most what the kit has left. Returns durability repaired. */
    public static int transfer(ItemStack kit, ItemStack target) {
        int left = kit.getMaxDamage() - kit.getDamageValue();
        int moved = Math.min(left, target.getDamageValue());
        if (moved <= 0) return 0;
        target.setDamageValue(target.getDamageValue() - moved);
        kit.setDamageValue(kit.getDamageValue() + moved);
        return moved;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        out.accept(Component.translatable("fold.diaithirgear.kit_left", stack.getMaxDamage() - stack.getDamageValue()));
        if (slow > 0) out.accept(Component.translatable("fold.diaithirgear.kit_slow", slow));
        if (fatigue > 0) out.accept(Component.translatable("fold.diaithirgear.kit_fatigue", fatigue));
    }

    /** Burden: a heavy kit anywhere in the inventory applies its effects, refreshed every second. */
    public static final class Carry {
        @SubscribeEvent
        public void tick(PlayerTickEvent.Post e) {
            if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 3) return;
            int slow = 0, fat = 0;
            for (ItemStack s : p.getInventory().getNonEquipmentItems()) {
                if (s.getItem() instanceof RepairKit k) { slow = Math.max(slow, k.slow); fat = Math.max(fat, k.fatigue); }
            }
            if (slow > 0) p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, slow - 1, false, false));
            if (fat > 0) p.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 30, fat - 1, false, false));
        }
    }
}
