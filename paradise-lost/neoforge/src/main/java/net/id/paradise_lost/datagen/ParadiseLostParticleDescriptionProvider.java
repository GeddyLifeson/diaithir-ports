package net.id.paradise_lost.datagen;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.particle.ParadiseLostParticleTypes;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public class ParadiseLostParticleDescriptionProvider extends ParticleDescriptionProvider {
    public ParadiseLostParticleDescriptionProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, helper);
    }

    @Override
    protected void addDescriptions() {
        sprite(ParadiseLostParticleTypes.CHERINE_FLAME, ModConstants.locate("cherine_flame"));
        spriteSet(ParadiseLostParticleTypes.MOTHER_AUREL_LEAF,
                ModConstants.locate("golden_leaf_1"),
                ModConstants.locate("golden_leaf_2"),
                ModConstants.locate("golden_leaf_3"));
        sprite(ParadiseLostParticleTypes.LEVITA_BLOOP, Identifier.withDefaultNamespace("generic_0"));
        spriteSet(ParadiseLostParticleTypes.LEVITATION_TOTEM,
                Identifier.withDefaultNamespace("glitter_7"),
                Identifier.withDefaultNamespace("glitter_6"),
                Identifier.withDefaultNamespace("glitter_5"),
                Identifier.withDefaultNamespace("glitter_4"),
                Identifier.withDefaultNamespace("glitter_3"),
                Identifier.withDefaultNamespace("glitter_2"),
                Identifier.withDefaultNamespace("glitter_1"),
                Identifier.withDefaultNamespace("glitter_0"));
        spriteSet(ParadiseLostParticleTypes.LIT_CLOUD,
                Identifier.withDefaultNamespace("generic_7"),
                Identifier.withDefaultNamespace("generic_6"),
                Identifier.withDefaultNamespace("generic_5"),
                Identifier.withDefaultNamespace("generic_4"),
                Identifier.withDefaultNamespace("generic_3"),
                Identifier.withDefaultNamespace("generic_2"),
                Identifier.withDefaultNamespace("generic_1"),
                Identifier.withDefaultNamespace("generic_0"));
        spriteSet(ParadiseLostParticleTypes.LEVITA_SPARKLE,
                ModConstants.locate("levita_sparkle_0"),
                ModConstants.locate("levita_sparkle_1"),
                ModConstants.locate("levita_sparkle_2"),
                ModConstants.locate("levita_sparkle_3"),
                ModConstants.locate("levita_sparkle_4"),
                ModConstants.locate("levita_sparkle_5"));
    }
}
