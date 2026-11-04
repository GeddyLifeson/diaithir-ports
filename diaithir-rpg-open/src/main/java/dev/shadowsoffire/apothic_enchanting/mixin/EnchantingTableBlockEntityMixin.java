package dev.shadowsoffire.apothic_enchanting.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dev.shadowsoffire.apothic_enchanting.table.EnchantmentTableItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Applies the {@link Clearable} interface to the EnchantingTableBlockEntity to wipe the attached inventory when called.
 */
@Mixin(EnchantingTableBlockEntity.class)
public abstract class EnchantingTableBlockEntityMixin extends BlockEntity implements Clearable {

    private EnchantingTableBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void clearContent() {
        EnchantmentTableItemHandler handler = this.getData(EnchantmentTableItemHandler.TYPE);
        for (int i = 0; i < handler.size(); i++) {
            handler.set(i, ItemResource.EMPTY, 0);
        }
        this.setData(EnchantmentTableItemHandler.TYPE, handler);
    }

}
