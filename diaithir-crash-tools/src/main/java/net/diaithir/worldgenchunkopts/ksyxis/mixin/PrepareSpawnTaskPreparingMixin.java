// Ported from Ksyxis (MIT) https://github.com/VidTu/Ksyxis
// Modifications: Mojang-mapped 26.1 target only, static handler, no logging/assert helpers.
package net.diaithir.worldgenchunkopts.ksyxis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Uses a zero-level chunk ticket instead of level 3 when preparing the player spawn. */
@Mixin(targets = "net.minecraft.server.network.config.PrepareSpawnTask$Preparing", remap = false)
@Pseudo
public final class PrepareSpawnTaskPreparingMixin {
    private PrepareSpawnTaskPreparingMixin() {}

    @ModifyConstant(method = "lambda$tick$0(Lnet/minecraft/world/level/ChunkPos;)V", constant = @Constant(intValue = 3), remap = false, require = 0, expect = 0)
    private static int ksyxis_lambdaTick0_addTicketAndLoadWithRadius(final int ticket) {
        return 0;
    }
}
