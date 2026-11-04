package com.diaithir.overworld_biomes.space;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Airless worlds. In the Moon and Earth orbit a player in survival loses breath health every second unless they wear the
 * whole space suit with an oxygen module in their pack, or stand within 6 blocks of an oxygen distributor. The Moon also
 * has about a sixth of Earth gravity.
 */
public final class SpaceAir {
    private static final Identifier LOW_G = Identifier.fromNamespaceAndPath("stellaris", "moon_gravity");
    private static final Identifier SOFT_FALL = Identifier.fromNamespaceAndPath("stellaris", "moon_fall");
    private static final String[] SUIT = {"space_suit_helmet", "space_suit_chestplate", "space_suit_leggings", "space_suit_boots"};
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @SubscribeEvent
    public void tick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 0) return;
        boolean moon = p.level().dimension().equals(SpaceTravel.MOON);
        var g = p.getAttribute(Attributes.GRAVITY);
        var f = p.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (g != null) {
            if (moon) g.addOrUpdateTransientModifier(new AttributeModifier(LOW_G, -0.83, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            else g.removeModifier(LOW_G);
        }
        if (f != null) {
            if (moon) f.addOrUpdateTransientModifier(new AttributeModifier(SOFT_FALL, -0.8, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            else f.removeModifier(SOFT_FALL);
        }
        if (!SpaceTravel.isVacuum(p.level()) || p.isCreative() || p.isSpectator() || breathable(p)) return;
        p.hurtServer(p.level(), p.damageSources().drown(), 2.0F);
        p.sendSystemMessage(Component.literal("No air here. Wear the full space suit with an oxygen module, or stand near an oxygen distributor."), true);
    }

    static boolean breathable(ServerPlayer p) {
        boolean suit = true;
        for (int i = 0; i < SUIT.length; i++) {
            suit &= id(p.getItemBySlot(SLOTS[i])).equals("stellaris:" + SUIT[i]);
        }
        if (suit) {
            for (ItemStack s : p.getInventory()) {
                if (id(s).startsWith("stellaris:space_suit_oxygen_module")) return true;
            }
        }
        BlockPos c = p.blockPosition();
        for (BlockPos q : BlockPos.betweenClosed(c.offset(-6, -6, -6), c.offset(6, 6, 6))) {
            if (p.level().getBlockState(q).getBlock() == BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("stellaris", "oxygen_distributor"))) return true;
        }
        return false;
    }

    private static String id(ItemStack s) {
        return BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
    }
}
