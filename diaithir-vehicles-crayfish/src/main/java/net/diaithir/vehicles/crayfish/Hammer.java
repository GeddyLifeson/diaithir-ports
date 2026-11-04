// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

/** Repair hammer: each blow gives a vehicle 5 health back (any entity with {@link CrayfishCaps#HEALTH}). */
public class Hammer extends Item {
    public Hammer(Properties p) {
        super(p);
    }

    public static boolean strike(Player player, InteractionHand hand, ItemStack stack, Entity target) {
        VehicleHealth h = target.getCapability(CrayfishCaps.HEALTH, null);
        if (h == null || !h.repair()) return false;
        stack.hurtAndBreak(1, player, hand);
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.6F, 1.2F);
        player.swing(hand);
        return true;
    }
}
