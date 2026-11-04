// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A fuel pump that stands on a fuel tank. Use it to take the hose, use it again to hang the hose back; with the hose, use any vehicle with a fuel store. */
public class GasPumpBlock extends BaseEntityBlock {
    public static final MapCodec<GasPumpBlock> CODEC = simpleCodec(GasPumpBlock::new);

    public GasPumpBlock(Properties p) {
        super(p);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GasPumpEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, CrayfishVehicles.GAS_PUMP_BE.get(), GasPumpEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof GasPumpEntity pump)) return InteractionResult.PASS;
        if (pump.tank() == null) {
            player.sendOverlayMessage(Component.literal("The pump needs a fuel tank under it."));
            return InteractionResult.CONSUME;
        }
        if (player.getUUID().equals(pump.fueler())) {
            pump.release();
            player.sendOverlayMessage(Component.literal("Hose hung up."));
        } else if (pump.fueler() != null) {
            player.sendOverlayMessage(Component.literal("Someone else has the hose."));
        } else {
            pump.take(player);
            player.sendOverlayMessage(Component.literal("Hose taken. Use a vehicle to fuel it."));
        }
        return InteractionResult.CONSUME;
    }
}
