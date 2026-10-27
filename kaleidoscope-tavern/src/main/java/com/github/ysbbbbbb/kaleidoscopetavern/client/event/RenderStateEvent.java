package com.github.ysbbbbbb.kaleidoscopetavern.client.event;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModEffects;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.jspecify.annotations.NonNull;

import static com.github.ysbbbbbb.kaleidoscopetavern.effect.GrassStealthEffect.notInGrassStealthPlant;

@EventBusSubscriber(modid = KaleidoscopeTavern.MOD_ID, value = Dist.CLIENT)
public class RenderStateEvent {
    public static final ContextKey<Boolean> GRASS_STEALTH_HIDE = new ContextKey<>(KaleidoscopeTavern.modLoc("grass_stealth_hide"));

    @SubscribeEvent
    public static void onRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, @NonNull AvatarRenderState state) {
                if (avatar.hasEffect(ModEffects.GRASS_STEALTH) && avatar.isShiftKeyDown()) {
                    Level level = avatar.level();
                    BlockPos pos = avatar.blockPosition();
                    BlockPos abovePos = pos.above();
                    if (notInGrassStealthPlant(level, pos) && notInGrassStealthPlant(level, abovePos)) {
                        state.setRenderData(GRASS_STEALTH_HIDE, false);
                    } else {
                        state.setRenderData(GRASS_STEALTH_HIDE, true);
                    }
                } else {
                    state.setRenderData(GRASS_STEALTH_HIDE, false);
                }
            }
        });
    }
}
