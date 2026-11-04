package net.diaithir.worldvisuals.ctm;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.Nullable;

/**
 * Block state model type "diaithirctm:connected". The variant's model uses the 4 by 4 sheet as its texture.
 * Each quad is moved to the tile for the face's neighbours; with the client flag off every face uses tile 0, the plain block.
 */
final class CtmModel implements BlockStateModel {
    /** The blockstate entry: {"type": "diaithirctm:connected", "model": "ns:block/name"}. */
    record Unbaked(Variant variant) implements CustomUnbakedBlockStateModel {
        static final MapCodec<Unbaked> CODEC = Variant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new CtmModel(variant.bake(baker));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            variant.resolveDependencies(resolver);
        }
    }

    private final BlockStateModelPart inner;

    private CtmModel(BlockStateModelPart inner) {
        this.inner = inner;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(new Part(inner, null, null, null));
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(new Part(inner, level, pos, state));
    }

    @Override
    public Material.Baked particleMaterial() {
        return inner.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return inner.materialFlags();
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return null;
    }

    private static final class Part implements BlockStateModelPart {
        private final BlockStateModelPart inner;
        private final @Nullable BlockAndTintGetter level;
        private final @Nullable BlockPos pos;
        private final @Nullable BlockState state;

        Part(BlockStateModelPart inner, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state) {
            this.inner = inner;
            this.level = level;
            this.pos = pos;
            this.state = state;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            List<BakedQuad> in = inner.getQuads(direction);
            if (in.isEmpty()) {
                return in;
            }
            List<BakedQuad> out = new ArrayList<>(in.size());
            for (BakedQuad q : in) {
                out.add(retile(q));
            }
            return out;
        }

        private BakedQuad retile(BakedQuad q) {
            Direction face = q.direction();
            int mask = 0;
            if (level != null && state != null && pos != null && ClientSetup.enabled()) {
                mask = Mask.of(face, d -> level.getBlockState(pos.relative(d)).is(state.getBlock()));
            }
            TextureAtlasSprite s = q.materialInfo().sprite();
            float u0 = s.getU0(), u1 = s.getU1(), v0 = s.getV0(), v1 = s.getV1();
            long[] uv = new long[4];
            for (int i = 0; i < 4; i++) {
                float ru = (UVPair.unpackU(q.packedUV(i)) - u0) / (u1 - u0);
                float rv = (UVPair.unpackV(q.packedUV(i)) - v0) / (v1 - v0);
                uv[i] = UVPair.pack(u0 + Mask.tile(ru, mask % 4) * (u1 - u0), v0 + Mask.tile(rv, mask / 4) * (v1 - v0));
            }
            return new BakedQuad(q.position0(), q.position1(), q.position2(), q.position3(), uv[0], uv[1], uv[2], uv[3],
                face, q.materialInfo(), q.bakedNormals(), q.bakedColors());
        }

        @Override
        public boolean useAmbientOcclusion() {
            return inner.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return inner.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return inner.materialFlags();
        }
    }
}
