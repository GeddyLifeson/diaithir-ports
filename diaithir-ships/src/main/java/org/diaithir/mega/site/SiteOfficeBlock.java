package org.diaithir.mega.site;

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
import org.diaithir.mega.MegaMod;
import org.jspecify.annotations.Nullable;

/** The only crafting surface for megastructures: holds the blueprint, the stage ledger and the Age of the office. */
public class SiteOfficeBlock extends BaseEntityBlock {
    public static final MapCodec<SiteOfficeBlock> CODEC = simpleCodec(SiteOfficeBlock::new);

    public SiteOfficeBlock(Properties props) { super(props); }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SiteOfficeBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, MegaMod.OFFICE_BE.get(), SiteOfficeBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof SiteOfficeBlockEntity o)
            player.sendOverlayMessage(Component.translatable("megamachines.site.status", o.structureId().isEmpty() ? "-" : o.structureId(),
                    o.engine() == null ? "-" : String.valueOf(o.engine().stageIndex() + 1), o.statusName(),
                    o.engine() == null ? "0%" : Math.round(o.engine().functionShare() * 100) + "%"));
        return InteractionResult.CONSUME;
    }
}
