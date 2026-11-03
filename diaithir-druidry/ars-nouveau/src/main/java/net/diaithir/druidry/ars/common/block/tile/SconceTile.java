package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.api.entity.IDispellable;
import net.diaithir.druidry.ars.api.registry.ParticleColorRegistry;
import net.diaithir.druidry.ars.api.spell.ILightable;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.diaithir.druidry.ars.api.spell.SpellStats;
import net.diaithir.druidry.ars.api.util.IWololoable;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.block.SconceBlock;
import net.diaithir.druidry.ars.common.spell.augment.AugmentDampen;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Ported from Ars Nouveau (LGPL-3.0). The particle timeline/emitter (flame particles) was not ported (26.1).
 */
public class SconceTile extends ModdedTile implements ILightable, ITickable, IDispellable, IWololoable {
    protected ParticleColor color = ParticleColor.defaultParticleColor();
    public boolean lit;

    public SconceTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.SCONCE_TILE, pos, state);
    }

    public SconceTile(BlockEntityType type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.color = input.read("color", CompoundTag.CODEC).map(ParticleColorRegistry::from).orElse(ParticleColor.defaultParticleColor());
        lit = input.getBooleanOr("lit", false);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("color", CompoundTag.CODEC, color.serialize());
        output.putBoolean("lit", lit);
    }

    @Override
    public void onLight(HitResult rayTraceResult, Level world, LivingEntity shooter, SpellStats stats, SpellContext spellContext) {
        lit = true;
        if (rayTraceResult instanceof BlockHitResult) {
            BlockState state = world.getBlockState(((BlockHitResult) rayTraceResult).getBlockPos());
            world.setBlock(getBlockPos(), state.setValue(SconceBlock.LIGHT_LEVEL, Math.min(Math.max(0, 15 - stats.getBuffCount(AugmentDampen.INSTANCE)), 15)), 3);
            world.sendBlockUpdated(((BlockHitResult) rayTraceResult).getBlockPos(), state,
                    state.setValue(SconceBlock.LIGHT_LEVEL, Math.min(Math.max(0, 15 - stats.getBuffCount(AugmentDampen.INSTANCE)), 15)), 3);
        }
        updateBlock();
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean onDispel(@Nullable LivingEntity caster) {
        this.lit = false;
        level.setBlock(getBlockPos(), level.getBlockState(getBlockPos()).setValue(SconceBlock.LIGHT_LEVEL, 0), 3);
        updateBlock();
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
