package net.diaithir.goety;

import com.google.gson.reflect.TypeToken;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Sound events taken from the original (creatures, casting, altar); the list is written by tools/audiofx.py. */
public final class GoetySounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Goety.MOD_ID);
    private static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> ALL = new LinkedHashMap<>();

    public static void register(IEventBus bus) {
        List<String> ids = Spec.read("sounds.json", new TypeToken<List<String>>() {});
        for (String id : ids)
            ALL.put(id, SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Goety.MOD_ID, id))));
        SOUNDS.register(bus);
    }

    /** The sound with this id, or the fallback when the port lacks it. */
    public static SoundEvent get(String id, SoundEvent fallback) {
        var h = ALL.get(id);
        return h == null ? fallback : h.value();
    }

    public static int count() { return ALL.size(); }

    private GoetySounds() {}
}
