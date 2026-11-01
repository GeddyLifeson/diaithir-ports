package com.kyanite.deeperdarker.mixin;

import com.kyanite.deeperdarker.util.DDTags;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ancient paintings keep their variant when broken or picked (vanilla paintings drop a blank painting item). */
@SuppressWarnings("unused")
@Mixin(Painting.class)
public abstract class PaintingMixin extends HangingEntity {
    protected PaintingMixin(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract Holder<PaintingVariant> getVariant();

    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    public void deeperdarker$dropItem(ServerLevel level, @Nullable Entity causedBy, CallbackInfo ci) {
        if(getVariant().is(DDTags.Misc.ANCIENT_PAINTING) && level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1, 1);
            if(causedBy instanceof Player player && player.hasInfiniteMaterials()) {
                ci.cancel();
                return;
            }
            ItemStack stack = new ItemStack(Items.PAINTING);
            stack.set(DataComponents.PAINTING_VARIANT, this.getVariant());
            this.spawnAtLocation(level, stack);
            ci.cancel();
        }
    }

    @Inject(method = "getPickResult", at = @At("RETURN"), cancellable = true)
    public void deeperdarker$getPickResult(CallbackInfoReturnable<ItemStack> cir) {
        if(getVariant().is(DDTags.Misc.ANCIENT_PAINTING)) {
            ItemStack stack = new ItemStack(Items.PAINTING);
            stack.set(DataComponents.PAINTING_VARIANT, this.getVariant());
            cir.setReturnValue(stack);
        }
    }
}
