package net.diaithir.druidry.ars.common.network;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.event.FamiliarSummonEvent;
import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.capability.IPlayerCap;
import net.diaithir.druidry.ars.setup.registry.CapabilityRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Method;
import java.util.UUID;

public class PacketSummonFamiliar extends AbstractPacket{

    public static final Type<PacketSummonFamiliar> TYPE = new Type<>(ArsNouveau.prefix("summon_familiar"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSummonFamiliar> CODEC = StreamCodec.ofMember(PacketSummonFamiliar::toBytes, PacketSummonFamiliar::new);


    Identifier familiarID;

    public PacketSummonFamiliar(Identifier id) {
        this.familiarID = id;
    }

    //Decoder
    public PacketSummonFamiliar(RegistryFriendlyByteBuf buf) {
        familiarID = buf.readIdentifier();
    }

    //Encoder
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(familiarID);
    }

    private static void applyOwnerId(Object target, UUID uuid) {
        String[] names = {"setOwnerID", "setOwnerUUID", "setOwnerId"};
        for (String name : names) {
            try {
                Method m = target.getClass().getMethod(name, UUID.class);
                m.invoke(target, uuid);
                return;
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer owner) {
        IPlayerCap cap = CapabilityRegistry.getPlayerDataCap(owner);
        if (cap == null)
            return;

        if(owner == null)
            return;

        IFamiliar familiarEntity = cap.getFamiliarData(familiarID).getEntity(owner.level());
        applyOwnerId(familiarEntity, owner.getUUID());
        familiarEntity.getThisEntity().setPos(owner.getX(), owner.getY(), owner.getZ());

        FamiliarSummonEvent summonEvent = new FamiliarSummonEvent(familiarEntity.getThisEntity(), owner);
        NeoForge.EVENT_BUS.post(summonEvent);

        if (!summonEvent.isCanceled()) {
            owner.level().addFreshEntity(familiarEntity.getThisEntity());
            ParticleUtil.spawnPoof((ServerLevel) owner.level(), familiarEntity.getThisEntity().blockPosition());
            cap.setLastSummonedFamiliar(familiarID);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
