package org.dimdev.dimdoors.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.dimdev.dimdoors.block.BlockIdHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesMixin {
    @Shadow
    private ResourceKey<Block> id;

    @Inject(method = "effectiveDrops", at = @At("HEAD"), require = 0)
    private void dimdoors$fillIdDrops(CallbackInfoReturnable<?> cir) {
        if (this.id == null && BlockIdHolder.CURRENT != null) this.id = BlockIdHolder.CURRENT;
    }

    @Inject(method = "effectiveDescriptionId", at = @At("HEAD"), require = 0)
    private void dimdoors$fillIdDesc(CallbackInfoReturnable<?> cir) {
        if (this.id == null && BlockIdHolder.CURRENT != null) this.id = BlockIdHolder.CURRENT;
    }
}
