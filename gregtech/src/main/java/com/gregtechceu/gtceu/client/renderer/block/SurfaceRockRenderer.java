package com.gregtechceu.gtceu.client.renderer.block;

import net.minecraft.client.data.models.model.DelegatedModel;
import com.gregtechceu.gtceu.GTCEu;


import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;


import net.minecraft.world.entity.animal.equine.Variant;


import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashSet;
import java.util.Set;

public class SurfaceRockRenderer {

    private static final Set<SurfaceRockRenderer> MODELS = new HashSet<>();

    public static void create(Block block) {
        MODELS.add(new SurfaceRockRenderer(block));
    }

    public static void reinitModels() {  /* ported stub */ }

    private final Block block;

    protected SurfaceRockRenderer(Block block) {
        this.block = block;
    }
}
