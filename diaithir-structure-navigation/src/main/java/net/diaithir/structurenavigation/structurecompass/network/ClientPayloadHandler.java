// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Static handler; client screen opened through lazily-resolved client class.
package net.diaithir.structurenavigation.structurecompass.network;

import net.diaithir.structurenavigation.structurecompass.client.CompassClient;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handleOpen(OpenCompassPayload data, IPayloadContext context) {
        context.enqueueWork(() -> CompassClient.openStructureScreen(data.hand(), data.compassStack(), data.structureList(), data.tagList()))
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("structurecompass.networking.open_compass.failed", e.getMessage()));
                    return null;
                });
    }
}
