package net.diaithir.druidry.ars.client.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/** Port stub: Ars' custom sparkle particle is not ported; a vanilla glint stands in. */
public class ParticleSparkleData {
    public static ParticleOptions createData(ParticleColor color) {
        return ParticleTypes.END_ROD;
    }

    public static ParticleOptions createData(ParticleColor color, float size, int age) {
        return ParticleTypes.END_ROD;
    }
}
