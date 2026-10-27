package com.diaithir.addons.newage.content.energising;

import com.diaithir.addons.newage.content.TooltipBlock;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.util.StringFormatUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnergiserBlock extends HorizontalKineticBlock implements IBE<EnergiserBlockEntity>, TooltipBlock {
    private final int tier;
    
    public EnergiserBlock(Properties properties, int tier) {
        super(properties.strength(2.5F, 1.0F));
        this.tier = tier;
    }

    public static int getStrength(int tier) {
        return (int) ((Math.pow(4, tier)) * CNAConfig.getServer().energiserSpeedMultiplier.get());
    }

    public static long getCapacity(int tier) {
        return (long) (Math.pow(10, tier) * 1000);
    }


    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (context instanceof EntityCollisionContext && ((EntityCollisionContext) context).getEntity() instanceof Player)
            return AllShapes.CASING_14PX.get(Direction.DOWN);

        return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferredSide = getPreferredHorizontalFacing(context);
        if (preferredSide != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, preferredSide);
        return super.getStateForPlacement(context);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<EnergiserBlockEntity> getBlockEntityClass() {
        return EnergiserBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EnergiserBlockEntity> getBlockEntityType() {
        return CNABlockEntityTypes.ENERGISER.get();
    }


    public void appendTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(CreateLang.translate("tooltip.create_new_age.speed").style(ChatFormatting.GRAY).component());
        tooltipComponents.add(CreateLang.text(" ").translate("tooltip.create_new_age.energy_per_tick",
                        StringFormatUtil.formatLong(getStrength(tier))).style(ChatFormatting.AQUA)
                .add(CreateLang.text(" ").translate("tooltip.create_new_age.per_rpm", 10).style(ChatFormatting.GRAY)).component());
        tooltipComponents.add(CreateLang.translate("tooltip.create_new_age.stores").style(ChatFormatting.GRAY).component());
        tooltipComponents.add(CreateLang.text(" ").translate("tooltip.create_new_age.energy",
                StringFormatUtil.formatLong(getCapacity(tier))).style(ChatFormatting.AQUA).component());
    }

    public static EnergiserBlock newBasic(Properties properties) {
        return new EnergiserBlock(properties, 1);
    }

    public static EnergiserBlock newAdvanced(Properties properties) {
        return new EnergiserBlock(properties, 2);
    }

    public static EnergiserBlock newReinforced(Properties properties) {
        return new EnergiserBlock(properties, 3);
    }
}
