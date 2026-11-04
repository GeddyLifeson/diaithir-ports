package net.diaithir.hostile.hordes.neoforge.event;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.diaithir.hostile.hordes.HordeHoardWorkQueue;
import net.diaithir.hostile.hordes.command.HordeHoardCommands;
import net.diaithir.hostile.hordes.procedures.HordeSpawnerProcedure;
import net.diaithir.hostile.hordes.procedures.HordeStopMultiplyingProcedure;
import net.diaithir.hostile.hordes.procedures.HordeTickProcedure;
import net.diaithir.hostile.hordes.procedures.IntelligentDashAwayProcedure;
import net.diaithir.hostile.hordes.procedures.IntelligentSpawnsProcedure;
import net.diaithir.hostile.hordes.procedures.IntelligentTickProcedure;
import net.diaithir.hostile.hordes.procedures.IronGolemAttackedProcedure;
import net.diaithir.hostile.hordes.procedures.IronGolemHealCooldownProcedure;
import net.diaithir.hostile.hordes.procedures.IronGolemSwooshProcedure;
import net.diaithir.hostile.hordes.procedures.IronGolemTickProcedure;
import net.diaithir.hostile.hordes.procedures.LeapTickProcedure;
import net.diaithir.hostile.hordes.procedures.TeamMakerProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bridges 26.1.2 NeoForge events to common procedures.
 *
 * Source-event mapping:
 *   - 1.20.1 LivingEvent.LivingTickEvent  → 26.1.2 EntityTickEvent.Post (filtered to LivingEntity)
 *   - 1.20.1 LivingAttackEvent            → 26.1.2 LivingIncomingDamageEvent (cancelable)
 *   - 1.20.1 EntityJoinLevelEvent         → 26.1.2 EntityJoinLevelEvent (unchanged)
 *   - 1.20.1 LevelEvent.Load              → 26.1.2 LevelEvent.Load (unchanged)
 *   - 1.20.1 TickEvent.ServerTickEvent    → 26.1.2 ServerTickEvent.Post
 *
 * If your specific NeoForge 26.1.2.1-beta exposes different class names (e.g. an older
 * LivingAttackEvent-style cancelable damage event, or EntityTickEvent without Pre/Post
 * inner classes), adjust the imports + @SubscribeEvent parameter types.
 */
@EventBusSubscriber(modid = net.diaithir.hostile.celestials.DiaithirHostile.ID)
public final class HordeHoardNeoForgeEvents {

    private HordeHoardNeoForgeEvents() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        if (entity.level().isClientSide()) return;
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        HordeTickProcedure.execute(entity.level(), x, y, z, entity);
        IntelligentTickProcedure.execute(entity.level(), x, y, z, entity);
        IronGolemTickProcedure.execute(entity.level(), x, y, z, entity);
        LeapTickProcedure.execute(entity.level(), x, y, z, entity);
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity self = event.getEntity();
        Entity src = event.getSource().getEntity();
        Entity directSrc = event.getSource().getDirectEntity();
        double x = self.getX();
        double y = self.getY();
        double z = self.getZ();

        HordeStopMultiplyingProcedure.execute(self);
        IronGolemHealCooldownProcedure.execute(self);
        if (src != null) {
            IronGolemSwooshProcedure.execute(self.level(), src);
        }

        if (src != null && IntelligentDashAwayProcedure.execute(self.level(), x, y, z, self, src)) {
            event.setCanceled(true);
            return;
        }
        if (directSrc != null && IronGolemAttackedProcedure.execute(self, directSrc)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        IntelligentSpawnsProcedure.execute(event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        TeamMakerProcedure.execute(event.getLevel());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        HordeHoardWorkQueue.onServerTickEnd();
        HordeSpawnerProcedure.onServerTick(event.getServer());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        HordeHoardCommands.register(event.getDispatcher());
    }
}
