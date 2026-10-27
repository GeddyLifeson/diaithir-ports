package net.diaithir.steamrails;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.function.ToIntFunction;

/** What a player's hands do to a boiler or a tender: pour a water bucket, load fuel, read the gauges. Shared by blocks in the world and blocks on a moving carriage. */
final class Hand {
    private Hand() {}

    static ToIntFunction<ItemStack> burn(Level level) {
        return s -> level.fuelValues().burnDuration(s);
    }

    private static void swapBucket(Player p, InteractionHand hand, ItemStack held) {
        if (p.hasInfiniteMaterials()) return;
        held.shrink(1);
        ItemStack empty = new ItemStack(Items.BUCKET);
        if (held.isEmpty()) p.setItemInHand(hand, empty);
        else if (!p.getInventory().add(empty)) p.drop(empty, false);
    }

    /** Returns true when the hand did something. */
    static boolean boiler(Level level, Player p, InteractionHand hand, BoilerState b, int waterCap) {
        ItemStack held = p.getItemInHand(hand);
        if (held.is(Items.WATER_BUCKET)) {
            if (b.addWater(1000, waterCap) > 0) {
                swapBucket(p, hand, held);
                return true;
            }
            return false;
        }
        ToIntFunction<ItemStack> burn = burn(level);
        if (!held.isEmpty() && burn.applyAsInt(held) > 0) {
            ItemStack rest = b.addFuel(held, burn);
            if (rest.getCount() != held.getCount()) {
                if (!p.hasInfiniteMaterials()) held.setCount(rest.getCount());
                return true;
            }
        }
        return false;
    }

    static boolean tender(Level level, Player p, InteractionHand hand, TenderState t) {
        ItemStack held = p.getItemInHand(hand);
        if (held.is(Items.WATER_BUCKET)) {
            if (t.addWater(1000) > 0) {
                swapBucket(p, hand, held);
                return true;
            }
            return false;
        }
        ToIntFunction<ItemStack> burn = burn(level);
        if (!held.isEmpty() && burn.applyAsInt(held) > 0) {
            ItemStack rest = t.addFuel(held, burn);
            if (rest.getCount() != held.getCount()) {
                if (!p.hasInfiniteMaterials()) held.setCount(rest.getCount());
                return true;
            }
        }
        return false;
    }

    static Component boilerStatus(BoilerState b, Layout l) {
        return Component.translatable("message.diaithirsteamrails.boiler", String.format("%.1f", b.pressure), l.rating, (int) b.water, l.waterCapacity(),
                b.fuelCount(), l.cylinders, Component.translatable(b.burning() ? "message.diaithirsteamrails.fire_lit" : "message.diaithirsteamrails.fire_out"));
    }

    static Component tenderStatus(TenderState t) {
        return Component.translatable("message.diaithirsteamrails.tender", (int) t.water, TenderState.WATER_CAPACITY, t.fuelCount());
    }
}
