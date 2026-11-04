package net.diaithir.worldsys.villagercare;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Villager quality-of-life: sticky professions, daily restock, calmer panic. */
public final class VillagerCareFeature {
    private VillagerCareFeature() {}

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Post.class, VillagerCareFeature::tick);
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, VillagerCareFeature::join);
    }

    private static void join(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof Villager v && e.getLevel() instanceof ServerLevel level) restockIfDawn(v, level);
    }

    private static void tick(EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof Villager v) || !(v.level() instanceof ServerLevel level)) return;
        if (v.tickCount % 20 != 0) return;
        if (v.getVillagerData().level() > 1 || v.getVillagerXp() > 0) v.setVillagerXp(Math.max(v.getVillagerXp(), 1));
        restockIfDawn(v, level);
        calmPanic(v, level);
    }

    private static void restockIfDawn(Villager v, ServerLevel level) {
        if (v.getVillagerData().profession().is(VillagerProfession.NONE) || v.getVillagerData().profession().is(VillagerProfession.NITWIT)) return;
        long dayTime = level.getGameTime();
        long time = dayTime % 24000L;
        if (time > 100L) return;
        if (v.getPersistentData().getLongOr("diaithir_restock_day", -1L) == dayTime / 24000L) return;
        v.getPersistentData().putLong("diaithir_restock_day", dayTime / 24000L);
        for (MerchantOffer offer : v.getOffers()) offer.resetUses();
    }

    private static void calmPanic(Villager v, ServerLevel level) {
        if (!v.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE)) return;
        AABB box = v.getBoundingBox().inflate(6.0D);
        if (level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box, m -> m instanceof Enemy).isEmpty()) {
            v.getBrain().eraseMemory(MemoryModuleType.NEAREST_HOSTILE);
            v.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
            v.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
        }
    }
}
