package net.diaithir.stations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

/** One block class for every Foundry station; the station key picks which recipes it runs. */
public final class StationBlock extends BaseEntityBlock {
    public static final MapCodec<StationBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            propertiesCodec(),
            Codec.STRING.fieldOf("station").forGetter(StationBlock::station)
    ).apply(i, StationBlock::new));

    private final String station;

    public StationBlock(Properties properties, String station) {
        super(properties);
        this.station = station;
    }

    public String station() { return station; }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new StationBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, DiaithirStations.STATION_BE.get(), (l, p, s, be) -> be.tick());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof StationBlockEntity be) {
            if (be.busy()) {
                player.sendOverlayMessage(be.describe());
            } else {
                int n = be.load(stack.copyWithCount(player.isShiftKeyDown() ? 1 : stack.getCount()));
                stack.shrink(n);
                player.sendOverlayMessage(be.describe());
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof StationBlockEntity be) {
            if (player.isShiftKeyDown() && !be.busy()) {
                be.takeAll(player);
                player.sendOverlayMessage(Component.literal("Emptied"));
            } else {
                String made = be.start();
                player.sendOverlayMessage(made != null ? Component.literal("Started: " + made) : be.describe());
            }
        }
        return InteractionResult.CONSUME;
    }
}
