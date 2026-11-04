package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.api.entity.IDispellable;
import net.diaithir.druidry.ars.api.registry.ParticleColorRegistry;
import net.diaithir.druidry.ars.api.util.IWololoable;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class MageBlockTile extends ModdedTile implements ITickable, IDispellable, IWololoable {

    int age;
    public boolean isPermanent;
    public double lengthModifier;
    public ParticleColor color = ParticleColor.defaultParticleColor();

    public MageBlockTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.MAGE_BLOCK_TILE.get(), pos, state);
    }

    @Override
    public void tick() {
        if (isPermanent)
            return;
        if (!level.isClientSide()) {
            age++;
            //15 seconds
            if (age > (20 * 15 + 20 * 5 * lengthModifier)) {
                level.destroyBlock(this.getBlockPos(), false);
                level.removeBlockEntity(this.getBlockPos());
            }
        }
    }


    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 8);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.age = input.getIntOr("age", 0);
        this.color = input.read("lightColor", CompoundTag.CODEC).map(ParticleColorRegistry::from).orElse(ParticleColor.defaultParticleColor());
        this.isPermanent = input.getBooleanOr("permanent", false);
        this.lengthModifier = input.getDoubleOr("modifier", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("age", age);
        output.store("lightColor", CompoundTag.CODEC, color.serialize());
        output.putBoolean("permanent", isPermanent);
        output.putDouble("modifier", lengthModifier);
    }

    @Override
    public boolean onDispel(@NotNull LivingEntity caster) {
        level.destroyBlock(this.getBlockPos(), false);
        level.removeBlockEntity(this.getBlockPos());
        return true;
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
