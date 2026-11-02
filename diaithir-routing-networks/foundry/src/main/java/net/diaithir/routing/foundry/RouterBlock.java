package net.diaithir.routing.foundry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * Sorting Router: anything pushed into one face leaves by the other faces. Each face has its own filter. An item goes to the faces whose
 * filter matches it; if none do, to the faces with an empty filter. Faces with a filter that does not match never get it.
 * Sneak-use a face with an item to add it to that face's filter, sneak-use empty-handed to clear it, plain use to read all six.
 */
public final class RouterBlock extends BaseEntityBlock {
    public static final MapCodec<RouterBlock> CODEC = simpleCodec(RouterBlock::new);

    public RouterBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RouterBlockEntity(pos, state); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty() || !player.isShiftKeyDown()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof RouterBlockEntity be) {
            Direction d = hit.getDirection();
            boolean added = be.filter(d).add(ItemResource.of(stack));
            be.setChanged();
            player.sendOverlayMessage(Component.literal((added ? "Added to " : "Not added to ") + d.getName() + ": " + be.filter(d).describe()));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof RouterBlockEntity be) {
            if (player.isShiftKeyDown()) {
                be.filter(hit.getDirection()).clear();
                be.setChanged();
            }
            player.sendOverlayMessage(Component.literal(be.describe()));
        }
        return InteractionResult.CONSUME;
    }
}
