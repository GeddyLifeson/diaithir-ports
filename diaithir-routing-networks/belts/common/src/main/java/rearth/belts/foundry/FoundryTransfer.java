package rearth.belts.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rearth.belts.BlockEntitiesContent;
import rearth.belts.api.item.ItemApi;

public final class FoundryTransfer {
    
    private FoundryTransfer() {
    }
    
    // pushes into the block adjacent to source on the given side. Belt start chutes receive single items on a lane.
    public static int push(Level level, BlockPos source, Direction side, ItemStack stack, int lane, boolean simulate) {
        var targetPos = source.relative(side);
        if (stack.isEmpty() || !level.hasChunkAt(targetPos)) return 0;
        
        var chute = level.getBlockEntity(targetPos, BlockEntitiesContent.CHUTE_BLOCK.get());
        if (chute.isPresent())
            return chute.get().insertOnLane(stack.copyWithCount(1), lane, simulate);
        
        var inventory = ItemApi.BLOCK.find(level, targetPos, null, null, side.getOpposite());
        if (inventory == null || !inventory.supportsInsertion()) return 0;
        return inventory.insert(stack, simulate);
    }
    
    public static void drop(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) return;
        var center = pos.getCenter();
        level.addFreshEntity(new ItemEntity(level, center.x, center.y, center.z, stack));
    }
}
