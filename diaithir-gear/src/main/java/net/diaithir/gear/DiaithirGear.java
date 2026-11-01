package net.diaithir.gear;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

/** Gléaus (Diaithir Gear): cross-mod gear parity. Bullet damage parity, set bonuses, gem and affix crossover data. */
@Mod(DiaithirGear.ID)
public final class DiaithirGear {
    public static final String ID = "diaithirgear";
    public static final Logger LOG = LogUtils.getLogger();

    public DiaithirGear(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, GearConfig.SPEC);
        Parity.load();
        Sets.load();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void commands(RegisterCommandsEvent e) { SelfTest.register(e.getDispatcher()); }

    @SubscribeEvent
    public void incoming(LivingIncomingDamageEvent e) {
        e.setAmount(Parity.scaled(e.getSource(), e.getAmount()));
    }

    @SubscribeEvent
    public void tick(PlayerTickEvent.Post e) {
        if (e.getEntity() instanceof ServerPlayer sp && sp.tickCount % 20 == 7) Sets.update(sp);
    }

    @SubscribeEvent
    public void respawn(PlayerEvent.PlayerRespawnEvent e) { Sets.forget(e.getEntity()); }

    @SubscribeEvent
    public void dimension(PlayerEvent.PlayerChangedDimensionEvent e) { Sets.forget(e.getEntity()); }

    @SubscribeEvent
    public void logout(PlayerEvent.PlayerLoggedOutEvent e) { Sets.forget(e.getEntity()); }
}
