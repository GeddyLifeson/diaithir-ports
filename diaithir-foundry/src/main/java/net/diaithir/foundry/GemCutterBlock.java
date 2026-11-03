package net.diaithir.foundry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Right click with a raw gem to cut one into a cut gem. */
public final class GemCutterBlock extends Block {
    public static final MapCodec<GemCutterBlock> CODEC = simpleCodec(GemCutterBlock::new);

    public GemCutterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(DiaithirFoundry.RAW_GEM.get())) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) {
            stack.shrink(1);
            ItemStack out = new ItemStack(DiaithirFoundry.CUT_GEM.get());
            if (!player.getInventory().add(out)) player.drop(out, false);
        }
        return InteractionResult.SUCCESS;
    }
}
