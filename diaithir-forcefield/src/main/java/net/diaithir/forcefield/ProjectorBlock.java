package net.diaithir.forcefield;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** The field projector: placed by a player (who becomes the owner), fed with FE, upgraded with module items. */
public final class ProjectorBlock extends BaseEntityBlock {
    public static final MapCodec<ProjectorBlock> CODEC = simpleCodec(ProjectorBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ProjectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProjectorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != DiaithirForcefield.PROJECTOR_BE.get()) return null;
        return (lvl, pos, st, be) -> ((ProjectorBlockEntity) be).serverTick((ServerLevel) lvl);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof ProjectorBlockEntity be && by instanceof Player p) be.setOwner(p);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (stack.getItem() instanceof ModuleItem mi) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (level.getBlockEntity(pos) instanceof ProjectorBlockEntity be) {
                if (!be.canManage(player)) {
                    player.sendOverlayMessage(Component.literal("This projector is not yours."));
                } else if (be.install(mi.module)) {
                    if (!player.isCreative()) stack.shrink(1);
                    player.sendOverlayMessage(Component.literal("Module fitted. " + be.summary()));
                } else {
                    player.sendOverlayMessage(Component.literal("No room for another of that module."));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ProjectorBlockEntity be)) return InteractionResult.PASS;
        if (!be.canManage(player)) {
            player.sendOverlayMessage(Component.literal(be.summary()));
        } else if (player.isShiftKeyDown()) {
            be.cycleShape();
            player.sendOverlayMessage(Component.literal("Shape: " + be.shape().name().toLowerCase() + ". " + be.summary()));
        } else {
            be.setEnabled(!be.enabled());
            player.sendOverlayMessage(Component.literal((be.enabled() ? "Switched on. " : "Switched off. ") + be.summary()));
        }
        return InteractionResult.SUCCESS;
    }
}
