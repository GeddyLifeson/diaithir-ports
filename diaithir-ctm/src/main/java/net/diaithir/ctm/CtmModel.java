package net.diaithir.ctm;

import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jspecify.annotations.Nullable;

/** Wraps a block's model so each face picks its tile of the connected sheet from the neighbours. */
final class CtmModel extends DelegateBlockStateModel {
    private final TextureAtlasSprite sheet;

    CtmModel(BlockStateModel delegate, TextureAtlasSprite sheet) {
        super(delegate);
        this.sheet = sheet;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        List<BlockStateModelPart> inner = new ArrayList<>();
        delegate.collectParts(level, pos, state, random, inner);
        for (BlockStateModelPart part : inner) {
            parts.add(new Part(part, level, pos, state, sheet));
        }
    }

    private static final class Part implements BlockStateModelPart {
        private final BlockStateModelPart inner;
        private final BlockAndTintGetter level;
        private final BlockPos pos;
        private final BlockState state;
        private final TextureAtlasSprite sheet;

        Part(BlockStateModelPart inner, BlockAndTintGetter level, BlockPos pos, BlockState state, TextureAtlasSprite sheet) {
            this.sheet = sheet;
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
            int mask = Mask.of(face, d -> level.getBlockState(pos.relative(d)).is(state.getBlock()));
            BakedQuad.MaterialInfo m = q.materialInfo();
            TextureAtlasSprite s = m.sprite();
            float ou0 = s.getU0(), ou1 = s.getU1(), ov0 = s.getV0(), ov1 = s.getV1();
            float u0 = sheet.getU0(), u1 = sheet.getU1(), v0 = sheet.getV0(), v1 = sheet.getV1();
            long[] uv = new long[4];
            for (int i = 0; i < 4; i++) {
                float ru = (UVPair.unpackU(q.packedUV(i)) - ou0) / (ou1 - ou0);
                float rv = (UVPair.unpackV(q.packedUV(i)) - ov0) / (ov1 - ov0);
                uv[i] = UVPair.pack(u0 + Mask.tile(ru, mask % 4) * (u1 - u0), v0 + Mask.tile(rv, mask / 4) * (v1 - v0));
            }
            return new BakedQuad(q.position0(), q.position1(), q.position2(), q.position3(), uv[0], uv[1], uv[2], uv[3],
                face, new BakedQuad.MaterialInfo(sheet, m.layer(), m.itemRenderType(), m.tintIndex(), m.shade(), m.lightEmission(), m.ambientOcclusion()),
                q.bakedNormals(), q.bakedColors());
        }

        @Override
        public boolean useAmbientOcclusion() {
            return inner.useAmbientOcclusion();
        }

        @Override
        public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() {
            return inner.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return inner.materialFlags();
        }
    }
}
