package com.gregtechceu.gtceu.client.renderer.block;

import net.minecraft.client.data.models.model.DelegatedModel;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;


import net.minecraft.core.registries.BuiltInRegistries;


import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public class MaterialBlockRenderer {

    private static final Set<MaterialBlockRenderer> MODELS = new HashSet<>();

    public static void create(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        MODELS.add(new MaterialBlockRenderer(block, type, iconSet));
    }

    public static void reinitModels() {  /* ported stub */ }

    private final Block block;
    private final MaterialIconType type;
    private final MaterialIconSet iconSet;

    protected MaterialBlockRenderer(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        this.block = block;
        this.type = type;
        this.iconSet = iconSet;
    }
}
