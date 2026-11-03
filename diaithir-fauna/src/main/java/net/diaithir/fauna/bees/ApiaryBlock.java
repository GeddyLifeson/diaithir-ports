package net.diaithir.fauna.bees;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A jar of bees goes in, combs come out. Empty hand: take the combs; sneaking: also let the bees out in their jars. */
public final class ApiaryBlock extends BaseEntityBlock {
    public static final MapCodec<ApiaryBlock> CODEC = simpleCodec(ApiaryBlock::new);

    public ApiaryBlock(Properties props) { super(props); }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ApiaryEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Bees.APIARY_ENTITY.get(), ApiaryEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Bees.JAR.get()) || BeeJarItem.species(stack).isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ApiaryEntity be) {
            if (be.addBee(BeeJarItem.species(stack))) {
                stack.shrink(1);
                ItemStack empty = new ItemStack(Bees.JAR.get());
                if (!player.getInventory().add(empty)) player.drop(empty, false);
                player.sendOverlayMessage(Component.literal(be.status()));
            } else player.sendOverlayMessage(Component.literal("The apiary is full (" + ApiaryEntity.MAX_BEES + " bees)."));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ApiaryEntity be) {
            int taken = be.takeCombs(player);
            if (player.isShiftKeyDown()) for (String s : be.takeBees()) {
                ItemStack jar = BeeJarItem.filled(s);
                if (!player.getInventory().add(jar)) player.drop(jar, false);
            }
            player.sendOverlayMessage(Component.literal((taken > 0 ? "Took " + taken + " combs. " : "") + be.status()));
        }
        return InteractionResult.CONSUME;
    }
}
