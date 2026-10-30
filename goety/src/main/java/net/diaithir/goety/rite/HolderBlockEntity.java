package net.diaithir.goety.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** A block entity that holds one item and shows it to the client (altar and pedestal). */
public class HolderBlockEntity extends BlockEntity {
    protected ItemStack held = ItemStack.EMPTY;

    public HolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack held() {
        return held;
    }

    public void setHeld(ItemStack s) {
        held = s;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (!held.isEmpty()) out.store("Item", ItemStack.CODEC, held);
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        held = in.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
}
