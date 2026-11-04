package net.diaithir.goety;

import com.google.gson.reflect.TypeToken;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Sprite particles of the original; each is a plain particle type whose frames are in assets/goety/particles. */
public final class GoetyParticles {
    private static final DeferredRegister<ParticleType<?>> TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Goety.MOD_ID);
    public static final Map<String, DeferredHolder<ParticleType<?>, SimpleParticleType>> ALL = new LinkedHashMap<>();

    public static void register(IEventBus bus) {
        List<String> ids = Spec.read("particles.json", new TypeToken<List<String>>() {});
        for (String id : ids) ALL.put(id, TYPES.register(id, () -> new SimpleParticleType(false)));
        TYPES.register(bus);
    }

    private GoetyParticles() {}
}
