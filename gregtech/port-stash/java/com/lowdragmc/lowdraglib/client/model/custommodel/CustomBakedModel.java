package com.lowdragmc.lowdraglib.client.model.custommodel;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Submap;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel.RendererBakedModel.POS;
import static com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel.RendererBakedModel.WORLD;

/**
 * Used to bake the model with emissive effect, as well as connected textures.
 */
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class CustomBakedModel<T extends BakedModel> extends BakedModelWrapper<T> {
    private final Table<Direction, Connections, List<BakedQuad>> sideCache;
    private final List<BakedQuad> noSideCache;

    public CustomBakedModel(T parent) {
        super(parent);
        this.sideCache = Tables.newCustomTable(new EnumMap<>(Direction.class), HashMap::new);
        this.noSideCache = new ArrayList<>();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) { return null; /* ported stub */ }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) { return null; /* ported stub */ }

    @Nonnull
    public List<BakedQuad> getCustomQuads(BlockAndTintGetter level, BlockPos pos, @Nonnull BlockState state, @Nullable Direction side, RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) { return null; /* ported stub */ }

    public static List<BakedQuad> reBakeCustomQuads(List<BakedQuad> quads, BlockAndTintGetter level, BlockPos pos, @Nonnull BlockState state, @Nullable Direction side, float offset) {
        return buildCustomQuads(Connections.checkConnections(level, pos, state, side), quads, offset);
    }

    public static List<BakedQuad> buildCustomQuads(Connections connections, List<BakedQuad> base, float offset) { return null; /* ported stub */ }

    protected static Quad makeQuad(BakedQuad bq, LDLMetadataSection section, float offset) {
        Quad q = Quad.from(bq, offset);
        if (section.emissive) {
            q = q.setLight(15, 15);
        }
        return q;
    }
}
