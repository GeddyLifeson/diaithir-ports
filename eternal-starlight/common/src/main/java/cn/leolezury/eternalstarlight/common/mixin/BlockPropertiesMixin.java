package cn.leolezury.eternalstarlight.common.mixin;

import cn.leolezury.eternalstarlight.common.util.ESRegistering;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** PORT: fill in the block id from the registration context when the mod did not call setId. */
@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesMixin {
	@Shadow
	private ResourceKey<Block> id;

	@Inject(method = {"effectiveDrops", "effectiveDescriptionId"}, at = @At("HEAD"))
	private void eternalstarlight$fillId(CallbackInfoReturnable<?> cir) {
		if (this.id == null) {
			ResourceKey<Block> current = ESRegistering.current();
			if (current != null && current.registryKey().equals(net.minecraft.core.registries.Registries.BLOCK)) {
				this.id = current;
			}
		}
	}
}
