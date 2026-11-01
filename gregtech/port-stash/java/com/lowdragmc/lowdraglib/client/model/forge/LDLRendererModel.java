package com.lowdragmc.lowdraglib.client.model.forge;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;



import net.minecraft.util.TriState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2022/05/28
 * @implNote LDLModel, use vanilla way to improve model rendering
 */
@ParametersAreNonnullByDefault
public class LDLRendererModel implements IUnbakedGeometry<LDLRendererModel> {
    public static final LDLRendererModel INSTANCE = new LDLRendererModel();

    private LDLRendererModel() {}

    @Override
    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker arg, Function<Material, TextureAtlasSprite> function, ModelState arg2, ItemOverrides arg3) { return null; /* ported stub */ }

    public static final class RendererBakedModel implements BakedModel {

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) { return null; /* ported stub */ }

        @Override
        public boolean useAmbientOcclusion() { return false; /* ported stub */ }

        @Override
        public boolean isGui3d() { return false; /* ported stub */ }

        @Override
        public boolean usesBlockLight() { return false; /* ported stub */ }

        @Override
        public boolean isCustomRenderer() { return false; /* ported stub */ }

        @Override
        public TextureAtlasSprite getParticleIcon() { return null; /* ported stub */ }

        @Override
        public ItemOverrides getOverrides() { return null; /* ported stub */ }

        // forge

        public static final ModelProperty<IRenderer> IRENDERER = new ModelProperty<>();
        public static final ModelProperty<BlockAndTintGetter> WORLD = new ModelProperty<>();
        public static final ModelProperty<BlockPos> POS = new ModelProperty<>();
        public static final ModelProperty<ModelData> MODEL_DATA = new ModelProperty<>();


        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) { return null; /* ported stub */ }

        @Override
        public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) { return null; /* ported stub */ }


        @Override
        public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) { return null; /* ported stub */ }

        @Override
        public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) { return null; /* ported stub */ }

    }

    public static final class Loader implements IGeometryLoader<LDLRendererModel> {

        public static final Loader INSTANCE = new Loader();
        private Loader() {}

        @Override
        public LDLRendererModel read(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException { return null; /* ported stub */ }
    }
}
