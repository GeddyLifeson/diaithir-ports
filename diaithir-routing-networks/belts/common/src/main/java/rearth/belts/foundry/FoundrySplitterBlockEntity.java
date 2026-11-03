package rearth.belts.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import rearth.belts.BlockEntitiesContent;
import rearth.belts.api.item.ItemApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FoundrySplitterBlockEntity extends BlockEntity implements FoundryInventoryHolder {
    
    private static final int LANES = 2;
    private static final int OUTPUTS = 2;
    private static final int SLOT_LIMIT = 64;
    private static final float MAX_BUDGET = 2f;
    private static final int BLOCKED_COOLDOWN = 2;
    
    // one buffered stack per input lane
    private final ItemStack[] buffers = {ItemStack.EMPTY, ItemStack.EMPTY};
    // index 0 = left output, 1 = right output
    private final ItemStack[] filters = {ItemStack.EMPTY, ItemStack.EMPTY};
    private final InputStorage inputStorage = new InputStorage();
    
    private FoundryPriority inputPriority = FoundryPriority.NONE;
    private FoundryPriority outputPriority = FoundryPriority.NONE;
    private int tier = FoundryTiers.MIN_TIER;
    
    private float budget;
    private int cooldown;
    private int nextLane;
    private int nextOutput;
    
    public FoundrySplitterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.FOUNDRY_SPLITTER.get(), pos, state);
    }
    
    public void tick(Level level, BlockPos pos, BlockState state) {
        budget = Math.min(budget + FoundryTiers.itemsPerSecond(tier) / 20f, MAX_BUDGET);
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
        
        for (var lane : laneOrder()) {
            var buffered = buffers[lane];
            if (buffered.isEmpty()) continue;
            
            var single = buffered.copyWithCount(1);
            for (var output : outputOrder(level, single)) {
                var side = output == 0 ? facing.getCounterClockWise() : facing.getClockWise();
                if (FoundryTransfer.push(level, pos, side, single, lane, false) <= 0) continue;
                
                buffered.shrink(1);
                if (buffered.isEmpty()) buffers[lane] = ItemStack.EMPTY;
                nextLane = 1 - lane;
                nextOutput = 1 - output;
                setChanged();
                return true;
            }
        }
        
        return false;
    }
    
    private int[] laneOrder() {
        var first = switch (inputPriority) {
            case LEFT -> 0;
            case RIGHT -> 1;
            case NONE -> nextLane;
        };
        return new int[]{first, 1 - first};
    }
    
    // outputs with a matching filter come first, then outputs without a filter. Filtered outputs that do not match are skipped.
    private List<Integer> outputOrder(Level level, ItemStack stack) {
        var matching = new ArrayList<Integer>(OUTPUTS);
        var open = new ArrayList<Integer>(OUTPUTS);
        for (int output = 0; output < OUTPUTS; output++) {
            if (filters[output].isEmpty()) {
                open.add(output);
            } else if (FoundryFilters.matches(filters[output], stack, level)) {
                matching.add(output);
            }
        }
        
        prefer(matching);
        prefer(open);
        matching.addAll(open);
        return matching;
    }
    
    private void prefer(List<Integer> outputs) {
        if (outputs.size() < 2) return;
        var first = switch (outputPriority) {
            case LEFT -> 0;
            case RIGHT -> 1;
            case NONE -> nextOutput;
        };
        if (outputs.getFirst() != first) Collections.reverse(outputs);
    }
    
    public void dropContent(Level level, BlockPos pos) {
        for (int lane = 0; lane < LANES; lane++) {
            FoundryTransfer.drop(level, pos, buffers[lane]);
            buffers[lane] = ItemStack.EMPTY;
        }
    }
    
    public void assignFilter(int output, ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            resetFilter(output, player);
            return;
        }
        
        player.sendSystemMessage(Component.translatable("message.belts.filter_set"));
        filters[output] = stack.copyWithCount(1);
        setChanged();
    }
    
    public void resetFilter(int output, Player player) {
        player.sendSystemMessage(Component.translatable("message.belts.filter_reset"));
        filters[output] = ItemStack.EMPTY;
        setChanged();
    }
    
    public void cycleInputPriority(Player player) {
        inputPriority = inputPriority.next();
        player.sendSystemMessage(Component.translatable("message.belts.foundry_input_priority",
          Component.translatable("message.belts.foundry_priority." + inputPriority.key())));
        setChanged();
    }
    
    public void cycleOutputPriority(Player player) {
        outputPriority = outputPriority.next();
        player.sendSystemMessage(Component.translatable("message.belts.foundry_output_priority",
          Component.translatable("message.belts.foundry_priority." + outputPriority.key())));
        setChanged();
    }
    
    public void upgrade(Player player, ItemStack stack) {
        tier = FoundryTiers.upgrade(tier, player, stack);
        setChanged();
    }
    
    @Override
    public @Nullable ItemApi.InventoryStorage getInventory(@Nullable Direction side) {
        var back = getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        return side == back ? inputStorage : null;
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("tier", tier);
        output.putInt("inputPriority", inputPriority.ordinal());
        output.putInt("outputPriority", outputPriority.ordinal());
        
        for (int i = 0; i < OUTPUTS; i++) {
            output.store("filter" + i, ItemStack.OPTIONAL_CODEC, filters[i]);
        }
        
        var stored = output.childrenList("buffer");
        for (int lane = 0; lane < LANES; lane++) {
            if (buffers[lane].isEmpty()) continue;
            var child = stored.addChild();
            child.putInt("lane", lane);
            child.store("stack", ItemStack.OPTIONAL_CODEC, buffers[lane]);
        }
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tier = FoundryTiers.clamp(input.getIntOr("tier", FoundryTiers.MIN_TIER));
        inputPriority = FoundryPriority.byOrdinal(input.getIntOr("inputPriority", FoundryPriority.NONE.ordinal()));
        outputPriority = FoundryPriority.byOrdinal(input.getIntOr("outputPriority", FoundryPriority.NONE.ordinal()));
        
        for (int i = 0; i < OUTPUTS; i++) {
            filters[i] = input.read("filter" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        }
        
        Arrays.fill(buffers, ItemStack.EMPTY);
        input.childrenListOrEmpty("buffer").stream().forEach(child -> {
            var lane = child.getIntOr("lane", 0);
            if (lane < 0 || lane >= LANES) return;
            buffers[lane] = child.read("stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        });
    }
    
    private final class InputStorage implements ItemApi.InventoryStorage {
        
        @Override
        public boolean supportsExtraction() {
            return false;
        }
        
        @Override
        public int insert(ItemStack inserted, boolean simulate) {
            for (int lane = 0; lane < LANES; lane++) {
                var accepted = insertToSlot(inserted, lane, simulate);
                if (accepted > 0) return accepted;
            }
            return 0;
        }
        
        @Override
        public int insertToSlot(ItemStack inserted, int slot, boolean simulate) {
            if (inserted.isEmpty() || slot < 0 || slot >= LANES || !buffers[slot].isEmpty() || inserted.getCount() > SLOT_LIMIT)
                return 0;
            
            if (!simulate) {
                buffers[slot] = inserted.copy();
                setChanged();
            }
            return inserted.getCount();
        }
        
        @Override
        public int extract(ItemStack extracted, boolean simulate) {
            return 0;
        }
        
        @Override
        public int extractFromSlot(ItemStack extracted, int slot, boolean simulate) {
            return 0;
        }
        
        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (slot < 0 || slot >= LANES) return;
            buffers[slot] = stack.copy();
            setChanged();
        }
        
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot < 0 || slot >= LANES ? ItemStack.EMPTY : buffers[slot].copy();
        }
        
        @Override
        public int getSlotCount() {
            return LANES;
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return SLOT_LIMIT;
        }
    }
}
