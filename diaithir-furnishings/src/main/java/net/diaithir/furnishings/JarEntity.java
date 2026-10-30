package net.diaithir.furnishings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.ArrayList;
import java.util.List;

/** One kind of item, up to {@link #STACKS} stacks of it. The picked-up jar carries the contents as ordinary stacks. */
public final class JarEntity extends BlockEntity {
    public static final int STACKS = 4;

    private ItemStack kind = ItemStack.EMPTY;
    private int count;

    public JarEntity(BlockPos pos, BlockState state) {
        super(DiaithirFurnishings.JAR_ENTITY.get(), pos, state);
    }

    public ItemStack kind() {
        return kind;
    }

    public int count() {
        return count;
    }

    public int capacity() {
        return kind.isEmpty() ? 0 : kind.getMaxStackSize() * STACKS;
    }

    /** Only stackable things that are not themselves containers: the jar holds a count, not a list of different stacks. */
    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && stack.getMaxStackSize() > 1 && !stack.has(DataComponents.CONTAINER)
            && !(stack.getItem() instanceof net.minecraft.world.item.BlockItem bi
                 && (bi.getBlock() instanceof JarBlock || bi.getBlock() instanceof net.diaithir.furnishings.SackBlock));
    }

    /** Takes as much of the stack as fits, returns how many it took. */
    public int insert(ItemStack stack) {
        if (!accepts(stack)) {
            return 0;
        }
        if (count > 0 && !ItemStack.isSameItemSameComponents(kind, stack)) {
            return 0;
        }
        int room = count == 0 ? stack.getMaxStackSize() * STACKS : capacity() - count;
        int moved = Math.min(room, stack.getCount());
        if (moved <= 0) {
            return 0;
        }
        if (count == 0) {
            kind = stack.copyWithCount(1);
        }
        count += moved;
        changed();
        return moved;
    }

    /** Removes up to {@code amount} and returns them as one stack. */
    public ItemStack take(int amount) {
        int n = Math.min(Math.min(amount, count), kind.getMaxStackSize());
        if (n <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack out = kind.copyWithCount(n);
        count -= n;
        if (count == 0) {
            kind = ItemStack.EMPTY;
        }
        changed();
        return out;
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private List<ItemStack> asStacks() {
        List<ItemStack> list = new ArrayList<>();
        int left = count;
        while (left > 0 && !kind.isEmpty()) {
            int n = Math.min(left, kind.getMaxStackSize());
            list.add(kind.copyWithCount(n));
            left -= n;
        }
        return list;
    }

    private void fromStacks(Iterable<ItemStack> stacks) {
        kind = ItemStack.EMPTY;
        count = 0;
        for (ItemStack s : stacks) {
            if (s.isEmpty()) {
                continue;
            }
            if (kind.isEmpty()) {
                kind = s.copyWithCount(1);
            }
            if (ItemStack.isSameItemSameComponents(kind, s) && count < capacity()) {
                count = Math.min(capacity(), count + s.getCount());
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (count > 0) {
            output.store("Kind", ItemStack.CODEC, kind);
            output.putInt("Count", count);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        kind = input.read("Kind", ItemStack.CODEC).map(s -> s.copyWithCount(1)).orElse(ItemStack.EMPTY);
        count = kind.isEmpty() ? 0 : Math.min(input.getIntOr("Count", 0), capacity());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (count > 0) {
            components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(asStacks()));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        List<ItemStack> list = new ArrayList<>();
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItemCopyStream().forEach(list::add);
        fromStacks(list);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("Kind");
        output.discard("Count");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
