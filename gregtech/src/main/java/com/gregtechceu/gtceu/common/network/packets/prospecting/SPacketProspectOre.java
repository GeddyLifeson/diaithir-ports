package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;


import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SPacketProspectOre extends SPacketProspect<GeneratedVeinMetadata> {

    public static final Identifier ID = GTCEu.id("prospect_ore");
    public static final Type<SPacketProspectOre> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketProspectOre> CODEC = StreamCodec
            .ofMember(SPacketProspectOre::encode, SPacketProspectOre::new);

    public SPacketProspectOre(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    public SPacketProspectOre(ResourceKey<Level> key, Collection<GeneratedVeinMetadata> veins) {
        super(key, veins.stream().map(GeneratedVeinMetadata::center).toList(), veins);
    }

    @Override
    public void encodeData(RegistryFriendlyByteBuf buf, GeneratedVeinMetadata data) {
        data.writeToPacket(buf);
    }

    @Override
    public GeneratedVeinMetadata decodeData(RegistryFriendlyByteBuf buf) {
        return GeneratedVeinMetadata.readFromPacket(buf);
    }

    @Override
    public void execute(IPayloadContext context) {  /* ported stub */ }

    @Override
    public @NotNull Type<SPacketProspectOre> type() {
        return TYPE;
    }
}
