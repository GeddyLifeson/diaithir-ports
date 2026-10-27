package com.diaithir.addons.newage.content.electricity.connector;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.diaithir.addons.newage.content.electricity.network.ElectricalNetwork;
import com.diaithir.addons.newage.content.electricity.network.NetworkEnergyStorage;
import com.diaithir.addons.newage.esl.EnergyStorage;

import java.util.*;

public class ElectricalConnectorBlockEntity extends AbstractElectricalConnector implements IHaveGoggleInformation {
    private final NetworkEnergyStorage storage;

    public ElectricalConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        storage = new NetworkEnergyStorage(this, null);
        EnergyStorage.registerForBlockEntity((blockEntity, direction) -> blockEntity.storage, CNABlockEntityTypes.ELECTRICAL_CONNECTOR.get());
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("tooltip.create_new_age.connector_info")
                .style(ChatFormatting.WHITE).forGoggles(tooltip);

        CreateLang.translate("tooltip.create_new_age.mode")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        ElectricalConnectorMode mode = getBlockState().getValue(ElectricalConnectorBlock.MODE);
        CreateLang.translate("tooltip.create_new_age.connector_mode." + mode.getSerializedName())
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public BlockPos getSupportingBlockPos() {
        return getBlockPos().relative(getBlockState().getValue(BlockStateProperties.FACING).getOpposite());
    }

    @Override
    public void setNetwork(ElectricalNetwork network) {
        super.setNetwork(network);
        storage.setNetwork(network);
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }
}
