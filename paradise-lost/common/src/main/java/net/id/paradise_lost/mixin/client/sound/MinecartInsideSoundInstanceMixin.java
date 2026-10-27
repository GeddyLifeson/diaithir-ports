package net.id.paradise_lost.mixin.client.sound;

import net.id.paradise_lost.util.MinecartFloatingHelper;
import net.minecraft.client.resources.sounds.RidingEntitySoundInstance;
import net.minecraft.client.resources.sounds.RidingMinecartSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mutes the riding rolling sound while a levita cart floats off the rails. RidingMinecartSoundInstance does not
 * declare tick() itself in 26.1, so this mixin adds an override that defers to the parent implementation.
 */
@Mixin(RidingMinecartSoundInstance.class)
public abstract class MinecartInsideSoundInstanceMixin extends RidingEntitySoundInstance {

    @Shadow
    @Final
    private AbstractMinecart minecart;

    protected MinecartInsideSoundInstanceMixin(Player player, Entity entity, boolean underwaterSound, SoundEvent soundEvent, SoundSource soundSource, float volumeMin, float volumeMax, float volumeAmplifier) {
        super(player, entity, underwaterSound, soundEvent, soundSource, volumeMin, volumeMax, volumeAmplifier);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.minecart.isRemoved()) {
            if (MinecartFloatingHelper.isFloating(this.minecart) && !MinecartFloatingHelper.isCartOnRail(this.minecart)) {
                this.volume = 0.0F;
            }
        }
    }
}
