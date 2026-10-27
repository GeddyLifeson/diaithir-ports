package com.diaithir.addons.newage.client;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.Identifier;

import com.diaithir.addons.newage.CreateNewAge;

public class CNAPartialModels {
    public static final PartialModel COIL = PartialModel.of(
            Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, "block/carbon_brushes/coil"));

    public static final PartialModel GENERATOR_COIL = PartialModel.of(
            Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, "block/generator_coil/block"));

    public static void load() {}
}
