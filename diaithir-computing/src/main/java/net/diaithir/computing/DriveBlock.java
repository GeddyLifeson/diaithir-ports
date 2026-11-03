package net.diaithir.computing;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Holds a disk next to a computer. Use a disk on it to put it in; sneak with an empty hand to take it out; an empty hand otherwise lists the files. */
public final class DriveBlock extends BaseEntityBlock {
    public static final MapCodec<DriveBlock> CODEC = simpleCodec(DriveBlock::new);

    public DriveBlock(Properties props) { super(props); }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DriveEntity(pos, state); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Registry.DISK.get())) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof DriveEntity be && be.disk.isEmpty()) {
            be.disk = stack.split(1);
            be.setChanged();
            player.sendOverlayMessage(Component.literal("Disk in."));
        } else player.sendOverlayMessage(Component.literal("The drive already has a disk."));
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof DriveEntity be) {
            if (be.disk.isEmpty()) player.sendOverlayMessage(Component.literal("Empty drive. Use a disk on it."));
            else if (player.isShiftKeyDown()) {
                if (!player.getInventory().add(be.disk)) player.drop(be.disk, false);
                be.disk = ItemStack.EMPTY;
                be.setChanged();
                player.sendOverlayMessage(Component.literal("Disk out."));
            } else {
                var files = Disks.files(be.disk);
                player.sendOverlayMessage(Component.literal(files.keySet().size() + " files, " + (Disks.MAX_TOTAL - Disks.total(files)) + " characters free."));
            }
        }
        return InteractionResult.CONSUME;
    }
}
