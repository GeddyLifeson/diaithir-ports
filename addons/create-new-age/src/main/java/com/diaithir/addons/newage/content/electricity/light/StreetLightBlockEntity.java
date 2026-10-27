package com.diaithir.addons.newage.content.electricity.light;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.electricity.connector.AbstractElectricalConnector;
import com.diaithir.addons.newage.content.electricity.network.ElectricalNetwork;
import com.diaithir.addons.newage.content.electricity.network.SimpleNetworkEnergyStorage;
import com.diaithir.addons.newage.util.RunnableUtil;
import com.diaithir.addons.newage.util.StringFormatUtil;
import com.diaithir.addons.newage.esl.EnergyStorage;

import java.util.List;

public class StreetLightBlockEntity extends AbstractElectricalConnector implements IHaveGoggleInformation {
    private final SimpleNetworkEnergyStorage storage;

    private long prvEnergy = -100000;
    public com.diaithir.addons.newage.content.RawScrollValue lightLevelBehaviour;

    public StreetLightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);

        storage = new SimpleNetworkEnergyStorage(this, null, CNAConfig.getServer().streetLightCapacity.get())
                .onFinalCommit(RunnableUtil.createBlockEntityUpdater(this))
                .setSupportsExtraction(false);

        EnergyStorage.registerForBlockEntity((blockEntity, direction) -> blockEntity.storage, CNABlockEntityTypes.STREET_LIGHT.get());
    }

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        storage.setStoredEnergy(tag.getLongOr("energy", 0L));
        super.read(tag, clientPacket);
    }

    @Override
    public Direction getFacing() {
        return null;
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        tag.putLong("energy", storage.getStoredEnergy());
        super.write(tag, clientPacket);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        lightLevelBehaviour = new com.diaithir.addons.newage.content.RawScrollValue(this);
        lightLevelBehaviour.between(0, 15);
        lightLevelBehaviour.setRaw(15);
        lightLevelBehaviour.withCallback( i -> {
            if (getLevel() != null && storage.getStoredEnergy() > 0)
                getLevel().setBlock(getBlockPos(), getBlockState().setValue(StreetLightBlock.LIGHT_LEVEL, i), 3);
        });
        behaviours.add(lightLevelBehaviour);
        super.addBehaviours(behaviours);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("tooltip.create_new_age.energy_stored")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CreateLang.translate("tooltip.create_new_age.energy_storage", StringFormatUtil.formatLong(storage.getStoredEnergy()),
                        StringFormatUtil.formatLong(storage.getCapacity()))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    protected void serverTick() {
        super.serverTick();
        if (getLevel() == null)
            return;
        long needed = (long) lightLevelBehaviour.getValue() * CNAConfig.getServer().streetLightLevelExtraction.get();
        long e = storage.internalExtract(needed, false);
        if (prvEnergy == storage.getStoredEnergy())
            return;
        if (e <= 0) {
            getLevel().setBlock(getBlockPos(), getBlockState().setValue(StreetLightBlock.LIGHT_LEVEL, 0), 3);
        } else {
            getLevel().setBlock(getBlockPos(), getBlockState().setValue(StreetLightBlock.LIGHT_LEVEL, lightLevelBehaviour.getValue()), 3);
        }
        if (storage.getStoredEnergy() != prvEnergy && level.getGameTime() % 20 == 0) {
            this.sendData();
            prvEnergy = storage.getStoredEnergy();
        }
    }



    @Override
    public void setNetwork(ElectricalNetwork network) {
        super.setNetwork(network);
        storage.setNetwork(network);
    }

    public Vec3 getConnectionPoint() {
        return new Vec3(0.5f, 1/16f, 0.5f);
    }
}
