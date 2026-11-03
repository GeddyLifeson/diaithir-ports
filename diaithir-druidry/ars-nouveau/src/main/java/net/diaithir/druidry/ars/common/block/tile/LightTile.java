package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.api.particle.ParticleEmitter;
import net.diaithir.druidry.ars.api.particle.PropertyParticleOptions;
import net.diaithir.druidry.ars.api.particle.configurations.LightBlobMotion;
import net.diaithir.druidry.ars.api.particle.configurations.properties.ColorProperty;
import net.diaithir.druidry.ars.api.particle.timelines.LightTimeline;
import net.diaithir.druidry.ars.api.particle.timelines.TimelineEntryData;
import net.diaithir.druidry.ars.api.registry.ParticleColorRegistry;
import net.diaithir.druidry.ars.api.registry.ParticlePropertyRegistry;
import net.diaithir.druidry.ars.api.spell.ITimelined;
import net.diaithir.druidry.ars.api.util.IWololoable;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.registry.ModParticles;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.util.ANCodecs;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

public class LightTile extends ModdedTile implements ITickable, IWololoable, ITimelined<LightTimeline> {
    protected LightTimeline timeline = new LightTimeline();
    public ParticleColor color = ParticleColor.defaultParticleColor();
    public ParticleEmitter particleEmitter;

    public LightTile(BlockPos pos, BlockState state) {
        this(BlockRegistry.LIGHT_TILE.get(), pos, state);
    }

    public LightTile(BlockEntityType<?> lightTile, BlockPos pos, BlockState state) {
        super(lightTile, pos, state);
    }

    @Override
    public void tick(Level level, BlockState state, BlockPos pos) {
        if (level.isClientSide) {
            if (particleEmitter != null) {
                particleEmitter.tick(level);
            }
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag compound, HolderLookup.@NotNull Provider pRegistries) {
        super.loadAdditional(compound, pRegistries);
        this.color = ParticleColorRegistry.from(compound.getCompound("color"));
        if (compound.contains("timeline")) {
            this.timeline = ANCodecs.decode(LightTimeline.CODEC.codec(), compound.getCompound("timeline"));
        } else {
            this.timeline = new LightTimeline();
            PropertyParticleOptions particleOptions = new PropertyParticleOptions(ModParticles.NEW_GLOW_TYPE.get());
            ColorProperty colorProperty = new ColorProperty();
            colorProperty.particleColor = color;
            particleOptions.map.set(ParticlePropertyRegistry.COLOR_PROPERTY.get(), colorProperty);
            timeline.onTickEffect = new TimelineEntryData(new LightBlobMotion(), particleOptions);
        }
        particleEmitter = new ParticleEmitter(() -> this.getBlockPos().getCenter(), () -> new Vec2(0, 0), timeline.onTickEffect);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider pRegistries) {
        super.saveAdditional(tag, pRegistries);
        tag.put("color", color.serialize());
        tag.put("timeline", ANCodecs.encode(LightTimeline.CODEC.codec(), timeline));
    }

    public void setTimeline(LightTimeline timeline) {
        this.timeline = timeline;
        this.particleEmitter = new ParticleEmitter(() -> this.getBlockPos().getCenter(), () -> new Vec2(0, 0), timeline.onTickEffect);
        updateBlock();
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

    @Override
    public LightTimeline getTimeline() {
        return timeline;
    }
}
