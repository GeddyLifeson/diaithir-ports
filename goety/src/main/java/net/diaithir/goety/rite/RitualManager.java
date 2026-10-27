package net.diaithir.goety.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.diaithir.goety.Goety;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/** Loads the rites from data/goety/rituals. */
public final class RitualManager extends SimpleJsonResourceReloadListener<Ritual> {
    private static volatile List<Ritual> RITUALS = List.of();

    public RitualManager() {
        super(Ritual.CODEC, FileToIdConverter.json("rituals"));
    }

    public static List<Ritual> all() {
        return RITUALS;
    }

    @Override
    protected void apply(Map<Identifier, Ritual> map, ResourceManager manager, ProfilerFiller profiler) {
        RITUALS = new ArrayList<>(map.values());
    }

    public static List<Ritual> forActivation(ItemStack s) {
        List<Ritual> out = new ArrayList<>();
        for (Ritual r : RITUALS) if (r.activatedBy(s)) out.add(r);
        return out;
    }

    @SubscribeEvent
    public static void onReload(AddServerReloadListenersEvent e) {
        e.addListener(Identifier.fromNamespaceAndPath(Goety.MOD_ID, "rituals"), new RitualManager());
    }
}
