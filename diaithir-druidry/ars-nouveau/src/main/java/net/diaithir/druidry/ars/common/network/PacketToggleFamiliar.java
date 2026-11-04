package net.diaithir.druidry.ars.common.network;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.event.FamiliarSummonEvent;
import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.capability.FamiliarData;
import net.diaithir.druidry.ars.common.capability.IPlayerCap;
import net.diaithir.druidry.ars.setup.registry.CapabilityRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Method;
import java.util.UUID;

public class PacketToggleFamiliar extends AbstractPacket{


    public PacketToggleFamiliar() {}

    //Decoder
    public PacketToggleFamiliar(RegistryFriendlyByteBuf buf) {
    }

    //Encoder
    public void toBytes(RegistryFriendlyByteBuf buf) {
    }

    private static void applyOwner(Object familiar, ServerPlayer owner) {
        UUID id = owner.getUUID();
        for (String name : new String[]{"setOwnerID", "setOwnerId", "setOwnerUUID"}) {
            for (Method m : familiar.getClass().getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(UUID.class)) {
                    try {
                        m.invoke(familiar, id);
                        return;
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        for (Method m : familiar.getClass().getMethods()) {
            if (m.getName().equals("setOwner") && m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(ServerPlayer.class)) {
                try {
                    m.invoke(familiar, owner);
                    return;
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer owner) {
        IPlayerCap cap = CapabilityRegistry.getPlayerDataCap(owner);
        if (cap == null)
            return;

        if(PacketDispelFamiliars.dispelForPlayer(owner)){
            return;
        }

        FamiliarData lastSummoned = cap.getLastSummonedFamiliar();
        if(lastSummoned == null)
            return;
        IFamiliar familiarEntity = lastSummoned.getEntity(owner.level());
        applyOwner(familiarEntity, owner);
        familiarEntity.getThisEntity().setPos(owner.getX(), owner.getY(), owner.getZ());

        FamiliarSummonEvent summonEvent = new FamiliarSummonEvent(familiarEntity.getThisEntity(), owner);
        NeoForge.EVENT_BUS.post(summonEvent);

        if (!summonEvent.isCanceled()) {
            owner.level().addFreshEntity(familiarEntity.getThisEntity());
            ParticleUtil.spawnPoof((ServerLevel) owner.level(), familiarEntity.getThisEntity().blockPosition());
        }
    }

    public static final Type<PacketToggleFamiliar> TYPE = new Type<>(ArsNouveau.prefix("toggle_familiar"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketToggleFamiliar> CODEC = StreamCodec.ofMember(PacketToggleFamiliar::toBytes, PacketToggleFamiliar::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
