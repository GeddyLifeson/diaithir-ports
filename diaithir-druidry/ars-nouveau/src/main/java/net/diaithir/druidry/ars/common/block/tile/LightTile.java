package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.api.registry.ParticleColorRegistry;
import net.diaithir.druidry.ars.api.util.IWololoable;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

/**
 * Ported from Ars Nouveau (LGPL-3.0). The particle timeline/emitter was not ported (26.1); only the colour is kept.
 */
public class LightTile extends ModdedTile implements ITickable, IWololoable {
    public ParticleColor color = ParticleColor.defaultParticleColor();

    public LightTile(BlockPos pos, BlockState state) {
        this(BlockRegistry.LIGHT_TILE.get(), pos, state);
    }

    public LightTile(BlockEntityType<?> lightTile, BlockPos pos, BlockState state) {
        super(lightTile, pos, state);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.color = input.read("color", CompoundTag.CODEC).map(ParticleColorRegistry::from).orElse(ParticleColor.defaultParticleColor());
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.store("color", CompoundTag.CODEC, color.serialize());
    }

    @Override
    public void setColor(ParticleColor color) {
        this.color = color;
        updateBlock();
    }

    @Override
    public ParticleColor getColor() {
        return color;
    }
}
