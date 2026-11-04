package dev.shadowsoffire.apothic_enchanting.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.api.EnchantmentStatBlock;
import dev.shadowsoffire.placebo.network.VanillaPacketDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FilteringShelfBlock extends ChiseledBookShelfBlock implements EnchantmentStatBlock {

    public FilteringShelfBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Set<Holder<Enchantment>> getBlacklistedEnchantments(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FilteringShelfTile shelf) {
            Set<Holder<Enchantment>> blacklist = new HashSet<>();
            for (ItemStack s : shelf.getEnchantedBooks()) {
                ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(s);
                if (enchants.size() != 1) {
                    continue; // Only books with one enchantment are legal.
                }

                Holder<Enchantment> ench = enchants.keySet().stream().findFirst().orElse(null);
                if (ench != null) {
                    blacklist.add(ench);
                }
            }
            return blacklist;
        }
        return Collections.emptySet();
    }

    @Override
    public float getMaxEnchantingPower(BlockState state, BlockGetter level, BlockPos pos) {
        return 30F;
    }

    public float getEnchantPowerBonus(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FilteringShelfTile shelf) {
            return shelf.getEnchantedBooks().size() * 0.5F;
        }
        return 0;
    }

    @Override
    public float getArcanaBonus(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FilteringShelfTile shelf) {
            return shelf.getEnchantedBooks().size();
        }
        return 0;
    }

    @Override
    public ParticleOptions getTableParticle(BlockState state) {
        return Ench.Particles.ENCHANT_WATER;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChiseledBookShelfBlockEntity shelf) {
            if (!canInsert(stack)) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }

            OptionalInt slot = this.getHitSlot(hitResult, state.getValue(FACING));
            if (slot.isEmpty()) {
                return InteractionResult.PASS;
            }
            else if (state.getValue(SLOT_OCCUPIED_PROPERTIES.get(slot.getAsInt()))) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            else {
                addBook(level, pos, player, shelf, stack, slot.getAsInt());
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FilteringShelfTile(pPos, pState);
    }

    // NOTE (26.1 port): Block#appendHoverText was removed; tooltip descriptions now live on BlockItem
    // or data components. Move the 'info.apothic_enchanting.filtering_shelf' tooltip to a BlockItem.

    public static boolean canInsert(ItemStack stack) {
        if (EnchantmentHelper.getEnchantmentsForCrafting(stack).size() > 1) {
            return false; // Books with more than 1 enchantment don't work for blacklisting, so prevent them from being used to avoid confusion.
        }
        return stack.is(ItemTags.BOOKSHELF_BOOKS);
    }

    public static boolean isEnchantedBook(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) && EnchantmentHelper.getEnchantmentsForCrafting(stack).size() == 1;
    }

    public static class FilteringShelfTile extends ChiseledBookShelfBlockEntity {

        public FilteringShelfTile(BlockPos pPos, BlockState pState) {
            super(pPos, pState);
        }

        @Override
        public boolean canPlaceItem(int pIndex, ItemStack pStack) {
            return canInsert(pStack);
        }

        @Override
        public BlockEntityType<?> getType() {
            return Ench.Tiles.FILTERING_SHELF;
        }

        public List<ItemStack> getBooks() {
            List<ItemStack> books = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                ItemStack stack = this.getItem(i);
                if (!stack.isEmpty()) books.add(stack);
            }
            return books;
        }

        public List<ItemStack> getEnchantedBooks() {
            return getBooks().stream().filter(FilteringShelfBlock::isEnchantedBook).toList();
        }

        @Override
        public ClientboundBlockEntityDataPacket getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }

        @Override
        public void setItem(int pSlot, ItemStack pStack) {
            super.setItem(pSlot, pStack);
            if (!this.level.isClientSide()) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            }
        }

    }

}
