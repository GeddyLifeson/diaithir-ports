package dev.shadowsoffire.apothic_enchanting.objects;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import dev.shadowsoffire.apothic_enchanting.library.EnchLibraryBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockItem for the enchantment library blocks that surfaces capacity + stored point count in
 * the item tooltip.
 */
public class LibraryBlockItem extends BlockItem {

    private final int maxLevel;

    public LibraryBlockItem(Block block, Item.Properties properties, int maxLevel) {
        super(block, properties);
        this.maxLevel = maxLevel;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.enchlib.capacity", Component.translatable("enchantment.level." + this.maxLevel)).withStyle(ChatFormatting.GOLD));
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null && data.contains("points")) {
            int points = data.copyTagWithoutId().getCompoundOrEmpty("points").size();
            if (points > 0) {
                tooltip.accept(Component.translatable("tooltip.enchlib.item", points).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack itemStack, BlockState placedState) {
        return updateLibraryBETag(level, player, pos, itemStack);
    }

    /**
     * Variant of {@link #updateCustomBlockEntityTag(Level, Player, BlockPos, ItemStack)} which has a more lenient check than type != customData.type()
     */
    private boolean updateLibraryBETag(Level level, @Nullable Player player, BlockPos pos, ItemStack itemStack) {
        if (level.isClientSide()) {
            return false;
        }
        else {
            TypedEntityData<BlockEntityType<?>> customData = itemStack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (customData != null) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) {
                    if (!customData.type().getValidBlocks().stream().anyMatch(EnchLibraryBlock.class::isInstance)) {
                        return false;
                    }

                    if (player != null) {
                        return customData.loadInto(blockEntity, level.registryAccess());
                    }

                    return false;
                }
            }

            return false;
        }
    }
}
