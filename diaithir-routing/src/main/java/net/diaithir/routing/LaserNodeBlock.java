package net.diaithir.routing;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** The laser node. Router Link item connects two nodes; an empty hand cycles the channel of the clicked face; sneak cycles its priority. */
public final class LaserNodeBlock extends BaseEntityBlock {
    public static final MapCodec<LaserNodeBlock> CODEC = simpleCodec(LaserNodeBlock::new);
    private static final VoxelShape SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public LaserNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaserNodeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, RoutingRefs.laserBe(), (l, p, s, be) -> be.serverTick((ServerLevel) l));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LaserNodeBlockEntity node)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        boolean link = RoutingRefs.isRouterLink(stack);
        if (level.isClientSide()) return stack.getItem() instanceof FilterItem || link ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.getItem() instanceof FilterItem) {
            node.setFilter(hit.getDirection(), FilterItem.read(stack));
            player.sendOverlayMessage(Component.literal("Side filter: " + FilterItem.read(stack).describe()));
            return InteractionResult.SUCCESS;
        }
        if (link) {
            CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (!data.contains("node")) {
                data.putLong("node", pos.asLong());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                player.sendOverlayMessage(Component.literal("First node stored"));
            } else {
                BlockPos first = BlockPos.of(data.getLongOr("node", 0L));
                stack.remove(DataComponents.CUSTOM_DATA);
                boolean ok = !first.equals(pos) && level.getBlockEntity(first) instanceof LaserNodeBlockEntity a && a.connect((ServerLevel) level, pos);
                if (ok) node.connect((ServerLevel) level, first);
                player.sendOverlayMessage(Component.literal(ok ? "Nodes connected" : "Connect failed: out of range or no line of sight"));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof LaserNodeBlockEntity node)) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) node.cyclePriority(hit.getDirection());
        else node.cycleChannel(hit.getDirection());
        player.sendOverlayMessage(Component.literal(node.describeSide(hit.getDirection())));
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LaserNodeBlockEntity node) node.disconnectAll((ServerLevel) level);
        return super.playerWillDestroy(level, pos, state, player);
    }
}
