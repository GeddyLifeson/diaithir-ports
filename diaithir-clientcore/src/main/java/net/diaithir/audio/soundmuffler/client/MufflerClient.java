// Ported from Extreme Sound Muffler (LGPL-3.0-only) https://github.com/LeoBeliik/ExtremeSoundMuffler
// Modifications: NeoForge SoundEvent.SoundSourceEvent / PlaySoundEvent replace the SoundEngine mixin; keybind via RegisterKeyMappingsEvent.
package net.diaithir.audio.soundmuffler.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public final class MufflerClient {
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("diaithiraudio", "soundmuffler"));
    private static final KeyMapping OPEN = new KeyMapping("key.diaithiraudio.open_muffler_gui",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY);

    /** Set while the GUI is previewing a sound so the preview is never muffled or recorded. */
    public static volatile boolean previewing;

    private MufflerClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, e -> e.register(OPEN));
        modBus.addListener(ModConfigEvent.Loading.class, e -> {
            if (e.getConfig().getSpec() == MufflerConfig.SPEC) MufflerState.load();
        });
        modBus.addListener(ModConfigEvent.Reloading.class, e -> {
            if (e.getConfig().getSpec() == MufflerConfig.SPEC) MufflerState.load();
        });
        NeoForge.EVENT_BUS.register(new Handlers());
    }

    public static void playPreview(String id) {
        Identifier rl = Identifier.tryParse(id);
        if (rl == null) return;
        Minecraft mc = Minecraft.getInstance();
        previewing = true;
        try {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(
                    net.minecraft.sounds.SoundEvent.createVariableRangeEvent(rl), 1.0F));
        } finally {
            previewing = false;
        }
    }

    public static final class Handlers {
        @SubscribeEvent
        public void onTick(ClientTickEvent.Post e) {
            Minecraft mc = Minecraft.getInstance();
            while (OPEN.consumeClick()) {
                if (mc.screen == null) {
                    MufflerState.ensureLoaded();
                    mc.setScreen(new MufflerScreen());
                }
            }
        }

        @SubscribeEvent
        public void onPlay(PlaySoundEvent e) {
            if (previewing) return;
            SoundInstance sound = e.getSound();
            if (sound == null) return;
            Identifier id = sound.getIdentifier();
            if (id == null) return;
            MufflerState.ensureLoaded();
            String idStr = id.toString();
            if (!MufflerState.isForbidden(idStr)) {
                MufflerState.recordRecent(idStr);
            }
            if (!MufflerState.isEnabled()) return;
            Double vol = MufflerState.volumeFor(idStr, id.getNamespace());
            if (vol == null) return;
            if (vol <= 0.0D) {
                e.setSound(null);
                return;
            }
            e.setSound(new ScaledSound(sound, vol.floatValue()));
        }

        @SubscribeEvent
        public void onLogin(ClientPlayerNetworkEvent.LoggingIn e) {
            MufflerState.load();
        }

        @SubscribeEvent
        public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
            MufflerState.save();
            MufflerState.clearRecent();
        }
    }
}
