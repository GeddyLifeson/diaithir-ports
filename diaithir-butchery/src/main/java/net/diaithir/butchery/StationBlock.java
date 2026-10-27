package net.diaithir.butchery;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A processing station. Which one it is (grinder, cold room, ...) is its id; what it makes is data (butchery.json "stations"). */
public class StationBlock extends BaseEntityBlock {
    public static final MapCodec<StationBlock> CODEC = simpleCodec(p -> new StationBlock(p, ""));
    final String station;

    public StationBlock(Properties properties, String station) {
        super(properties);
        this.station = station;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StationEntity(pos, state, station);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() || type != Butchery.STATION_ENTITY.get() ? null : (l, p, s, be) -> ((StationEntity) be).tick(l);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof StationEntity st)) return InteractionResult.PASS;
        if (level.isClientSide()) return ButcheryData.recipeFor(station, held) != null ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        if (st.accept(held)) return InteractionResult.SUCCESS;
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof StationEntity st)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            ItemStack out = st.takeOutput();
            if (!out.isEmpty()) {
                player.getInventory().placeItemBackInInventory(out);
            } else {
                player.sendOverlayMessage(Component.translatable(st.busy() ? "message.diaithirbutchery.working" : "message.diaithirbutchery.idle"));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
