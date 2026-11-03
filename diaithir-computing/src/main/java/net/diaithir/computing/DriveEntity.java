package net.diaithir.computing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Holds one disk. */
public final class DriveEntity extends BlockEntity {
    ItemStack disk = ItemStack.EMPTY;

    public DriveEntity(BlockPos pos, BlockState state) { super(Registry.DRIVE_ENTITY.get(), pos, state); }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide() && !disk.isEmpty()) net.minecraft.world.level.block.Block.popResource(level, pos, disk);
        disk = ItemStack.EMPTY;
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (!disk.isEmpty()) out.store("disk", ItemStack.OPTIONAL_CODEC, disk);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        disk = in.read("disk", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
