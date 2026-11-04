package net.diaithir.druidry.ars.api.registry;

import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

/** Port stub: only the default ParticleColor type exists (no rainbow/custom providers). */
public class ParticleColorRegistry {

    public static ParticleColor from(@Nullable CompoundTag compoundTag) {
        if (compoundTag == null) {
            return new ParticleColor(0, 0, 0);
        }
        return new ParticleColor(compoundTag);
    }

    public static ParticleColor from(Identifier location, int r, int g, int b) {
        return new ParticleColor(r, g, b);
    }
}
