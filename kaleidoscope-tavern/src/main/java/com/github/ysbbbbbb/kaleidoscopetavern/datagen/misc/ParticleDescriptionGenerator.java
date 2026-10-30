package com.github.ysbbbbbb.kaleidoscopetavern.datagen.misc;


import com.github.ysbbbbbb.kaleidoscopetavern.init.ModParticles;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

public class ParticleDescriptionGenerator extends ParticleDescriptionProvider {
    public ParticleDescriptionGenerator(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        this.spriteSet(ModParticles.WATER_TAP_DRIP.get(), Identifier.withDefaultNamespace("drip_hang"));
        this.spriteSet(ModParticles.LAVA_TAP_DRIP.get(), Identifier.withDefaultNamespace("drip_hang"));
    }
}
