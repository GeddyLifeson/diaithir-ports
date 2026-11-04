package com.gregtechceu.gtceu.compat;

import net.minecraft.resources.Identifier;

/** Replacement for the removed 1.21 ArmorLayer (armor textures are equipment assets in 26.1). */
public record ArmorLayer(Identifier assetName, String suffix, boolean dyeable) {

    public Identifier texture(boolean innerLayer) {
        return assetName.withPath(p -> "textures/models/armor/" + p + "_layer_" + (innerLayer ? 2 : 1) + suffix + ".png");
    }
}
