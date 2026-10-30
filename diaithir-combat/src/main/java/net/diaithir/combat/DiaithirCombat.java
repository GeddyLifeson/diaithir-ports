package net.diaithir.combat;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Komrag (Diaithir Combat): server-authoritative arc melee, parry, dodge roll and bow tuning. */
@Mod(DiaithirCombat.ID)
public final class DiaithirCombat {
    public static final String ID = "diaithircombat";

    /** Arrows fired by a tuned bow: arrow -> drop reduction (0..1 of vanilla gravity cancelled). */
    private static final Map<AbstractArrow, Float> TUNED = new WeakHashMap<>();
    private static final Map<java.util.UUID, Object[]> PENDING = new java.util.concurrent.ConcurrentHashMap<>();

    public DiaithirCombat(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CombatConfig.SPEC);
        Cat.load();
        bus.addListener(Net::register);
        NeoForge.EVENT_BUS.register(this);
        if (FMLEnvironment.getDist().isClient()) CombatClient.init(bus);
    }

    @SubscribeEvent
    public void commands(RegisterCommandsEvent e) { SelfTest.register(e.getDispatcher()); }

    @SubscribeEvent
    public void serverTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) { SelfTest.tick(); }

    @SubscribeEvent
    public void tags(TagsUpdatedEvent e) { Cat.clearCache(); }

    @SubscribeEvent
    public void tick(PlayerTickEvent.Post e) {
        if (e.getEntity() instanceof ServerPlayer sp) Engine.tick(sp);
    }

    @SubscribeEvent
    public void logout(PlayerEvent.PlayerLoggedOutEvent e) { Engine.forget(e.getEntity()); }

    /** Vanilla left click on an entity (clients without the mod, or a click the client did not intercept). */
    @SubscribeEvent
    public void attack(AttackEntityEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || !CombatConfig.ENABLED.getAsBoolean()) return;
        if (Cat.melee(sp.getMainHandItem()) == null) return;
        e.setCanceled(true);
        Engine.swing(sp);
    }

    @SubscribeEvent
    public void incoming(LivingIncomingDamageEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            if (Engine.dodging(sp, e.getSource())) { e.setCanceled(true); return; }
            if (Engine.parryCheck(sp, e.getSource())) { e.setCanceled(true); Engine.parried(sp, e.getSource()); }
        }
    }

    @SubscribeEvent
    public void loose(ArrowLooseEvent e) {
        if (!CombatConfig.RANGED_ON.getAsBoolean() || !(e.getEntity() instanceof ServerPlayer sp)) return;
        Cat c = Cat.bow(e.getBow());
        if (c == null) return;
        int age = Engine.ageOf(sp);
        e.setCharge(Engine.adjustCharge(c, age, e.getCharge()));
        PENDING.put(sp.getUUID(), new Object[]{sp.level().getGameTime(), Engine.dropReduction(c)});
    }

    @SubscribeEvent
    public void join(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof AbstractArrow a)) return;
        if (a.getOwner() instanceof ServerPlayer sp) {
            Object[] p = PENDING.remove(sp.getUUID());
            if (p != null && (long) p[0] == sp.level().getGameTime() && (float) p[1] > 0) TUNED.put(a, (float) p[1]);
        }
    }

    @SubscribeEvent
    public void arrowTick(EntityTickEvent.Post e) {
        if (e.getEntity() instanceof AbstractArrow a && !TUNED.isEmpty()) {
            Float r = TUNED.get(a);
            if (r != null && !a.isNoPhysics() && !a.onGround() && a.getDeltaMovement().lengthSqr() > 1e-4) {
                a.setDeltaMovement(a.getDeltaMovement().add(0, 0.05 * r, 0));
            }
        }
    }

}
