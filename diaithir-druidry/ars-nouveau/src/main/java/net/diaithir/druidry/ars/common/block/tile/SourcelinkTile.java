package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.api.source.AbstractSourceMachine;
import net.diaithir.druidry.ars.api.source.ISpecialSourceProvider;
import net.diaithir.druidry.ars.api.source.SourcelinkEventQueue;
import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.api.util.SourceUtil;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.capability.SourceStorage;
import net.diaithir.druidry.ars.common.entity.EntityFollowProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SourcelinkTile extends AbstractSourceMachine implements ITickable {

    int progress;
    public boolean isDisabled = false;
    public boolean registered = false;

    public SourcelinkTile(BlockEntityType<?> sourceLinkTile, BlockPos pos, BlockState state) {
        super(sourceLinkTile, pos, state);
    }

    @Override
    public void tick() {
        if (level.isClientSide())
            return;
        if (level.getGameTime() % 120 == 0 && usesEventQueue()) {
            SourcelinkEventQueue.addPosition(level, this.worldPosition);
            registered = true;
        }

        if (level.getGameTime() % 100 == 0 && getSource() > 0) {
            List<ISpecialSourceProvider> providers = SourceUtil.canGiveSource(worldPosition, level, 5);
            if (!providers.isEmpty()) {
                transferSource(this, providers.getFirst().getSource());
                spawnFollow(this.worldPosition, providers.getFirst().getCurrentPos());
            }
        }
    }

    private void spawnFollow(BlockPos from, BlockPos to) {
        if (level instanceof ServerLevel serverLevel) {
            var c = this.getColor();
            EntityFollowProjectile.spawn(serverLevel, from, to, c.getRedInt(), c.getGreenInt(), c.getBlueInt());
        }
    }

    public List<ArcanePedestalTile> getSurroundingPedestals() {
        List<ArcanePedestalTile> inventories = new java.util.ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(getBlockPos().below().east().north(), getBlockPos().above().west().south())) {
            if (level.getBlockEntity(p) instanceof ArcanePedestalTile pedestal) {
                inventories.add(pedestal);
            }
        }
        return inventories;
    }

    public void getManaEvent(BlockPos sourcePos, int total) {
        this.addSource(total);
        spawnFollow(sourcePos, this.worldPosition);
    }

    public boolean eventInRange(BlockPos sourcePos, @Nullable Event event) {
        return BlockUtil.distanceFrom(level, this.worldPosition, sourcePos) <= 15;
    }

    public boolean usesEventQueue() {
        return false;
    }

    public void doRandomAction() {
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput tag) {
        super.loadAdditional(tag);
        progress = tag.getIntOr("progress", 0);
        isDisabled = tag.getBooleanOr("disabled", false);
    }

    @Override
    protected @NotNull SourceStorage createDefaultStorage() {
        return new SourceStorage(20000, 10000, 10000, 0) {
            @Override
            public boolean canReceive() {
                return false;
            }
        };
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("progress", progress);
        tag.putBoolean("disabled", isDisabled);
    }
}
