package io.github.tr100000.researcher.mixin;

import io.github.tr100000.researcher.ResearcherCriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "onCraftedBy", at = @At("TAIL"))
    public void onCraftedByPlayer(ItemStack itemStack, Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            ResearcherCriteriaTriggers.ITEM_CRAFTED.trigger(serverPlayer, itemStack);
        }
    }
}
