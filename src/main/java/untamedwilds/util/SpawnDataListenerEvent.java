package untamedwilds.util;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import untamedwilds.UntamedWilds;
import untamedwilds.world.FaunaHandler;

@EventBusSubscriber(modid = UntamedWilds.MOD_ID)
public class SpawnDataListenerEvent {

    public static final JSONLoader<SpawnDataHolder> SPAWN_DATA_HOLDER = new JSONLoader<>("spawn_tables", SpawnDataHolder.CODEC);

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        // Reset cached spawn entries before datapack data is applied to avoid stale/duplicate entries.
        FaunaHandler.clearAllSpawnLists();
        event.addListener(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "spawn_tables"), SPAWN_DATA_HOLDER);
    }
}
