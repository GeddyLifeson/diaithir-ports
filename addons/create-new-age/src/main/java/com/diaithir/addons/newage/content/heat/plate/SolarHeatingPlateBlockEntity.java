package com.diaithir.addons.newage.content.heat.plate;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.heat.HeatBlockEntity;
import com.diaithir.addons.newage.util.StringFormatUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SolarHeatingPlateBlockEntity extends BlockEntity implements HeatBlockEntity, IHaveGoggleInformation {
    private final int energyPerSecond;
    private float last;

    public SolarHeatingPlateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int energyPerSecond) {
        super(type, pos, blockState);
        this.energyPerSecond = energyPerSecond;
    }

    public static SolarHeatingPlateBlockEntity createBasic(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new SolarHeatingPlateBlockEntity(type, pos, blockState, 20);
    }

    public static SolarHeatingPlateBlockEntity createAdvanced(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new SolarHeatingPlateBlockEntity(type, pos, blockState, 60);
    }

    public float heat = 0;

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        heat = tag.getFloatOr("heat", 0f);
        last = tag.getFloatOr("last", 0f);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putFloat("heat", heat);
        tag.putFloat("last", last);
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
        return  saveWithoutMetadata(registries);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        HeatBlockEntity.addToolTips(this, tooltip);

        CreateLang.translate("tooltip.create_new_age.generating")
                .style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        CreateLang.translate("tooltip.create_new_age.temperature.ps", StringFormatUtil.formatFloat(last))
                .style(ChatFormatting.AQUA).forGoggles(tooltip, 2);

        return true;
    }


    public void tick(BlockPos blockPos, Level world, BlockState blockState) {
        double generationMultiplier = CNAConfig.getServer().solarPanelHeatMultiplier.get();

        int dark = 0;
        if (world.isClientSide()) {
            // update sky could also work but this feels more right.
            dark = world.getSkyDarken();
        } else {
            dark = world.getSkyDarken();
        }
        dark *= 2;
        HeatBlockEntity.transferAround(this);
        HeatBlockEntity.handleOverheat(this);

        float light = world.getBrightness(LightLayer.SKY, blockPos.above()) - dark;
        last = (float) Math.max((light/15f)*energyPerSecond*generationMultiplier - Math.max(0, heat - (20 * energyPerSecond*generationMultiplier)), 0);
        addHeat(last);
        HeatBlockEntity.trySync(this);
    }
}
