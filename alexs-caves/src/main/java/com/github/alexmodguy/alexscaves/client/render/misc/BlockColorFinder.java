package com.github.alexmodguy.alexscaves.client.render.misc;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Average colour of a block's particle sprite (biome tints are not applied in 26.1; they live in the model JSON now). */
public class BlockColorFinder {

    public static final Object2IntMap<String> TEXTURES_TO_COLOR = new Object2IntOpenHashMap<>();

    public static int getBlockColor(BlockState blockState, @Nullable Object level, @Nullable BlockPos pos) {
        String key = blockState.toString();
        if (TEXTURES_TO_COLOR.containsKey(key)) return TEXTURES_TO_COLOR.getInt(key);
        int color = 0xFFFFFF;
        try {
            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(blockState).sprite();
            color = average(sprite);
        } catch (RuntimeException e) {
            // keep the neutral colour
        }
        TEXTURES_TO_COLOR.put(key, color);
        return color;
    }

    private static int average(TextureAtlasSprite sprite) {
        NativeImage image = sprite.contents().getOriginalImage();
        int w = sprite.contents().width(), h = Math.min(sprite.contents().height(), image.getHeight());
        float r = 0, g = 0, b = 0, n = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int p = image.getPixel(x, y);
                if (ARGB.alpha(p) == 0) continue;
                r += ARGB.red(p);
                g += ARGB.green(p);
                b += ARGB.blue(p);
                n++;
            }
        }
        if (n == 0) return 0xFFFFFF;
        return ((int) (r / n) << 16) | ((int) (g / n) << 8) | (int) (b / n);
    }
}
