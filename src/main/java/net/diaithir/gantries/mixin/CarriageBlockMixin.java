package net.diaithir.gantries.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import net.diaithir.gantries.Split;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GantryCarriageBlock.class)
abstract class CarriageBlockMixin {
    @ModifyExpressionValue(method = {"canSurvive", "cycleAxisIfNecessary"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private Block diaithirgantries$split(Block b) {
        return Split.asShaft(b);
    }
}
