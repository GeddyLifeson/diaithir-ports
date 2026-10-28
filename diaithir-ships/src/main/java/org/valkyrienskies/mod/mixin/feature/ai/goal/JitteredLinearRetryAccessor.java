package org.valkyrienskies.mod.mixin.feature.ai.goal;

import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.entity.ai.behavior.AcquirePoi$JitteredLinearRetry")
public interface JitteredLinearRetryAccessor {
    @Invoker("<init>")
    static Object create(RandomSource randomSource, long l) {
        throw new AssertionError();
    }
}
