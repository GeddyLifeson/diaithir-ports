package com.kyanite.deeperdarker.mixin;

import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("unused")
@Mixin(PhantomRenderState.class)
public class PhantomRenderStateMixin implements com.kyanite.deeperdarker.util.SculkPhantomFlag {
    @Unique
    private boolean deeperdarker$sculk;

    @Override
    public boolean deeperdarker$isSculk() {
        return deeperdarker$sculk;
    }

    @Override
    public void deeperdarker$setSculk(boolean value) {
        deeperdarker$sculk = value;
    }
}
