package net.diaithir.entityanimation.skinlayers3d.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;

public final class MeshCache {
    public enum Part { HAT, JACKET, LEFT_SLEEVE, RIGHT_SLEEVE, LEFT_PANTS, RIGHT_PANTS }

    public record Key(Identifier tex, boolean slim) {}

    public static final class Meshes {
        final VoxelMesh[] parts = new VoxelMesh[Part.values().length];
        boolean legacy;
        public VoxelMesh get(Part p) { return parts[p.ordinal()]; }
        public boolean isLegacy() { return legacy; }
    }

    private static final Map<Key, Meshes> CACHE = new LinkedHashMap<>(16, 0.75f, true);
    private static final Meshes NONE = new Meshes();

    static { NONE.legacy = true; }

    private MeshCache() {}

    public static synchronized void clear() {
        CACHE.clear();
    }

    public static synchronized Meshes get(Identifier tex, boolean slim) {
        Key key = new Key(tex, slim);
        Meshes m = CACHE.get(key);
        if (m != null) return m;
        m = build(tex, slim);
        if (m == null) return null;
        CACHE.put(key, m);
        int max = SkinLayersConfig.MAX_CACHED_MESHES.get();
        while (CACHE.size() > max) {
            var it = CACHE.keySet().iterator();
            it.next();
            it.remove();
        }
        return m;
    }

    private static Meshes build(Identifier tex, boolean slim) {
        AbstractTexture t = Minecraft.getInstance().getTextureManager().getTexture(tex);
        NativeImage img = null;
        if (t instanceof DynamicTexture dt) img = dt.getPixels();
        if (img == null) return null;
        if (img.getHeight() < 64) return NONE;
        Meshes m = new Meshes();
        int arm = slim ? 3 : 4;
        m.parts[Part.HAT.ordinal()] = VoxelMesh.build(img, 32, 0, 8, 8, 8);
        m.parts[Part.JACKET.ordinal()] = VoxelMesh.build(img, 16, 32, 8, 12, 4);
        m.parts[Part.RIGHT_SLEEVE.ordinal()] = VoxelMesh.build(img, 40, 32, arm, 12, 4);
        m.parts[Part.LEFT_SLEEVE.ordinal()] = VoxelMesh.build(img, 48, 48, arm, 12, 4);
        m.parts[Part.RIGHT_PANTS.ordinal()] = VoxelMesh.build(img, 0, 32, 4, 12, 4);
        m.parts[Part.LEFT_PANTS.ordinal()] = VoxelMesh.build(img, 0, 48, 4, 12, 4);
        return m;
    }
}
