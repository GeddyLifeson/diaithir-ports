package net.diaithir.conduits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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

/**
 * A conduit node of tier 1 or 2. Use the tuner on it to step its channel up (sneak: down). Use it empty-handed to read it,
 * sneak with an empty hand to step the mode of the face you click (off, send, receive).
 */
public final class ConduitBlock extends BaseEntityBlock {
    public static final MapCodec<ConduitBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec(), Codec.INT.fieldOf("tier").forGetter(b -> b.tier)).apply(i, ConduitBlock::new));
    public final int tier;

    public ConduitBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConduitBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, DiaithirConduits.CONDUIT_BE.get(), ConduitBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        if (!level.isClientSide() && by instanceof Player p && level.getBlockEntity(pos) instanceof ConduitBlockEntity be) {
            be.claim(p);
            Channels.get(level.getServer()).rememberName(p.getStringUUID(), p.getGameProfile().name());
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(DiaithirConduits.TUNER.get())) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ConduitBlockEntity be && level instanceof ServerLevel sl) {
            int max = Config.MAX_CHANNEL_NUMBER.get();
            // the tuner steps through the player's own channels
            boolean mine = be.channel() > 0 && be.channelOwner().equals(player.getStringUUID());
            int next = !mine ? 1 : player.isShiftKeyDown() ? (be.channel() - 2 + max) % max + 1 : be.channel() % max + 1;
            String err = be.join(sl, player.getStringUUID(), next, player);
            Channels ch = Channels.get(sl.getServer());
            ch.rememberName(player.getStringUUID(), player.getGameProfile().name());
            player.sendOverlayMessage(Component.literal(err != null ? err : be.describe(ch)));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ConduitBlockEntity be) {
            if (player.isShiftKeyDown()) {
                if (!be.canManage(player)) {
                    player.sendOverlayMessage(Component.literal("This node belongs to " + be.ownerName + "."));
                    return InteractionResult.CONSUME;
                }
                Direction d = hit.getDirection();
                int next = (be.mode(d) + 1) % 3;
                player.sendOverlayMessage(Component.literal("Face " + be.setMode(d, next)));
            } else {
                player.sendOverlayMessage(Component.literal(be.describe(Channels.get(level.getServer()))));
            }
        }
        return InteractionResult.CONSUME;
    }
}
