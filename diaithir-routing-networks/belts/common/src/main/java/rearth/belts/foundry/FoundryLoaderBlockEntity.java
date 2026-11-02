package rearth.belts.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import rearth.belts.BlockEntitiesContent;
import rearth.belts.api.item.ItemApi;

public class FoundryLoaderBlockEntity extends BlockEntity {
    
    private static final float MAX_BUDGET = 2f;
    private static final int BLOCKED_COOLDOWN = 2;
    
    private ItemStack filter = ItemStack.EMPTY;
    
    private float budget;
    private int cooldown;
    private int nextLane;
    
    public FoundryLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.FOUNDRY_LOADER.get(), pos, state);
    }
    
    public void tick(Level level, BlockPos pos, BlockState state) {
        budget = Math.min(budget + FoundryTiers.itemsPerSecond(state.getValue(FoundryLoaderBlock.TIER)) / 20f, MAX_BUDGET);
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        
        while (budget >= 1f) {
            if (!moveOneItem(level, pos, state)) {
                cooldown = BLOCKED_COOLDOWN;
                budget = Math.min(budget, 1f);
                return;
            }
            budget -= 1f;
        }
    }
    
    private boolean moveOneItem(Level level, BlockPos pos, BlockState state) {
        var facing = state.getValue(HorizontalDirectionalBlock.FACING);
        var sourcePos = pos.relative(facing.getOpposite());
        if (!level.hasChunkAt(sourcePos)) return false;
        
        var source = ItemApi.BLOCK.find(level, sourcePos, null, null, facing);
        if (source == null || !source.supportsExtraction()) return false;
        
        for (int slot = 0; slot < source.getSlotCount(); slot++) {
            var available = source.getStackInSlot(slot);
            if (available.isEmpty() || !FoundryFilters.matches(filter, available, level)) continue;
            
            var single = available.copyWithCount(1);
            for (var lane : laneOrder(state.getValue(FoundryLoaderBlock.LANE))) {
                if (FoundryTransfer.push(level, pos, facing, single, lane, true) <= 0) continue;
                if (source.extract(single, false) <= 0) break;
                
                FoundryTransfer.push(level, pos, facing, single, lane, false);
                nextLane = 1 - lane;
                setChanged();
                return true;
            }
        }
        
        return false;
    }
    
    private int[] laneOrder(FoundryLaneMode mode) {
        return switch (mode) {
            case LEFT -> new int[]{0};
            case RIGHT -> new int[]{1};
            case AUTO -> new int[]{nextLane, 1 - nextLane};
        };
    }
    
    public void assignFilter(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            resetFilter(player);
            return;
        }
        
        player.sendSystemMessage(Component.translatable("message.belts.filter_set"));
        filter = stack.copyWithCount(1);
        setChanged();
    }
    
    public void resetFilter(Player player) {
        player.sendSystemMessage(Component.translatable("message.belts.filter_reset"));
        filter = ItemStack.EMPTY;
        setChanged();
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("filter", ItemStack.OPTIONAL_CODEC, filter);
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        filter = input.read("filter", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
