package com.diaithir.addons.newage.content.motor.extension;

import net.neoforged.neoforge.registries.DeferredHolder;
import com.diaithir.addons.newage.content.TooltipBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.diaithir.addons.newage.content.motor.MotorBlock;
import com.diaithir.addons.newage.content.motor.MotorBlockEntity;
import com.diaithir.addons.newage.content.motor.extension.variants.IMotorExtensionVariant;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MotorExtensionBlock extends Block implements IBE<MotorExtensionBlockEntity>, TooltipBlock, IWrenchable {
    protected static final VoxelShape DOWN_AABB = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
    protected static final VoxelShape UP_AABB = Block.box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape NORTH_AABB = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 12.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(2.0, 2.0, 4.0, 14.0, 14.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(0.0, 2.0, 2.0, 12.0, 14.0, 14.0);
    protected static final VoxelShape EAST_AABB = Block.box(4.0, 2.0, 2.0, 16.0, 14.0, 14.0);
    private final IMotorExtensionVariant variant;
    DeferredHolder<BlockEntityType<?>, BlockEntityType<MotorExtensionBlockEntity>> entry;

    public MotorExtensionBlock(Properties properties, DeferredHolder<BlockEntityType<?>, BlockEntityType<MotorExtensionBlockEntity>> entry, IMotorExtensionVariant variant) {
        super(properties);
        this.variant = variant;
        this.entry = entry;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    public void appendTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(Component.translatable("tooltip.create_new_age.motor_extension").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(CreateLang.translate("tooltip.create_new_age.stress_limit_multiplier").style(ChatFormatting.GRAY)
                .component());
        tooltipComponents.add(CreateLang.text(" ").add(CreateLang.number((int)(variant.getMultiplier() * 100)).text("%").style(ChatFormatting.AQUA)).component());

        tooltipComponents.add(CreateLang.translate("tooltip.create_new_age.additional_capacity").style(ChatFormatting.GRAY)
                .component());
        tooltipComponents.add(CreateLang.text(" ").add(CreateLang.number(variant.getExtraCapacity()).text("⚡").style(ChatFormatting.AQUA)).component());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace().getOpposite();
        BlockPos pos = context.getClickedPos();
        if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
            direction = direction.getOpposite();
        } else if (!(context.getLevel().getBlockState(pos.relative(direction)).getBlock() instanceof MotorBlock)) {
            for (Direction d : Direction.values()) {
                if (context.getLevel().getBlockState(pos.relative(d)).getBlock() instanceof MotorBlock) {
                    direction = d;
                    break;
                }
            }
        }

        BlockState blockState = super.getStateForPlacement(context).setValue(BlockStateProperties.FACING, direction);
        return blockState;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (world instanceof ServerLevel) {
            if (player != null && !player.isCreative()) {
                Block.getDrops(state, (ServerLevel)world, pos, world.getBlockEntity(pos), player, context.getItemInHand()).forEach((itemStack) -> {
                    player.getInventory().placeItemBackInInventory(itemStack);
                });
            }

            state.spawnAfterBreak((ServerLevel)world, pos, ItemStack.EMPTY, true);
            world.destroyBlock(pos, false);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (state.hasBlockEntity()) {
            BlockEntity entity = context.getLevel().getBlockEntity(context.getClickedPos());
            if (entity instanceof MotorBlockEntity en) {
                en.needsPower = !en.needsPower;
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    public VoxelShape getShape(BlockState arg, BlockGetter arg2, BlockPos arg3, CollisionContext arg4) {
        return switch (arg.getValue(BlockStateProperties.FACING)) {
            case DOWN -> DOWN_AABB;
            case UP -> UP_AABB;
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
        };

    }

    @Override
    public Class<MotorExtensionBlockEntity> getBlockEntityClass() {
        return MotorExtensionBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MotorExtensionBlockEntity> getBlockEntityType() {
        return entry.get();
    }
}
