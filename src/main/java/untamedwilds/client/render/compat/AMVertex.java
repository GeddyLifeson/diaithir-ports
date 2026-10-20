package untamedwilds.client.render.compat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;

/**
 * Whole-vertex emitter used by the vendored Citadel model classes (from Alex's Mobs Continued,
 * GPL-3.0-only).
 */
public class AMVertex {

    public static void addVertex(VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int packedOverlay, int packedLight, float nx, float ny, float nz) {
        consumer.addVertex(x, y, z, ARGB.colorFromFloat(alpha, red, green, blue), u, v, packedOverlay, packedLight, nx, ny, nz);
    }
}
