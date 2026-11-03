// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: Lombok removed, config via ModConfigSpec.
package net.diaithir.audio.soundphysics.wrappers;

import net.diaithir.audio.soundphysics.RaycastingHelper;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;

public class RedPermeatedSoundInstance extends RedTickableInstance {
    private float permeationIndex;
    private int id;
    private boolean sourceSet = false;
    private float targetMuffle;

    public RedPermeatedSoundInstance(Identifier soundID, Sound sound, SoundSource category, Vec3 position, float volume, float pitch, SoundInstance wrapped, float permeationIndex, float attenuationMultiplier) {
        super(soundID, sound, category, position, volume, pitch, wrapped, attenuationMultiplier);
        this.permeationIndex = permeationIndex;
        this.targetMuffle = permeationIndex;
        super.updatePos();
        super.updateVolume();
        updateMuffle();
    }

    public float getPermeationIndex() {
        return permeationIndex;
    }

    public void setPermeationIndex(float permeationIndex) {
        if (super.isStopped()) return;
        this.targetMuffle = permeationIndex;
    }

    @Override
    public void tick() {
        super.updateWrapped();
        tickCount++;
        int rate = SoundPhysicsConfig.TICK_RATE.get();
        if (super.isStopped() || rate == 0) return;
        if (tickCount % rate == 0 && !RaycastingHelper.permeatedTickQueue.contains(this)) {
            RaycastingHelper.permeatedTickQueue.add(this);
        }
        super.updatePos();
        super.updateVolume();
        super.updatePitch();
        updateMuffle();
    }

    public void updateMuffle() {
        float deltaVolume = targetMuffle - permeationIndex;
        int rate = Math.max(1, SoundPhysicsConfig.TICK_RATE.get());
        float maxVolumeChange = Math.max(Math.abs(deltaVolume / rate), 0.3f);

        if (Math.abs(deltaVolume) <= 0.001f || Math.abs(deltaVolume) > maxVolumeChange * rate) {
            permeationIndex = targetMuffle;
            if (sourceSet && AL10.alIsSource(id)) {
                RaycastingHelper.fxHandler.applyMuffleToSource(id, 1 - permeationIndex);
            }
            return;
        }

        float volumeChange = Math.min(maxVolumeChange, Math.abs(deltaVolume));
        if (deltaVolume > 0) {
            permeationIndex += volumeChange;
        } else {
            permeationIndex -= volumeChange;
        }

        if (sourceSet && AL10.alIsSource(id)) {
            RaycastingHelper.fxHandler.applyMuffleToSource(id, 1 - permeationIndex);
        }
    }

    public void setSource(int id) {
        sourceSet = true;
        this.id = id;
    }
}
