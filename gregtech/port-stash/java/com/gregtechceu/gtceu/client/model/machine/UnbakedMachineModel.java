package com.gregtechceu.gtceu.client.model.machine;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.sprite.Material;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartBakedModel;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartUnbakedModel;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;



import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class UnbakedMachineModel implements IUnbakedGeometry<UnbakedMachineModel> {

    @Getter
    private final MachineDefinition definition;
    @Getter
    private final Map<MachineRenderState, UnbakedModel> models;
    @Nullable
    @Getter
    private final MultiPartUnbakedModel multiPart;
    @Getter
    private final List<DynamicRender<?, ?>> dynamicRenders;
    private final Set<String> replaceableTextures;
    private final Map<String, Identifier> textureOverrides;

    public UnbakedMachineModel(MachineDefinition definition,
                               Map<MachineRenderState, UnbakedModel> models,
                               @Nullable MultiPartUnbakedModel multiPart,
                               List<DynamicRender<?, ?>> dynamicRenders,

                               Set<String> replaceableTextures,
                               Map<String, Identifier> textureOverrides) {
        this.definition = definition;
        this.models = models;
        this.multiPart = multiPart;
        this.dynamicRenders = dynamicRenders;
        this.replaceableTextures = replaceableTextures;
        this.textureOverrides = textureOverrides;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) { return null; /* ported stub */ }

    @Override
    public void resolveParents(Function<Identifier, UnbakedModel> resolver, IGeometryBakingContext context) {  /* ported stub */ }
}
