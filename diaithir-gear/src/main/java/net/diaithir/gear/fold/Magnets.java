package net.diaithir.gear.fold;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Item magnets. Design from "Magnets by Juix" (CC0-1.0, datapack): an Item Magnet (radius 4 m) or Super Item Magnet (radius 8 m) in either
 * hand pulls loose items and experience orbs onto the holder, ignoring anything younger than 5 ticks so thrown items can leave.
 * Written as a real item rather than a function-tick datapack.
 */
public final class Magnets {
    static final int MIN_AGE = 5;

    static double radiusOf(ItemStack s) {
        if (s.is(Folds.SUPER_ITEM_MAGNET.get())) return 8.0;
        if (s.is(Folds.ITEM_MAGNET.get())) return 4.0;
        return 0.0;
    }

    /** Pull everything in range to the player; returns the number of entities moved. */
    public static int pull(ServerPlayer p, double radius) {
        if (radius <= 0 || p.isSpectator()) return 0;
        AABB box = p.getBoundingBox().inflate(radius);
        int n = 0;
        List<ItemEntity> items = p.level().getEntitiesOfClass(ItemEntity.class, box, e -> e.tickCount >= MIN_AGE && e.distanceToSqr(p) <= radius * radius);
        for (ItemEntity e : items) { e.teleportTo(p.getX(), p.getY(), p.getZ()); n++; }
        List<ExperienceOrb> orbs = p.level().getEntitiesOfClass(ExperienceOrb.class, box, e -> e.tickCount >= MIN_AGE && e.distanceToSqr(p) <= radius * radius);
        for (ExperienceOrb e : orbs) { e.teleportTo(p.getX(), p.getY(), p.getZ()); n++; }
        return n;
    }

    @SubscribeEvent
    public void tick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        double r = Math.max(radiusOf(p.getMainHandItem()), radiusOf(p.getOffhandItem()));
        if (r > 0) pull(p, r);
    }
}
