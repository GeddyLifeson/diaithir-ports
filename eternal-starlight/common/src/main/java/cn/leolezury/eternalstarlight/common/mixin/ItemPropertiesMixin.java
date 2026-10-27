package cn.leolezury.eternalstarlight.common.mixin;

import cn.leolezury.eternalstarlight.common.util.ESRegistering;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** PORT: fill in the item id from the registration context when the mod did not call setId. */
@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin {
	@Shadow
	private ResourceKey<Item> id;

	@Inject(method = "effectiveDescriptionId", at = @At("HEAD"), cancellable = true)
	private void eternalstarlight$blockPrefix(CallbackInfoReturnable<String> cir) {
		if (this.id == null) {
			ResourceKey<Item> current = ESRegistering.current();
			if (current != null && current.registryKey().equals(net.minecraft.core.registries.Registries.ITEM)) {
				this.id = current;
			}
		}
		// Items registered under the id of a block are BlockItems; vanilla gives them the block description id.
		if (this.id != null && net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(this.id.identifier())) {
			cir.setReturnValue(net.minecraft.util.Util.makeDescriptionId("block", this.id.identifier()));
		}
	}

	@Inject(method = "itemIdOrThrow", at = @At("HEAD"))
	private void eternalstarlight$fillId(CallbackInfoReturnable<?> cir) {
		if (this.id == null) {
			ResourceKey<Item> current = ESRegistering.current();
			if (current != null && current.registryKey().equals(net.minecraft.core.registries.Registries.ITEM)) {
				this.id = current;
			}
		}
	}
}
