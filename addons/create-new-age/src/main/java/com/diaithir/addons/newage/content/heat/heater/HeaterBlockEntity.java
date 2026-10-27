package com.diaithir.addons.newage.content.heat.heater;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.heat.HeatBlockEntity;
import com.diaithir.addons.newage.util.StringFormatUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeaterBlockEntity extends BlockEntity implements HeatBlockEntity, IHaveGoggleInformation {
    public HeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public float heat = 0;

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        heat = tag.getFloatOr("heat", 0f);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putFloat("heat", heat);
    }

    @Override
    public boolean canConnect(Direction from) {
        return from != Direction.DOWN;
    }


    @Override
    public float getHeat() {
        return heat;
    }

    @Override
    public void addHeat(float amount) {
        heat += amount;
        setChanged();
    }

    @Override
    public void setHeat(float amount) {
        heat = amount;
        setChanged();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public float[] getHeatTiers() {
        return new float[] {
                50,
                100,
                400,
                500
        };
    }

    @Override
    public float getTierHeat() {
        BlazeBurnerBlock.HeatLevel strength = getBlockState().getValue(HeaterBlock.STRENGTH);
        double heat = 0;
        Double mult = CNAConfig.getServer().heaterRequiredHeatMultiplier.get();
        switch (strength) {
            case NONE -> {
                heat = 0;
            }
            case SMOULDERING -> {
                heat = 50 * mult;
            }
            case FADING -> {
                heat = 100 * mult;
            }
            case KINDLED -> {
                heat = 400 * mult;
            }
            case SEETHING -> {
                heat = 500 * mult;
            }
        }
        return (float)heat;
    }

    @Override
    public double getHeatTierMultiplier() {
        return CNAConfig.getServer().heaterRequiredHeatMultiplier.get();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        HeatBlockEntity.addToolTips(this, tooltip);
        BlazeBurnerBlock.HeatLevel strength = getBlockState().getValue(HeaterBlock.STRENGTH);
        double heat = 0;

        Double mult = CNAConfig.getServer().heaterRequiredHeatMultiplier.get();

        switch (strength) {

            case NONE -> {
                heat = 0;
            }
            case SMOULDERING -> {
                heat = 50 * mult;
            }
            case FADING -> {
                heat = 100 * mult;
            }
            case KINDLED -> {
                heat = 400 * mult;
            }
            case SEETHING -> {
                heat = 500 * mult;
            }
        }

        CreateLang.translate("tooltip.create_new_age.releasing")
                .style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        CreateLang.translate("tooltip.create_new_age.temperature.ps", StringFormatUtil.formatFloat((float)heat))
                .style(ChatFormatting.AQUA).forGoggles(tooltip, 2);
        return true;
    }

}
