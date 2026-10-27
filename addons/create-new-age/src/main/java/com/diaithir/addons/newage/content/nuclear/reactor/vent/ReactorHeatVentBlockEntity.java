package com.diaithir.addons.newage.content.nuclear.reactor.vent;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.heat.HeatBlockEntity;
import com.diaithir.addons.newage.content.nuclear.reactor.RodFindingReactorBlockEntity;
import com.diaithir.addons.newage.content.nuclear.reactor.rod.ReactorRodBlockEntity;
import com.diaithir.addons.newage.util.StringFormatUtil;

import java.util.LinkedList;
import java.util.List;

public class ReactorHeatVentBlockEntity extends RodFindingReactorBlockEntity implements HeatBlockEntity, IHaveGoggleInformation {

    private float extract;

    public ReactorHeatVentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public float heat = 0;

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        heat = tag.getFloatOr("heat", 0f);
        extract = tag.getFloatOr("extract", 0f);
        super.read(tag, clientPacket);
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        tag.putFloat("heat", heat);
        tag.putFloat("extract", extract);
        super.write(tag, clientPacket);
    }

    @Override
    public boolean canConnect(Direction from) {
        return from != getBlockState().getValue(BlockStateProperties.FACING);
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

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    int tick = 0;
    public void tick(BlockPos pos, Level world, BlockState state) {
        tick++;
        if (tick >= 20) {
            double multiplier = CNAConfig.getServer().overheatingMultiplier.get();
            HeatBlockEntity.handleOverheat(this);
            tick = 0;
            extract = 0;

            transferAroundNonRodOnly(this);

            List<ReactorRodBlockEntity> rods = new LinkedList<>();
            for (Direction dir : Direction.values()) {
                findRods(rods, dir);
            }

            float cap = multiplier > 0 ? (float) (maxHeat() * multiplier) : Float.MAX_VALUE;
            for (ReactorRodBlockEntity rod : rods) {
                float total = Math.min(rod.heat, cap - heat);
                if (total <= 0) {
                    break;
                }
                rod.heat -= total;
                setChanged();
                extract += total;
                heat += total;
            }
        }
    }

    static <T extends BlockEntity & HeatBlockEntity> void transferAroundNonRodOnly(T self) {
        if (self.getLevel() == null) {
            return;
        }
        float totalToAverage = self.getHeat();
        int totalBlocks = 1;
        HeatBlockEntity[] setters = new HeatBlockEntity[6];
        for (int i = 0 ; i < 6 ; i++) {
            Direction value = Direction.values()[i];
            BlockEntity entity = self.getLevel().getBlockEntity(self.getBlockPos().relative(value));
            if (entity instanceof HeatBlockEntity hbe && !(entity instanceof ReactorRodBlockEntity) && hbe.canAdd(value)) {
                setters[i] = hbe;
                totalToAverage += hbe.getHeat();
                totalBlocks++;
            }
        }
        HeatBlockEntity.average(self, totalToAverage, totalBlocks, setters);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        HeatBlockEntity.addToolTips(this, tooltip);

        CreateLang.translate("tooltip.create_new_age.extracting")
                .style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        CreateLang.translate("tooltip.create_new_age.temperature", StringFormatUtil.formatFloat(extract))
                .style(ChatFormatting.AQUA).forGoggles(tooltip, 2);
        return true;
    }
}