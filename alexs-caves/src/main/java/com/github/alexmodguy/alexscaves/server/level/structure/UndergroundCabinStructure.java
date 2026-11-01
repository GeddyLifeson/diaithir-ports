package com.github.alexmodguy.alexscaves.server.level.structure;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.level.structure.piece.UndergroundCabinStructurePiece;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
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

public class UndergroundCabinStructure extends Structure {

    public static final MapCodec<UndergroundCabinStructure> CODEC = simpleCodec((settings) -> new UndergroundCabinStructure(settings));

    private static final Identifier[] CABIN_NBT = new Identifier[]{
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "underground_cabin_0"),
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "underground_cabin_1"),
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "underground_cabin_2"),
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "underground_cabin_3"),
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "underground_cabin_4"),
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "underground_cabin_5"),
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "underground_cabin_6")
    };

    public UndergroundCabinStructure(StructureSettings settings) {
        super(settings);
    }

    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Rotation rotation = Rotation.getRandom(context.random());
        LevelHeightAccessor levelHeight = context.heightAccessor();
        int y = context.chunkGenerator().getBaseHeight(context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ(), Heightmap.Types.OCEAN_FLOOR_WG, levelHeight, context.randomState()) - 20;
        int maxHeight = y - 14 - context.random().nextInt(15);
        BlockPos blockpos = new BlockPos(context.chunkPos().getMinBlockX(), maxHeight, context.chunkPos().getMinBlockZ());
        Identifier res = Util.getRandom(CABIN_NBT, context.random());
        return Optional.of(new GenerationStub(blockpos, (piecesBuilder -> piecesBuilder.addPiece(new UndergroundCabinStructurePiece(context.structureTemplateManager(), res, blockpos, rotation)))));
    }

    @Override
    public StructureType<?> type() {
        return ACStructureRegistry.UNDERGROUND_CABIN.get();
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.UNDERGROUND_STRUCTURES;
    }


}
