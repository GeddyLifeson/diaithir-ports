package net.diaithir.gantries.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import net.diaithir.gantries.Split;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GantryShaftBlock.class)
abstract class ShaftBlockMixin {
    @WrapOperation(method = {"updateShape", "getStateForPlacement", "onPlace"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean diaithirgantries$splitIs(BlockState state, Object asked, Operation<Boolean> original) {
        return Split.isShaft(original.call(state, asked), state, asked);
    }
}
