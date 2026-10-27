package plus.dragons.createdragonsplus.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.common.NeoForge;
import plus.dragons.createdragonsplus.client.CreateDragonsPlusClient;
import plus.dragons.createdragonsplus.data.map.CDPColoringCatalysts;

/** NeoForge networking: payload registration and catalyst sync on configuration and datapack sync. */
public final class CDPNetworking {
    private CDPNetworking() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1").optional()
                .playToClient(ColoringCatalystSyncPayload.TYPE, ColoringCatalystSyncPayload.STREAM_CODEC,
                        (payload, ctx) -> ctx.enqueueWork(() -> CreateDragonsPlusClient.applySnapshot(payload))));
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, event -> {
            event.sendRecipes(plus.dragons.createdragonsplus.registry.CDPRecipes.COLORING.getType(), plus.dragons.createdragonsplus.registry.CDPRecipes.FREEZING.getType(),
                    plus.dragons.createdragonsplus.registry.CDPRecipes.SANDING.getType(), plus.dragons.createdragonsplus.registry.CDPRecipes.ENDING.getType());
            var payload = CDPColoringCatalysts.snapshot().payload();
            if (event.getPlayer() != null) {
                PacketDistributor.sendToPlayer(event.getPlayer(), payload);
            } else {
                PacketDistributor.sendToAllPlayers(payload);
            }
        });
    }
}
