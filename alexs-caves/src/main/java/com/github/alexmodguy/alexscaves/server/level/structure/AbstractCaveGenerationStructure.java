package com.github.alexmodguy.alexscaves.server.level.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractCaveGenerationStructure extends Structure {

    private final ResourceKey<Biome> matchingBiome;

    protected AbstractCaveGenerationStructure(StructureSettings settings, ResourceKey<Biome> matchingBiome) {
        super(settings);
        this.matchingBiome = matchingBiome;
    }

    /** Gate for the whole structure start (terrain checks the 3D biome source used to provide). */
    protected boolean canStartAt(Structure.GenerationContext context) {
        return true;
    }

    protected int surfaceAt(Structure.GenerationContext context, int x, int z) {
        return context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
    }

    public Optional<GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        if (!canStartAt(context)) return Optional.empty();
        return atYCaveBiomePoint(context, Heightmap.Types.OCEAN_FLOOR_WG, (builder) -> {
            this.generatePieces(builder, context);
        });
    }

    protected Optional<Structure.GenerationStub> atYCaveBiomePoint(Structure.GenerationContext context, Heightmap.Types heightMap, Consumer<StructurePiecesBuilder> builderConsumer) {
        ChunkPos chunkpos = context.chunkPos();
        int i = chunkpos.getMiddleBlockX();
        int j = chunkpos.getMiddleBlockZ();
        int k = getGenerateYHeight(context.random(), i, j);
        return Optional.of(new Structure.GenerationStub(new BlockPos(i, k, j), builderConsumer));
    }

    public void generatePieces(StructurePiecesBuilder builder, Structure.GenerationContext context) {
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x(), context.chunkPos().z());
        int i = context.chunkPos().getMinBlockX();
        int j = context.chunkPos().getMinBlockZ();
        int k = context.chunkGenerator().getSeaLevel();
        BlockPos center = new BlockPos(i, getGenerateYHeight(context.random(), i, j), j);
        int heightRad = getHeightRadius(context.random(), context.chunkGenerator().getSeaLevel());
        int widthRad = getWidthRadius(context.random());
        // keep the bowl under the terrain: squash its height to the lowest surface across the footprint
        int lowSurface = surfaceAt(context, i, j);
        for (int dx = -1; dx <= 1; dx += 2) for (int dz = -1; dz <= 1; dz += 2) {
            lowSurface = Math.min(lowSurface, surfaceAt(context, i + dx * widthRad / 2, j + dz * widthRad / 2));
        }
        heightRad = Math.min(heightRad, (lowSurface - 14 - center.getY()));
        if (heightRad < 40) return;
        int biomeUp = biomeContinuesInDirectionFor(CaveMaskBiomeSource.of(context, matchingBiome), context.randomState(), Direction.UP, center, heightRad) + getYExpandUp();
        int biomeDown = biomeContinuesInDirectionFor(CaveMaskBiomeSource.of(context, matchingBiome), context.randomState(), Direction.DOWN, center, heightRad) + getYExpandDown();
        BlockPos ground = center.below(biomeDown - 2);
        int biomeEast = biomeContinuesInDirectionFor(CaveMaskBiomeSource.of(context, matchingBiome), context.randomState(), Direction.EAST, ground, widthRad);
        int biomeWest = biomeContinuesInDirectionFor(CaveMaskBiomeSource.of(context, matchingBiome), context.randomState(), Direction.WEST, ground, widthRad);
        int biomeNorth = biomeContinuesInDirectionFor(CaveMaskBiomeSource.of(context, matchingBiome), context.randomState(), Direction.NORTH, ground, widthRad);
        int biomeSouth = biomeContinuesInDirectionFor(CaveMaskBiomeSource.of(context, matchingBiome), context.randomState(), Direction.SOUTH, ground, widthRad);
        int widthBlocks = (biomeEast + biomeWest + biomeNorth + biomeSouth) / 4;
        int heightBlocks = (biomeUp + biomeDown) / 2;
        int widthChunks = (int) Math.ceil((widthBlocks + 16) / 16F / 2F) + 2;
        int heightChunks = getHeightOverride((int) Math.ceil((heightBlocks + 16) / 16F / 2F));
        int minYChunks = (int) Math.ceil(context.chunkGenerator().getMinY() / 16F);
        int maxYChunks = (int) Math.ceil(320 / 16F);
        for (int chunkX = -widthChunks; chunkX <= widthChunks; chunkX++) {
            for (int chunkZ = -widthChunks; chunkZ <= widthChunks; chunkZ++) {
                for (int chunkY = Math.max(-heightChunks, minYChunks); chunkY <= Math.min(heightChunks, maxYChunks); chunkY++) {
                    StructurePiece piece = createPiece(center.offset(new BlockPos(chunkX * 16, chunkY * 16, chunkZ * 16)), center, heightBlocks, widthBlocks, context.randomState());
                    builder.addPiece(piece);
                }
            }
        }
    }

    protected int getHeightOverride(int heightIn) {
        return heightIn;
    }

    protected int getYExpandUp() {
        return -5;
    }

    protected int getYExpandDown() {
        return -5;
    }

    protected abstract StructurePiece createPiece(BlockPos offset, BlockPos center, int heightBlocks, int widthBlocks, RandomState randomState);

    private static Holder<Biome> getBiomeHolder(BiomeSource biomeSource, RandomState randomState, BlockPos pos) {
        return biomeSource.getNoiseBiome(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()), randomState.sampler());
    }

    protected int biomeContinuesInDirectionFor(BiomeSource biomeSource, RandomState randomState, Direction direction, BlockPos start, int cutoff) {
        int i = 0;
        while (i < cutoff) {
            BlockPos check = start.relative(direction, i);
            Holder<Biome> biomeHolder = getBiomeHolder(biomeSource, randomState, check);
            if (!biomeHolder.is(matchingBiome)) {
                break;
            }
            i += 16;
        }
        return Math.min(i, cutoff);
    }

    public abstract int getGenerateYHeight(WorldgenRandom random, int x, int y);

    public abstract int getWidthRadius(WorldgenRandom random);

    public abstract int getHeightRadius(WorldgenRandom random, int seaLevel);

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.RAW_GENERATION;
    }
}
