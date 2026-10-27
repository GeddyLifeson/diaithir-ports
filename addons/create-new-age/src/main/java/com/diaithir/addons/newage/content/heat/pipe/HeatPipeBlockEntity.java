package com.diaithir.addons.newage.content.heat.pipe;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.diaithir.addons.newage.content.heat.HeatBlockEntity;
import com.diaithir.addons.newage.util.StringFormatUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeatPipeBlockEntity extends BlockEntity implements HeatBlockEntity, IHaveGoggleInformation {
    public float generating;

    public HeatPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public float heat = 0;

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        heat = tag.getFloatOr("heat", 0f);
        generating = tag.getFloatOr("generating", 0f);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putFloat("heat", heat);
        tag.putFloat("generating", generating);
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

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        HeatBlockEntity.addToolTips(this, tooltip);

        if (generating > 0.05) {
            CreateLang.translate("tooltip.create_new_age.generating")
                    .style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
            CreateLang.translate("tooltip.create_new_age.temperature.ps", StringFormatUtil.formatFloat(generating))
                    .style(ChatFormatting.AQUA).forGoggles(tooltip, 2);
        }

        return true;
    }

}
