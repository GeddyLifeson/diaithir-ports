package net.diaithir.brewing;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A keg or fermenting barrel: pour a base, drop ingredients in by hand, wait, then draw servings with the container. */
public final class BrewVesselBlock extends BaseEntityBlock {
    public static final MapCodec<BrewVesselBlock> CODEC = simpleCodec(p -> new BrewVesselBlock(p, "keg", 6));

    final String station;
    final int capacity;

    public BrewVesselBlock(Properties properties, String station, int capacity) {
        super(properties);
        this.station = station;
        this.capacity = capacity;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrewVesselEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, DiaithirBrewing.VESSEL_ENTITY.get(), BrewVesselEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BrewVesselEntity be)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (be.done()) {
            if (stack.is(be.container)) {
                if (!level.isClientSide()) {
                    ItemStack drink = be.serve();
                    stack.consume(1, player);
                    if (!player.getInventory().add(drink)) {
                        player.drop(drink, false);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (be.busy()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        Item item = stack.getItem();
        if (be.acceptsBase(item)) {
            if (!level.isClientSide()) {
                be.base = item;
                Item left = BrewRecipes.all().stream().filter(r -> r.base() == item).map(BrewRecipe::baseReturns).findFirst().orElse(Items.AIR);
                stack.consume(1, player);
                if (left != Items.AIR && !player.getInventory().add(new ItemStack(left))) {
                    player.drop(new ItemStack(left), false);
                }
                be.tryStart();
            }
            return InteractionResult.SUCCESS;
        }
        if (be.acceptsIngredient(item)) {
            if (!level.isClientSide()) {
                be.held.merge(item, 1, Integer::sum);
                stack.consume(1, player);
                be.tryStart();
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof BrewVesselEntity be) {
            if (!level.isClientSide()) {
                if (player.isShiftKeyDown() && !be.done()) {
                    be.dump(level, pos);
                } else if (!be.startNow()) {
                    player.sendOverlayMessage(status(be));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static Component status(BrewVesselEntity be) {
        if (be.done()) {
            return Component.translatable("block.diaithirbrewing.ready", be.drink.getHoverName(), be.drink.getCount());
        }
        if (be.busy()) {
            return Component.translatable("block.diaithirbrewing.fermenting", 100 * be.progress / be.active.ticks());
        }
        return Component.translatable("block.diaithirbrewing.filling", be.count(), be.capacity());
    }
}
