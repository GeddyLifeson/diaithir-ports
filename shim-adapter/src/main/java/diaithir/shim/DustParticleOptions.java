package diaithir.shim;

import org.joml.Vector3f;

/** 1.21.1 constructor (Vector3f color, float scale) over the 26.1 DustParticleOptions(int color, float scale). */
public class DustParticleOptions extends net.minecraft.core.particles.DustParticleOptions {
    public DustParticleOptions(Vector3f color, float scale) {
        super(((int) (color.x * 255f) << 16) | ((int) (color.y * 255f) << 8) | (int) (color.z * 255f), scale);
    }
}
