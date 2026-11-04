package net.diaithir.druidry.ars.client.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/** Port stub: Ars' custom glow particles are not ported; vanilla glints stand in. */
@Deprecated
public class GlowParticleData {

    public static ParticleOptions createData(ParticleColor color) {
        return ParticleTypes.END_ROD;
    }

    public static ParticleOptions createData(ParticleColor color, boolean disableDepthTest) {
        return ParticleTypes.END_ROD;
    }

    public static ParticleOptions createData(ParticleColor color, boolean disableDepthTest, float size, float alpha, int age) {
        return ParticleTypes.END_ROD;
    }

    public static ParticleOptions createData(ParticleColor color, float size, float alpha, int age) {
        return ParticleTypes.END_ROD;
    }
}
