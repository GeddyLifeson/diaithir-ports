package net.diaithir.fauna;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Use a fossil fragment on the bench with a brush in the other hand to clean it. */
public class PrepBenchBlock extends Block {
    public PrepBenchBlock(Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Fossils.FRAGMENT.get())) return InteractionResult.TRY_WITH_EMPTY_HAND;
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack brush = player.getItemInHand(other);
        if (!brush.is(Items.BRUSH)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        Species s = Fossils.speciesOf(stack);
        if (s == null) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            stack.shrink(1);
            brush.hurtAndBreak(1, player, other == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            ItemStack out = Fossils.stack(Fossils.CLEANED.get(), s);
            if (!player.getInventory().add(out)) player.drop(out, false);
            level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1f, 1f);
        }
        return InteractionResult.SUCCESS;
    }
}
