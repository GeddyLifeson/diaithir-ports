package com.github.alexmodguy.alexscaves.server.block.grower;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.Optional;

public class PewenGrower {

    public static final ResourceKey<ConfiguredFeature<?, ?>> PEWEN_TREE = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "pewen_tree"));

    public static final TreeGrower GROWER = new TreeGrower(
            "pewen",
            Optional.empty(),
            Optional.of(PEWEN_TREE),
            Optional.empty()
    );
}
