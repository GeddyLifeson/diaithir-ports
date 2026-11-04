package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Block state model for pipes and cables: a core cube plus one arm per connected side. The connection mask is read
 * from the block entity at render time (26.1 passes level and pos into collectParts), replacing the old
 * ModelData based PipeModel.
 */
public class PipeStateModel implements BlockStateModel {

    public record Unbaked(Variant core, Variant down, Variant up, Variant north, Variant south, Variant west,
                          Variant east) implements CustomUnbakedBlockStateModel {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Variant.CODEC.fieldOf("core").forGetter(Unbaked::core),
                Variant.CODEC.fieldOf("down").forGetter(Unbaked::down),
                Variant.CODEC.fieldOf("up").forGetter(Unbaked::up),
                Variant.CODEC.fieldOf("north").forGetter(Unbaked::north),
                Variant.CODEC.fieldOf("south").forGetter(Unbaked::south),
                Variant.CODEC.fieldOf("west").forGetter(Unbaked::west),
                Variant.CODEC.fieldOf("east").forGetter(Unbaked::east)).apply(i, Unbaked::new));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new PipeStateModel(core.bake(baker), new BlockStateModelPart[] { down.bake(baker), up.bake(baker),
                    north.bake(baker), south.bake(baker), west.bake(baker), east.bake(baker) });
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            core.resolveDependencies(resolver);
            down.resolveDependencies(resolver);
            up.resolveDependencies(resolver);
            north.resolveDependencies(resolver);
            south.resolveDependencies(resolver);
            west.resolveDependencies(resolver);
            east.resolveDependencies(resolver);
        }
    }

    private final BlockStateModelPart core;
    private final BlockStateModelPart[] arms;

    public PipeStateModel(BlockStateModelPart core, BlockStateModelPart[] arms) {
        this.core = core;
        this.arms = arms;
    }

    private static int connections(BlockAndTintGetter level, BlockPos pos) {
        if (level != null && pos != null && level.getBlockEntity(pos) instanceof PipeBlockEntity<?, ?> pipe) {
            return pipe.getConnections();
        }
        return 0;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        output.add(core);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockStateModelPart> output) {
        output.add(core);
        int mask = connections(level, pos);
        for (Direction d : Direction.values()) {
            if (PipeBlockEntity.isConnected(mask, d)) output.add(arms[d.ordinal()]);
        }
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return connections(level, pos);
    }

    @Override
    public Material.Baked particleMaterial() {
        return core.particleMaterial();
    }

    @Override
    public int materialFlags() {
        int flags = core.materialFlags();
        for (var a : arms) flags |= a.materialFlags();
        return flags;
    }
}
