package net.diaithir.entityanimation.skinlayers3d.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientSetup {
    private static final KeyMapping TOGGLE = new KeyMapping("key.diaithir_skinlayers3d.toggle",
            InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC);
    private static final KeyMapping CYCLE = new KeyMapping("key.diaithir_skinlayers3d.cycle_lod",
            InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC);
    private static final double[] LODS = {8.0, 16.0, 32.0};
    private static final String[] LOD_NAMES = {"Near", "Medium", "Far"};

    private ClientSetup() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(ClientSetup::onKeys);
        modBus.addListener(ClientSetup::onReloadListeners);
        modBus.addListener(ClientSetup::onAddLayers);
        modBus.addListener(ClientSetup::onConfigReload);
        NeoForge.EVENT_BUS.addListener(ClientSetup::onTick);
        NeoForge.EVENT_BUS.addListener(ClientSetup::onLogout);
        RenderContext.registerEvents(NeoForge.EVENT_BUS);
    }

    private static void onKeys(RegisterKeyMappingsEvent e) {
        e.register(TOGGLE);
        e.register(CYCLE);
    }

    private static void onReloadListeners(AddClientReloadListenersEvent e) {
        e.addListener(Identifier.fromNamespaceAndPath("diaithir_entity_animation", "skin_layers_cache"),
                (ResourceManagerReloadListener) (ResourceManager rm) -> MeshCache.clear());
    }

    private static void onAddLayers(EntityRenderersEvent.AddLayers e) {
        VoxelLayer.attach(e);
    }

    private static void onConfigReload(ModConfigEvent.Reloading e) {
        MeshCache.clear();
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        MeshCache.clear();
    }

    private static void onTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        while (TOGGLE.consumeClick()) {
            boolean v = !SkinLayersConfig.ENABLED.get();
            SkinLayersConfig.ENABLED.set(v);
            if (mc.player != null)
                mc.gui.setOverlayMessage(Component.literal("3D skin layers: " + (v ? "on" : "off")), false);
        }
        while (CYCLE.consumeClick()) {
            double cur = SkinLayersConfig.LOD_DISTANCE.get();
            int idx = 0;
            for (int i = 0; i < LODS.length; i++) if (cur >= LODS[i] - 0.01) idx = i + 1;
            idx %= LODS.length;
            SkinLayersConfig.LOD_DISTANCE.set(LODS[idx]);
            if (mc.player != null)
                mc.gui.setOverlayMessage(Component.literal("3D skin layer LOD: " + LOD_NAMES[idx]
                        + " (" + (int) LODS[idx] + " blocks)"), false);
        }
    }
}
