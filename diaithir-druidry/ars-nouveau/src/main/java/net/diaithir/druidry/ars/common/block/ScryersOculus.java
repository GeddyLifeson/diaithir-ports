package net.diaithir.druidry.ars.common.block;

import net.diaithir.druidry.ars.api.camera.ICameraMountable;
import net.diaithir.druidry.ars.common.block.tile.ArcanePedestalTile;
import net.diaithir.druidry.ars.common.block.tile.ScryersOculusTile;
import net.diaithir.druidry.ars.common.items.data.ScryPosData;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScryersOculus extends TickableModBlock {

    public ScryersOculus() {
        this(defaultProperties().noOcclusion());
    }

    public ScryersOculus(Properties properties) {
        super(properties);
    }


    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pHand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!pLevel.isClientSide() && pLevel.getBlockEntity(pPos) instanceof ScryersOculusTile oculus) {
            // Port note: the radial menu screen is client-only UI that is not part of the port; the oculus cycles
            // through the scrolls on nearby pedestals on sneak-use and mounts the selected scroll's camera on use.
            List<ItemStack> scrolls = new ArrayList<>();
            int offset = 3;
            for (BlockPos b : BlockPos.betweenClosed(pPos.offset(offset, -offset, offset), pPos.offset(-offset, offset, -offset))) {
                if (pLevel.getBlockEntity(b) instanceof ArcanePedestalTile tile && tile.getStack().is(ItemsRegistry.SCRYER_SCROLL.get())) {
                    scrolls.add(tile.getStack());
                }
            }
            if (scrolls.isEmpty()) {
                PortUtil.sendMessage(pPlayer, Component.translatable("ars_nouveau.scryers_eye.no_scrolls"));
                return InteractionResult.SUCCESS;
            }
            if (pPlayer.isShiftKeyDown()) {
                oculus.selectedScroll = (oculus.selectedScroll + 1) % scrolls.size();
                PortUtil.sendMessage(pPlayer, scrolls.get(oculus.selectedScroll).getHoverName());
                return InteractionResult.SUCCESS;
            }
            ItemStack scroll = scrolls.get(oculus.selectedScroll % scrolls.size());
            ScryPosData data = scroll.get(DataComponentRegistry.SCRY_DATA);
            if (data == null || data.pos().isEmpty()) {
                PortUtil.sendMessage(pPlayer, Component.translatable("ars_nouveau.scryers_eye.no_pos"));
                return InteractionResult.SUCCESS;
            }
            net.minecraft.core.GlobalPos global = data.pos().get();
            BlockPos target = global.pos();
            if (global.dimension().equals(pLevel.dimension()) && pLevel.getBlockEntity(target) instanceof ICameraMountable mountable) {
                mountable.mountCamera(pLevel, target, pPlayer);
            } else {
                PortUtil.sendMessage(pPlayer, Component.translatable("ars_nouveau.camera.not_loaded"));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ScryersOculusTile(pPos, pState);
    }

    @Override
    public boolean isPathfindable(BlockState pState, PathComputationType pType) {
        return false;
    }
}
