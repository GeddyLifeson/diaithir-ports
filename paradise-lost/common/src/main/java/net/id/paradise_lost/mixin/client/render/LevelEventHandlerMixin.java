package net.id.paradise_lost.mixin.client.render;

import net.id.paradise_lost.client.rendering.util.ParadiseLostEvents;
import net.id.paradise_lost.registry.ItemRegistry;
import net.id.paradise_lost.util.ParadiseLostSoundEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelEventHandler.class)
public abstract class LevelEventHandlerMixin {

    @Shadow
    @Final
    private ClientLevel level;

    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    public void paradiseLost$processLevelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (eventId != ParadiseLostEvents.NITRA_EXPLODE) {
            return;
        }
        RandomSource random = this.level.getRandom();
        this.level.playLocalSound(pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 0.2F, 1.0F + random.nextFloat() * 0.4F, false);
        this.level.playLocalSound(pos, ParadiseLostSoundEvents.ENTITY_NITRA_EXPLODE, SoundSource.NEUTRAL, 2.0F, 0.5F + random.nextFloat() * 0.4F, false);
        for (int i = 0; i < 4; i++) {
            this.level.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(),
                    random.nextDouble() * 0.05,
                    random.nextDouble() * 0.05,
                    random.nextDouble() * 0.05
            );
        }
        for (int i = 0; i < 8; i++) {
            this.level.addParticle(
                    new ItemParticleOption(ParticleTypes.ITEM, ItemRegistry.NITRA_BULB.get()),
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5,
                    random.nextGaussian() * 0.15,
                    random.nextDouble() * 0.2,
                    random.nextGaussian() * 0.15
            );
        }
        ci.cancel();
    }
}
