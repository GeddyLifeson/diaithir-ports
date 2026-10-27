package com.diaithir.addons.newage.content.electricity.light;

import com.diaithir.addons.newage.content.TooltipBlock;
import net.minecraft.world.level.redstone.Orientation;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.diaithir.addons.newage.CNABlocks;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.electricity.connector.ElectricalConnectorBlock;
import com.diaithir.addons.newage.content.electricity.connector.ElectricalConnectorMode;
import com.diaithir.addons.newage.util.StringFormatUtil;

import java.util.List;

public class StreetLightBlock extends Block implements IBE<StreetLightBlockEntity>, TooltipBlock, IWrenchable {

    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);

    public StreetLightBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIGHT_LEVEL, 0).setValue(ElectricalConnectorBlock.MODE, ElectricalConnectorMode.INERT));
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<S> p_153214_) {
        return (level, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof StreetLightBlockEntity streetLightBlock && !level.isClientSide())
                streetLightBlock.serverTick();
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(LIGHT_LEVEL).add(ElectricalConnectorBlock.MODE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(
                Block.box(4, 14, 4, 12, 16, 12),
                Block.box(2.5, 12, 2.5, 13.5, 14, 13.5),
                Block.box(3.5, 6, 3.5, 12.5, 12, 12.5),
                Block.box(4.5, 2, 4.5, 11.5, 6, 11.5),
                Block.box(5.5, 0, 5.5, 10.5, 2, 10.5)
        );
    }

    @Override
    public Class<StreetLightBlockEntity> getBlockEntityClass() {
        return StreetLightBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StreetLightBlockEntity> getBlockEntityType() {
        return CNABlockEntityTypes.STREET_LIGHT.get();
    }

    public void appendTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(CreateLang.translate("tooltip.create_new_age.speed").style(ChatFormatting.GRAY).component());
        tooltipComponents.add(CreateLang.text(" ").translate("tooltip.create_new_age.energy_per_tick",
                        StringFormatUtil.formatLong(CNAConfig.getServer().streetLightLevelExtraction.get())).style(ChatFormatting.AQUA)
                .add(CreateLang.text(" ").translate("tooltip.create_new_age.per_light_level").style(ChatFormatting.GRAY)).component());
        tooltipComponents.add(CreateLang.translate("tooltip.create_new_age.stores").style(ChatFormatting.GRAY).component());
        tooltipComponents.add(CreateLang.text(" ").translate("tooltip.create_new_age.energy",
                StringFormatUtil.formatLong(CNAConfig.getServer().streetLightCapacity.get())).style(ChatFormatting.AQUA).component());
    }


    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation neighborPos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof StreetLightBlockEntity connector)
            connector.neighborChanged();
    }
}
