package net.diaithir.worldvisuals.betterclouds.mixin;

import net.diaithir.worldvisuals.betterclouds.CloudConfig;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, index = 1)
    private int diaithir$color(int color) {
        if (!CloudConfig.on()) return color;
        double o = CloudConfig.OPACITY.get();
        if (o >= 0.999) return color;
        int a = (int) (((color >>> 24) & 0xFF) * o);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float diaithir$height(float bottomY) {
        return CloudConfig.on() ? bottomY + CloudConfig.HEIGHT_OFFSET.get().floatValue() : bottomY;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int diaithir$range(int range) {
        return CloudConfig.on() ? Math.max(range, CloudConfig.MIN_DISTANCE_CHUNKS.get()) : range;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private long diaithir$speed(long gameTime) {
        return CloudConfig.on() ? (long) (gameTime * CloudConfig.SPEED.get()) : gameTime;
    }

    @ModifyConstant(method = "render", constant = @Constant(floatValue = 4.0F), require = 0)
    private float diaithir$thickness(float vanilla) {
        return CloudConfig.on() ? CloudConfig.THICKNESS.get().floatValue() : vanilla;
    }
}
