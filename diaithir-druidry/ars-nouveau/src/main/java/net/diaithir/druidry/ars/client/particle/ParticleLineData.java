package net.diaithir.druidry.ars.client.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/** Port stub: the custom line particle is not ported; a vanilla particle stands in. */
public class ParticleLineData {
    public static ParticleOptions createData(ParticleColor color) {
        return ParticleTypes.END_ROD;
    }

    public static ParticleOptions createData(ParticleColor color, float scale, int age) {
        return ParticleTypes.END_ROD;
    }
}
