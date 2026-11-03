package net.diaithir.worldvisuals.flickerfix;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Field;

final class FlickerFixClient {
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("worldvisuals", "flickerfix"));
    private static final KeyMapping TOGGLE = new KeyMapping("key.flickerfix.toggle", InputConstants.UNKNOWN.getValue(), CATEGORY);

    private static Field durationField;
    private static boolean reflectionFailed;
    private static int lastDuration = Integer.MIN_VALUE;

    private FlickerFixClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener(FlickerFixClient::registerKeys);
        NeoForge.EVENT_BUS.register(new Handler());
    }

    private static void registerKeys(RegisterKeyMappingsEvent e) {
        e.registerCategory(CATEGORY);
        e.register(TOGGLE);
    }

    private static Field durationField() {
        if (durationField == null && !reflectionFailed) {
            try {
                for (Field f : MobEffectInstance.class.getDeclaredFields()) {
                    if (f.getType() == int.class && !java.lang.reflect.Modifier.isStatic(f.getModifiers())
                            && !java.lang.reflect.Modifier.isFinal(f.getModifiers())
                            && f.getName().toLowerCase().contains("duration")) {
                        f.setAccessible(true);
                        durationField = f;
                        break;
                    }
                }
            } catch (Throwable t) {
                reflectionFailed = true;
            }
            if (durationField == null) reflectionFailed = true;
        }
        return durationField;
    }

    public static final class Handler {
        @SubscribeEvent
        public void onTickPre(ClientTickEvent.Pre e) {
            if (!FlickerFixConfig.ENABLED.get()) return;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null || mc.getCameraEntity() != player) return;
            MobEffectInstance inst = player.getEffect(MobEffects.NIGHT_VISION);
            if (inst == null) {
                lastDuration = Integer.MIN_VALUE;
                return;
            }
            int duration = inst.getDuration();
            if (duration == MobEffectInstance.INFINITE_DURATION) return;
            if (!NightVisionMath.inFlickerWindow(duration)) return;
            Field f = durationField();
            if (f == null) return;
            boolean hold = FlickerFixConfig.MODE.get() == FlickerFixConfig.Mode.HOLD;
            int fade = FlickerFixConfig.FADE_TICKS.get();
            boolean fading = !hold && duration <= fade;
            if (fading) return;
            // Present a duration above the flicker threshold so vanilla's own scale is a constant 1.0.
            int visible = NightVisionMath.FLICKER_WINDOW + 1;
            try {
                f.setInt(inst, visible + 0 * duration);
            } catch (IllegalAccessException ex) {
                reflectionFailed = true;
            }
        }

        @SubscribeEvent
        public void onTickPost(ClientTickEvent.Post e) {
            Minecraft mc = Minecraft.getInstance();
            while (TOGGLE.consumeClick()) {
                boolean now = !FlickerFixConfig.ENABLED.get();
                FlickerFixConfig.ENABLED.set(now);
                if (mc.player != null) {
                    mc.player.sendOverlayMessage(Component.literal("FlickerFix: " + (now ? "ON" : "OFF")));
                }
            }
        }

        @SubscribeEvent
        public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
            lastDuration = Integer.MIN_VALUE;
        }
    }
}
