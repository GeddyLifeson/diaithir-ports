package io.github.tr100000.researcher.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeCraftingHolder.class)
public interface RecipeCraftingHolderMixin {
    @Inject(method = "setRecipeUsed(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/RecipeCraftingHolder;setRecipeUsed(Lnet/minecraft/world/item/crafting/RecipeHolder;)V"), cancellable = true)
    // Only used by crafting (as of 26.1)
    private void shouldCraftRecipe(ServerPlayer player, RecipeHolder<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!player.researcher$getPlayerTracker().canCraftRecipe(recipe.id().identifier())) {
            cir.setReturnValue(false);
        }
    }
}
