package net.id.paradise_lost.util;

import net.id.paradise_lost.platform.Services;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class RenderUtils {

    public static int toHex(Vec3i color) {
        return rgb(color.getX(), color.getY(), color.getZ());
    }

    @Deprecated(forRemoval = true)
    public static int toHex(int r, int g, int b) {
        return rgb(r, g, b);
    }

    @Deprecated(forRemoval = true)
    public static int toHex(int r, int g, int b, int a) {
        return rgb(r, g, b) | (a << 24);
    }

    private static int rgb(int r, int g, int b) {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static Vec3i toRGB(int hex) {
        return new Vec3i((hex & 0xFF0000) >> 16, (hex & 0xFF00) >> 8, (hex & 0xFF));
    }

    public static void transparentRenderLayer(Block block) {
        Services.CLIENT.setRenderLayerTranslucent(block);
    }

    public static void transparentRenderLayer(Fluid fluid) {

    }

    public static void cutoutRenderLayer(Block block) {
        Services.CLIENT.setRenderLayerCutout(block);
    }

    public static void cutoutMippedRenderLayer(Block block) {
        Services.CLIENT.setRenderLayerCutoutMipped(block);
    }
}
