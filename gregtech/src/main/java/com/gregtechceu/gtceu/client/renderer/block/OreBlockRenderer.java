package com.gregtechceu.gtceu.client.renderer.block;

import net.minecraft.client.data.models.model.DelegatedModel;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gregtechceu.gtceu.utils.memoization.function.MemoizedBiFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;


import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class OreBlockRenderer {

    protected static final Set<OreBlockRenderer> MODELS = new HashSet<>();

    protected static final JsonObject NULL_ELEMENT_MARKER = new JsonObject();
    protected static final MemoizedBiFunction<MaterialIconType, MaterialIconSet, JsonObject> TEMPLATE_MODEL_CACHE = GTMemoizer
            .memoizeFunctionWeakIdent(OreBlockRenderer::loadTemplateOreModel);

    // First format key is material set name, 2nd is stone type prefix's name, 3rd is icon type's name
    public static final String ORE_MODEL_NAME_FORMAT = "block/material_sets/%s/ores/%s/%s";

    protected final MaterialBlock block;

    public static void create(MaterialBlock block) {
        MODELS.add(new OreBlockRenderer(block));
    }

    public OreBlockRenderer(MaterialBlock block) {
        this.block = block;
    }

    @ApiStatus.Internal
    public static void reinitModels() { /* ported stub */ }

    /// This is called for every combination of tag prefix + icon type + icon set
    protected static void copyOreModelWithBaseStone(TagPrefix tagPrefix, TagPrefix.OreType oreType,
                                                    MaterialIconType iconType, MaterialIconSet iconSet) {  /* ported stub */ }

    private static JsonObject loadTemplateOreModel(MaterialIconType iconType, MaterialIconSet iconSet) { return null; /* ported stub */ }
}
