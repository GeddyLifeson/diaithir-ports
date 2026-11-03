package org.diaithir.mega;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The cab of a mega vehicle. Holds which machine it drives, whether an autonomy module is fitted, and says whether the machine may run. */
public class MegaCabBlock extends BaseEntityBlock {
    public static final MapCodec<MegaCabBlock> CODEC = simpleCodec(MegaCabBlock::new);
    /** The carts module's item; referenced by id so ships needs no dependency on carts. */
    public static final String MODULE_ITEM = "diaithircarts:autonomy_module";

    public MegaCabBlock(Properties props) { super(props); }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MegaCabBlockEntity(pos, state); }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                          net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!held.is(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(MODULE_ITEM)))
                || held.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof MegaCabBlockEntity cab) {
            boolean fitted = cab.fitModule();
            if (fitted && !player.getAbilities().instabuild) held.shrink(1);
            player.sendOverlayMessage(Component.translatable(fitted ? "megamachines.cab.module_fitted" : "megamachines.cab.module_refused"));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof MegaCabBlockEntity cab)
            player.sendOverlayMessage(Component.translatable("megamachines.cab.status." + cab.status().name().toLowerCase(), cab.specId()));
        return InteractionResult.CONSUME;
    }
}
