package net.diaithir.bosses;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Hooks: a controller is created for any Mob that a profile names, and lives until it leaves the level. */
public final class BossEvents {
    private static final Set<EntitySpawnReason> ELITE_SPAWNS = Set.of(EntitySpawnReason.NATURAL, EntitySpawnReason.CHUNK_GENERATION,
        EntitySpawnReason.STRUCTURE, EntitySpawnReason.PATROL, EntitySpawnReason.EVENT, EntitySpawnReason.REINFORCEMENT);

    @SubscribeEvent
    public static void join(EntityJoinLevelEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl) || !(e.getEntity() instanceof Mob m)) return;
        if (BossController.ALL.containsKey(m.getUUID())) return;
        Data.Profile p = Data.byEntity(BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).toString());
        if (p == null) return;
        BossController.ALL.put(m.getUUID(), new BossController(m, sl, p));
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent e) {
        if (e.getLevel().isClientSide()) return;
        BossController c = BossController.ALL.get(e.getEntity().getUUID());
        if (c != null) c.close();
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof Mob m) || m.level().isClientSide()) return;
        BossController c = BossController.ALL.get(m.getUUID());
        if (c != null) c.tick();
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        if (e.getEntity().level().isClientSide()) return;
        BossController c = BossController.ALL.get(e.getEntity().getUUID());
        if (c != null) c.onDeath(e.getSource());
        Elites.onDeath(e.getEntity());
    }

    /** No single hit may take more than a profile's share of the boss's health. */
    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent e) {
        BossController c = BossController.ALL.get(e.getEntity().getUUID());
        if (c == null) return;
        if (e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        float amount = e.getAmount() / c.damageDivisor;
        float cap = (float) (e.getEntity().getMaxHealth() * c.prof.max_hit_fraction);
        e.setAmount(Math.min(amount, cap));
    }

    @SubscribeEvent
    public static void damaged(LivingDamageEvent.Post e) {
        if (e.getEntity().level().isClientSide()) return;
        Elites.onDamaged(e.getEntity(), e.getSource(), e.getInflictedDamage());
    }

    @SubscribeEvent
    public static void finalizeSpawn(FinalizeSpawnEvent e) {
        if (!ELITE_SPAWNS.contains(e.getSpawnType())) return;
        Mob m = e.getEntity();
        if (!(m.level() instanceof ServerLevel sl)) return;
        Elites.roll(m, Ages.current(sl.getServer()), sl.getRandom());
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent e) {
        BossController.ALL.values().forEach(c -> c.bar.removeAllPlayers());
        BossController.ALL.clear();
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        BossCommands.register(e.getDispatcher());
    }

    @SubscribeEvent
    public static void reload(AddServerReloadListenersEvent e) {
        e.addListener(Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "profiles"), new SimplePreparableReloadListener<Boolean>() {
            @Override protected Boolean prepare(ResourceManager manager, ProfilerFiller profiler) { return Boolean.TRUE; }
            @Override protected void apply(Boolean prepared, ResourceManager manager, ProfilerFiller profiler) { Data.reload(manager); }
        });
    }

    private BossEvents() {}
}
