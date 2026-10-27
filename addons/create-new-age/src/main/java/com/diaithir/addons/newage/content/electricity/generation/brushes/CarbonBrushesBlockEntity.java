package com.diaithir.addons.newage.content.electricity.generation.brushes;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
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
import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.electricity.generation.coil.GeneratorCoilBlock;
import com.diaithir.addons.newage.content.electricity.generation.coil.GeneratorCoilBlockEntity;
import com.diaithir.addons.newage.util.StringFormatUtil;
import com.diaithir.addons.newage.esl.EnergyHelper;
import com.diaithir.addons.newage.esl.EnergyStorage;
import com.diaithir.addons.newage.esl.SimpleEnergyStorage;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.diaithir.addons.newage.esl.TransactionStack;

import java.util.List;

public class CarbonBrushesBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {
    private final SimpleEnergyStorage storage;

    private int lastOutput = 0;


    public CarbonBrushesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        storage = new SimpleEnergyStorage(0)
                .setSupportsInsertion(false);

        EnergyStorage.registerForBlockEntity((blockEntity, direction) -> blockEntity.storage, CNABlockEntityTypes.CARBON_BRUSHES.get());

        setLazyTickRate(20);
    }

    @Override
    protected void write(ValueOutput compound, boolean clientPacket) {
        compound.putInt("lastOutput", lastOutput);
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(ValueInput compound, boolean clientPacket) {
        lastOutput = compound.getIntOr("lastOutput", 0);
        super.read(compound, clientPacket);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("tooltip.create_new_age.energy_stats")
                .style(ChatFormatting.WHITE).forGoggles(tooltip);

        CreateLang.translate("tooltip.create_new_age.energy_output")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CreateLang.translate("tooltip.create_new_age.energy_per_tick", StringFormatUtil.formatLong(lastOutput))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        return true;
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return storage;
    }

    private int syncOut = 0;

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;
        Direction facing = getBlockState().getValue(DirectionalKineticBlock.FACING);

        storage.setCapacity(lastOutput * 20L);

        int coilsLeft = CNAConfig.getServer().maxCoils.get();
        lastOutput = 0;
        coilsLeft = processCoil(worldPosition, facing, coilsLeft);
        processCoil(worldPosition, facing.getOpposite(), coilsLeft);

        try (Transaction t = TransactionStack.get().openOuter()) {
            EnergyHelper.insertToSurrounding(storage, getBlockPos(), getLevel(), storage.getStoredEnergy(), t);
            t.commit();
        }
    }

    @Override
    public void lazyTick() {
        if (level == null || level.isClientSide()) return;
        if (syncOut > 0) {
            syncOut = 0;
            setChanged();
            sendData();
        }
    }

    private int processCoil(BlockPos pos, Direction dir, int left) {
        if (left <= 0)
            return 0;

        pos = pos.relative(dir);

        if (level.getBlockEntity(pos) instanceof GeneratorCoilBlockEntity coil && coil.getBlockState().getValue(GeneratorCoilBlock.AXIS).test(dir)) {
            int energy = coil.takeGeneratedEnergy();
            lastOutput += energy;
            syncOut += energy;
            storage.internalInsert(energy, false);
            return processCoil(pos, dir, left - 1);
        }
        return left;
    }
}
