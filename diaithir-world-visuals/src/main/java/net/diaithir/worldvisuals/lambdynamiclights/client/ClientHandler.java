package net.diaithir.worldvisuals.lambdynamiclights.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

public final class ClientHandler {
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("worldvisuals", "visuals"));
    private static final KeyMapping TOGGLE =
            new KeyMapping("key.worldvisuals.dynlights.toggle", InputConstants.UNKNOWN.getValue(), CATEGORY);
    private static final KeyMapping CYCLE =
            new KeyMapping("key.worldvisuals.dynlights.cycle", InputConstants.UNKNOWN.getValue(), CATEGORY);
    private static final String[] MODES = {"Off", "Fast", "Full"};

    private ClientHandler() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientHandler::onKeys);
        modBus.addListener(ClientHandler::onConfig);
        NeoForge.EVENT_BUS.addListener(ClientHandler::onTick);
        NeoForge.EVENT_BUS.addListener(ClientHandler::onLogout);
        NeoForge.EVENT_BUS.addListener(ClientHandler::onLogin);
        NeoForge.EVENT_BUS.addListener(ClientHandler::onLeave);
    }

    @SubscribeEvent
    private static void onKeys(RegisterKeyMappingsEvent e) {
        e.registerCategory(CATEGORY);
        e.register(TOGGLE);
        e.register(CYCLE);
    }

    private static void onConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == DynamicLightsConfig.SPEC) LuminanceResolver.refresh();
    }

    private static void onTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        while (TOGGLE.consumeClick()) {
            boolean on = DynamicLightEngine.toggle();
            msg(mc, "Dynamic lights: " + (on ? "on" : "off"));
        }
        while (CYCLE.consumeClick()) {
            msg(mc, "Dynamic lights quality: " + MODES[DynamicLightEngine.cycleMode()]);
        }
        DynamicLightEngine.tick();
    }

    private static void msg(Minecraft mc, String s) {
        if (mc.player != null) mc.player.sendOverlayMessage(Component.literal(s));
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        DynamicLightEngine.clear();
    }

    private static void onLogin(ClientPlayerNetworkEvent.LoggingIn e) {
        DynamicLightEngine.clear();
    }

    private static void onLeave(EntityLeaveLevelEvent e) {
        if (e.getLevel().isClientSide()) DynamicLightEngine.remove(e.getEntity().getId());
    }
}
