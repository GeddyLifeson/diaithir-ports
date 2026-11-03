package net.diaithir.entityanimation.notenoughanimations.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public final class ClientEvents {
    static final double MAX_DIST_SQ = 48.0 * 48.0;
    static final boolean FOREIGN = ModList.get().isLoaded("playeranimator")
            || ModList.get().isLoaded("epicfight") || ModList.get().isLoaded("bettercombat");

    private ClientEvents() {}

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        while (ClientBootstrap.toggleKey != null && ClientBootstrap.toggleKey.consumeClick()) {
            boolean v = !AnimationConfig.ENABLED.get();
            AnimationConfig.ENABLED.set(v);
            mc.gui.setOverlayMessage(Component.literal("Entity animations: " + (v ? "on" : "off")), false);
        }
        while (ClientBootstrap.configKey != null && ClientBootstrap.configKey.consumeClick()) {
            ClientBootstrap.openConfig();
        }

        if (!AnimationConfig.ENABLED.get() || FOREIGN) return;
        boolean others = AnimationConfig.APPLY_TO_OTHER_PLAYERS.get();
        for (AbstractClientPlayer p : mc.level.players()) {
            if (p != mc.player && !others) continue;
            if (p.distanceToSqr(mc.player) > MAX_DIST_SQ) continue;
            AnimationData.tick(p);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        AnimationData.clear();
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload e) {
        if (e.getLevel().isClientSide()) AnimationData.clear();
    }
}
