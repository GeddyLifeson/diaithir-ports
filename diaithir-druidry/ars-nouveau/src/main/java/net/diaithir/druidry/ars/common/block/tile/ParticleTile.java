package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

/**
 * Ported from Ars Nouveau (LGPL-3.0). The particle timeline / emitter (client particle system) was not ported in the 26.1 port,
 * so this only keeps the temporary-block lifetime logic.
 */
public class ParticleTile extends ModdedTile implements ITickable {
    public boolean isTemporary;
    public int ticksRemaining;

    public ParticleTile(BlockPos pos, BlockState state) {
        this(BlockRegistry.PARTICLE_TILE.get(), pos, state);
    }

    public ParticleTile(BlockEntityType<?> lightTile, BlockPos pos, BlockState state) {
        super(lightTile, pos, state);
    }

    @Override
    public void tick(Level level, BlockState state, BlockPos pos) {
        if (!level.isClientSide() && isTemporary) {
            if (ticksRemaining <= 0) {
                level.removeBlock(pos, false);
                return;
            }
            ticksRemaining--;
        }
    }

    public void playSound() {
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.isTemporary = input.getBooleanOr("isTemporary", false);
        this.ticksRemaining = input.getIntOr("ticksRemaining", 0);
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("isTemporary", isTemporary);
        output.putInt("ticksRemaining", ticksRemaining);
    }
}
