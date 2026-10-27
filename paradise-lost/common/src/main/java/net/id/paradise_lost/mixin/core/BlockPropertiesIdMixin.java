package net.id.paradise_lost.mixin.core;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.registration.RegIdContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Supplies the registry id to mod blocks whose Properties were built without setId (26.1 requires it). */
@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesIdMixin {
    @Shadow
    private ResourceKey<Block> id;

    @Inject(method = {"effectiveDrops", "effectiveDescriptionId"}, at = @At("HEAD"))
    private void paradiseLost$fillId(CallbackInfoReturnable<?> cir) {
        String current = RegIdContext.CURRENT.get();
        if (this.id == null && current != null) {
            this.id = ResourceKey.create(Registries.BLOCK, ModConstants.locate(current));
        }
    }
}
