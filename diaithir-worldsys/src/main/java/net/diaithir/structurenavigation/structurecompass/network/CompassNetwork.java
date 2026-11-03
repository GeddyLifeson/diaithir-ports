// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Merged PacketHandler and server handler; server now also stores the is_tag flag and validates blacklist.
package net.diaithir.structurenavigation.structurecompass.network;

import net.diaithir.structurenavigation.structurecompass.CompassIds;
import net.diaithir.structurenavigation.structurecompass.items.StructureCompassItem;
import net.diaithir.structurenavigation.structurecompass.registry.CompassComponents;
import net.diaithir.structurenavigation.structurecompass.util.StructureUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class CompassNetwork {
    private CompassNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CompassIds.MOD_ID);
        registrar.playToClient(OpenCompassPayload.ID, OpenCompassPayload.CODEC, ClientPayloadHandler::handleOpen);
        registrar.playToServer(SetStructurePayload.ID, SetStructurePayload.CODEC, CompassNetwork::handleSetStructure);
    }

    private static void handleSetStructure(SetStructurePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
                    Player player = context.player();
                    if (player == null) return;
                    ItemStack stack = player.getItemInHand(payload.hand());
                    if (stack.getItem() instanceof StructureCompassItem && !StructureUtil.isBlacklisted(payload.structureLocation())) {
                        stack.set(CompassComponents.STRUCTURE.get(), payload.structureLocation());
                        stack.set(CompassComponents.IS_TAG.get(), payload.isTag());
                        stack.remove(CompassComponents.STRUCTURE_INFO.get());
                    }
                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("structurecompass.networking.set_structure.failed", e.getMessage()));
                    return null;
                });
    }
}
