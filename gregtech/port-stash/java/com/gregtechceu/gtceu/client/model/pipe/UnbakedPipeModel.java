package com.gregtechceu.gtceu.client.model.pipe;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.sprite.Material;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;



import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import static com.gregtechceu.gtceu.client.model.machine.MachineModelLoader.MISSING_MARKER;

public class UnbakedPipeModel implements IUnbakedGeometry<UnbakedPipeModel> {

    @Getter
    private final Map<@Nullable Direction, UnbakedModel> parts;
    @Getter
    private final Map<@NotNull Direction, UnbakedModel> restrictors;

    public UnbakedPipeModel(Map<@Nullable Direction, UnbakedModel> parts,
                            Map<@NotNull Direction, UnbakedModel> restrictors) {
        this.parts = parts;
        this.restrictors = restrictors;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) { return null; /* ported stub */ }

    @Override
    public void resolveParents(Function<Identifier, UnbakedModel> resolver, IGeometryBakingContext context) {  /* ported stub */ }
}
