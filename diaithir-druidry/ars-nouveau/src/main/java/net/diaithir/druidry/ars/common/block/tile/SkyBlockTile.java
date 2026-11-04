package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.api.entity.IDispellable;
import net.diaithir.druidry.ars.api.event.EventQueue;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.block.MirrorWeave;
import net.diaithir.druidry.ars.common.event.timed.SkyweaveVisibilityEvent;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public class SkyBlockTile extends MirrorWeaveTile implements ITickable, IDispellable {

    private boolean showFacade;
    public int previousLight;

    public SkyBlockTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.SKYWEAVE_TILE.get(), pos, state);
    }

    @Override
    public void tick() {
        if(showFacade && !level.isClientSide()) {
            if(getBlockState().getValue(MirrorWeave.LIGHT_LEVEL) != this.mimicState.getLightEmission(level, worldPosition)){
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(MirrorWeave.LIGHT_LEVEL, this.mimicState.getLightEmission(level, worldPosition)));
            }
            return;
        }
        if(!level.isClientSide() && level.isBrightOutside()){
            if(getBlockState().getValue(MirrorWeave.LIGHT_LEVEL) != 15){
                previousLight = getBlockState().getValue(MirrorWeave.LIGHT_LEVEL);
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(MirrorWeave.LIGHT_LEVEL, 15));
            }
        }
        if(!level.isClientSide() && !level.isBrightOutside()){
            if(getBlockState().getValue(MirrorWeave.LIGHT_LEVEL) != previousLight){
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(MirrorWeave.LIGHT_LEVEL, this.mimicState.getLightEmission(level, worldPosition)));
            }
        }
    }


    public void setShowFacade(boolean showFacade){
        if(this.showFacade == showFacade){
            return;
        }

        int ticks = 1;
        for(Direction d : Direction.values()){
            BlockPos offset = getBlockPos().relative(d);
            if(level.getBlockEntity(offset) instanceof SkyBlockTile neighbor){
                if(this.showFacade() == neighbor.showFacade()) {
                    ticks++;
                    EventQueue.getServerInstance().addEvent(new SkyweaveVisibilityEvent(neighbor, ticks, showFacade));
                }
            }
        }
        this.showFacade = showFacade;
        this.updateBlock();
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putBoolean("showFacade", showFacade);
        tag.putInt("previousLight", previousLight);
    }

    @Override
    protected void loadAdditional(ValueInput pTag) {
        super.loadAdditional(pTag);
        showFacade = pTag.getBooleanOr("showFacade", false);
        previousLight = pTag.getIntOr("previousLight", 0);
    }

    public boolean showFacade() {
        return showFacade;
    }

    @Override
    public boolean onDispel(@NotNull LivingEntity caster) {
        this.setShowFacade(!this.showFacade());
        return true;
    }
}
