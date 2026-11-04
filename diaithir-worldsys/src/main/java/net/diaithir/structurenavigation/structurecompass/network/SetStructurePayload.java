// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Uses CompassIds for namespace and hand codec.
package net.diaithir.structurenavigation.structurecompass.network;

import net.diaithir.structurenavigation.structurecompass.CompassIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

public record SetStructurePayload(InteractionHand hand, Identifier structureLocation,
                                  boolean isTag) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SetStructurePayload> CODEC = StreamCodec.composite(
            CompassIds.INTERACTION_HAND,
            SetStructurePayload::hand,
            Identifier.STREAM_CODEC,
            SetStructurePayload::structureLocation,
            ByteBufCodecs.BOOL,
            SetStructurePayload::isTag,
            SetStructurePayload::new);

    public static final Type<SetStructurePayload> ID = new Type<>(CompassIds.modLoc("set_structure"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
