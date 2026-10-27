package com.github.alexmodguy.alexscaves.server.level.structure;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.level.structure.piece.AbyssalRuinsStructurePiece;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class AbyssalRuinsStructure extends Structure {

    public static final MapCodec<AbyssalRuinsStructure> CODEC = simpleCodec(AbyssalRuinsStructure::new);

    private static final Identifier[] RUINS_NBT = new Identifier[]{
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "abyssal_ruins_0"),
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "abyssal_ruins_1"),
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "abyssal_ruins_2"),
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "abyssal_ruins_3"),
    };

    public AbyssalRuinsStructure(StructureSettings settings) {
        super(settings);
    }

    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!CaveMaskBiomeSource.insideParent(context, com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry.ABYSSAL_CHASM)) {
            return Optional.empty();
        }
        Rotation rotation = Rotation.getRandom(context.random());
        LevelHeightAccessor levelHeight = context.heightAccessor();
        int y = context.chunkGenerator().getBaseHeight(context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ(), Heightmap.Types.OCEAN_FLOOR_WG, levelHeight, context.randomState());
        if (y > context.chunkGenerator().getSeaLevel()) {
            return Optional.empty();
        }
        BlockPos blockpos = new BlockPos(context.chunkPos().getMinBlockX(), context.chunkGenerator().getMinY() + 15, context.chunkPos().getMinBlockZ());
        Identifier res = Util.getRandom(RUINS_NBT, context.random());
        return Optional.of(new GenerationStub(blockpos, (piecesBuilder -> piecesBuilder.addPiece(new AbyssalRuinsStructurePiece(context.structureTemplateManager(), res, blockpos, rotation)))));
    }

    @Override
    public StructureType<?> type() {
        return ACStructureRegistry.ABYSSAL_RUINS.get();
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.UNDERGROUND_STRUCTURES;
    }


}
