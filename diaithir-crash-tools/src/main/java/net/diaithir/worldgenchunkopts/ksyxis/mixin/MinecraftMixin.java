// Ported from Ksyxis (MIT) https://github.com/VidTu/Ksyxis
// Modifications: Mojang-mapped 26.1 target only, static handler, no logging/assert helpers.
package net.diaithir.worldgenchunkopts.ksyxis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Removes the fake 500 ms delay when creating/loading an integrated-server world. Client only. */
@Mixin(targets = "net.minecraft.client.Minecraft", remap = false)
public final class MinecraftMixin {
    private MinecraftMixin() {}

    @ModifyConstant(method = "doWorldLoad", constant = @Constant(longValue = 500L), remap = false, require = 0, expect = 0)
    private static long ksyxis_doWorldLoad_closeDelayMs(final long delay) {
        return 0L;
    }
}
