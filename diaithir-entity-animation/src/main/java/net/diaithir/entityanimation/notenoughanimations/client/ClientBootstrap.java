package net.diaithir.entityanimation.notenoughanimations.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public final class ClientBootstrap {
    static KeyMapping toggleKey;
    static KeyMapping configKey;
    static ModContainer modContainer;

    private ClientBootstrap() {}

    public static void init(IEventBus modBus, ModContainer container) {
        modContainer = container;
        container.registerConfig(ModConfig.Type.CLIENT, AnimationConfig.SPEC, "diaithir_entityanimation-client.toml");
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (mc, parent) -> new net.neoforged.neoforge.client.gui.ConfigurationScreen(container, parent));
    }

    static void openConfig() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.screen;
        mc.setScreen(new net.neoforged.neoforge.client.gui.ConfigurationScreen(modContainer, parent));
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static final class ModBusEvents {
        @SubscribeEvent
        public static void onKeys(RegisterKeyMappingsEvent e) {
            KeyMapping.Category cat = new KeyMapping.Category(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("diaithir_entityanimation", "main"));
            toggleKey = new KeyMapping("key.diaithir_entityanimation.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, cat);
            configKey = new KeyMapping("key.diaithir_entityanimation.config", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), cat);
            e.registerCategory(cat);
            e.register(toggleKey);
            e.register(configKey);
        }

        @SubscribeEvent
        public static void onConfig(ModConfigEvent.Reloading e) {
            if (e.getConfig().getSpec() == AnimationConfig.SPEC) AnimationData.rebuildOverrides();
        }

        @SubscribeEvent
        public static void onLoad(ModConfigEvent.Loading e) {
            if (e.getConfig().getSpec() == AnimationConfig.SPEC) AnimationData.rebuildOverrides();
        }
    }
}
