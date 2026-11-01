package com.gregtechceu.gtceu.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * PORT: the 1.21 custom RenderTypes were rebuilt on vanilla ones, since the RenderType construction API changed in
 * 26.1.
 */
@OnlyIn(Dist.CLIENT)
public final class GTRenderTypes {

    private GTRenderTypes() {}

    public static RenderType getLightRing() {
        return RenderTypes.lightning();
    }

    public static RenderType getMonitor() {
        return RenderTypes.lightning();
    }

    public static RenderType guiTexture(Identifier texture) {
        return RenderTypes.text(texture);
    }
}
