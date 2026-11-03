package net.diaithir.gantries.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import java.util.Queue;
import java.util.Set;
import net.diaithir.gantries.GantrySplitBlock;
import net.diaithir.gantries.Split;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Contraption.class)
abstract class ContraptionMixin {
    /** A carriage and the shafts it rides count a split as a shaft when a contraption is gathered or a pinion is moved. */
    @WrapOperation(method = {"moveBlock", "moveGantryPinion"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean diaithirgantries$splitIs(BlockState state, Object asked, Operation<Boolean> original) {
        return Split.isShaft(original.call(state, asked), state, asked);
    }

    /** A plain shaft still pulls a split next to it along its axis; a split pulls nothing (that is what cuts the gantry into runs). */
    @Inject(method = "moveGantryShaft", at = @At("HEAD"))
    private void diaithirgantries$stickToSplit(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state, CallbackInfo ci) {
        if (state.getBlock() instanceof GantrySplitBlock) return;
        Direction facing = state.getValue(GantryShaftBlock.FACING);
        for (Direction d : Direction.values()) {
            if (d.getAxis() != facing.getAxis()) continue;
            BlockPos offset = pos.relative(d);
            if (visited.contains(offset)) continue;
            BlockState o = world.getBlockState(offset);
            if (o.getBlock() instanceof GantrySplitBlock && o.getValue(GantryShaftBlock.FACING) == facing) frontier.add(offset);
        }
    }
}
