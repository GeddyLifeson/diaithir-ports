// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: client bootstrap; play/tick hooks via NeoForge events, remaining engine hooks (stop/destroy/loadLibrary/init) via mixin into SoundEngine.
package net.diaithir.audio.soundphysics.client;

import com.mojang.blaze3d.audio.Channel;
import java.util.List;
import java.util.Map;
import net.diaithir.audio.soundphysics.RaycastingHelper;
import net.diaithir.audio.soundphysics.client.mixin.ChannelAccessor;
import net.diaithir.audio.soundphysics.client.mixin.ChannelHandleAccessor;
import net.diaithir.audio.soundphysics.client.mixin.SoundEngineAccessor;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig.DebugType;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig.LegacyReverb;
import net.diaithir.audio.soundphysics.data.SoundData;
import net.diaithir.audio.soundphysics.wrappers.RedPermeatedSoundInstance;
import net.diaithir.audio.soundphysics.wrappers.RedPositionedSoundInstance;
import net.diaithir.audio.soundphysics.wrappers.RedSoundInstance;
import net.diaithir.audio.soundphysics.wrappers.RedTickableInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.diaithir.audio.soundphysics.RaycastingHelper.*;

public final class SoundPhysicsClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("diaithir_audio_soundphysics");

    private static volatile SoundEngine engine;
    private static boolean wasPaused = false;

    private SoundPhysicsClient() {}

    public static void init(IEventBus modBus, ModContainer container) {
        // NOTE: the SoundEngine instance capture, stop/stopAll/destroy/emergencyShutdown/loadLibrary hooks and the
        // instanceToChannel wrapper have no NeoForge event; they live in net.diaithir.audio.soundphysics.client.mixin.*
        // and must be declared in diaithir_audio_soundphysics.mixins.json (see resources).
        NeoForge.EVENT_BUS.register(new Handler());
    }

    // ---- called from SoundEngineMixin ----

    public static void setEngine(SoundEngine e) {
        engine = e;
    }

    public static void onAudioEngineStart() {
        fxHandler.efxInitialized = false;
        fxHandler.initializeReverb();
    }

    public static void onEngineShutdown(String where) {
        try {
            fxHandler.cleanupEFXResources();
        } catch (Throwable t) {
            LOGGER.error("Error during EFX cleanup on {}", where, t);
        }
        try {
            RaycastingHelper.shutdown();
        } catch (Throwable t) {
            LOGGER.error("Error during RaycastingHelper.shutdown() on {}", where, t);
        }
    }

    public static void onStopAll() {
        soundQueue.clear();
    }

    public static void onStop(SoundInstance sound, Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel) {
        if (!fxHandler.efxInitialized) return;
        if (sound == null) return;
        try {
            soundQueue.removeIf(d -> d.sound == sound || (d.sound instanceof RedSoundInstance r && r.original == sound));
            SoundInstance customSound = soundInstanceMap.get(sound);
            RedPermeatedSoundInstance soundPermeation = soundPermInstanceMap.get(sound);
            if (soundPermeation != null) soundPermeation.setStopped(true);
            soundInstanceMap.remove(sound);
            soundPermInstanceMap.remove(sound);

            if (soundPermeation != null) {
                ChannelAccess.ChannelHandle h = instanceToChannel.get(soundPermeation);
                if (h != null) h.execute(Channel::stop);
            }
            if (customSound != null) {
                ChannelAccess.ChannelHandle h = instanceToChannel.get(customSound);
                if (h != null) h.execute(Channel::stop);
            }
        } catch (Exception e) {
            LOGGER.debug("Error stopping wrapped sound", e);
        }
    }

    // ---- NeoForge event handlers ----

    public static final class Handler {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPlay(PlaySoundEvent event) {
            SoundInstance sound = event.getSound();
            if (sound == null || event.getEngine() == null) return;
            setEngine(event.getEngine());

            if (!fxHandler.efxInitialized) fxHandler.initializeReverb();
            if (!fxHandler.efxInitialized) return;
            if (isSoundBlacklisted(sound.toString())) return;

            Minecraft client = Minecraft.getInstance();
            if (client == null || client.player == null || client.level == null) return;

            if (SoundPhysicsConfig.DISABLE_FX_ON_UI.get() && sound.getSource() == SoundSource.MASTER) return;

            try {
                if (sound instanceof RedPermeatedSoundInstance perm) {
                    if (SoundPhysicsConfig.PERMEATION.get()) FXQueue.add(perm);
                    return;
                }
                if (sound instanceof RedPositionedSoundInstance || sound instanceof RedTickableInstance) return;
                if (sound instanceof RedSoundInstance) return;
                if (sound.getAttenuation() == SoundInstance.Attenuation.NONE) return;

                // resolve pitches/sound sets before positional data is read
                sound.resolve(client.getSoundManager());

                Vec3 soundPos = new Vec3(sound.getX(), sound.getY(), sound.getZ());
                int range = SoundPhysicsConfig.PROC_RANGE.get();
                if (range != -1 && soundPos.subtract(playerEyePos).length() > range) return;

                if (soundQueue.size() > SoundPhysicsConfig.MAX_SOUNDS.get()) return;

                RedSoundInstance red = new RedSoundInstance(sound);
                SoundData data = new SoundData(red, soundPos, sound.getIdentifier().toString());
                soundQueue.offer(data);
                notProcd.add(sound);

                // cancel the vanilla play; the wrapper is played by playQueuedObjects once rays are resolved
                event.setSound(null);
            } catch (Exception e) {
                LOGGER.error("Error tracking sound: {}", e.getMessage());
            }
        }

        @SubscribeEvent
        public void onClientTick(ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            if (client.level == null || client.player == null) return;

            RaycastingHelper.castBouncingRaysAndDetectSFX(client.level, client.player);
            RaycastingHelper.playQueuedObjects();

            if (!fxHandler.efxInitialized) fxHandler.initializeReverb();
            if (!fxHandler.efxInitialized) return;

            if (client.isPaused()) return;

            SoundEngine eng = engine;
            if (eng == null) return;
            Map<SoundInstance, ChannelAccess.ChannelHandle> map = ((SoundEngineAccessor) eng).spp$getInstanceToChannel();

            while (!FXQueue.isEmpty()) {
                try {
                    RedPermeatedSoundInstance sound = FXQueue.poll();
                    if (sound == null) continue;
                    ChannelAccess.ChannelHandle handle = map.get(sound);
                    if (handle == null) continue;
                    Channel channel = ((ChannelHandleAccessor) handle).spp$getChannel();
                    if (channel == null) continue;
                    int id = ((ChannelAccessor) channel).spp$getSource();
                    sound.setSource(id);
                    fxHandler.applyMuffleToSource(id, 1 - sound.getPermeationIndex());
                } catch (Exception e) {
                    if (SoundPhysicsConfig.DEBUG.get() != DebugType.OFF)
                        LOGGER.debug("sourceID is invalid for a sound, non-issue", e);
                }
            }

            if (SoundPhysicsConfig.REVERB.get()) updateActiveSources();
        }
    }

    private static boolean isSoundBlacklisted(String soundName) {
        if (soundName == null || soundName.isEmpty()) return false;
        List<? extends String> blacklist = SoundPhysicsConfig.SOUND_BLACKLIST.get();
        if (blacklist == null || blacklist.isEmpty()) return false;
        String lower = soundName.toLowerCase();
        return blacklist.stream()
                .filter(e -> e != null && !e.trim().isEmpty())
                .anyMatch(e -> lower.contains(e.toLowerCase().trim()));
    }

    /** Brute-force reverb on all currently playing OpenAL sources. */
    private static void updateActiveSources() {
        long context = ALC10.alcGetCurrentContext();
        if (context == 0) return;

        AL10.alGetError();

        try {
            LegacyReverb legacy = SoundPhysicsConfig.LEGACY_REVERB.get();
            for (int sourceId = 1; sourceId <= 256; sourceId++) {
                if (AL10.alIsSource(sourceId)) {
                    int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
                    if (state == AL10.AL_PLAYING || state == AL10.AL_PAUSED) {
                        if (legacy == LegacyReverb.VERSION140) fxHandler.applyLegacyReverbToSource(sourceId);
                        else if (legacy == LegacyReverb.VERSION100) fxHandler.applyInitalLegacyReverbToSource(sourceId);
                        else fxHandler.applyReverbToSource(sourceId);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
