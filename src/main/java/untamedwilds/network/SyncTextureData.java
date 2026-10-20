package untamedwilds.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import untamedwilds.UntamedWilds;
import untamedwilds.entity.ComplexMob;
import untamedwilds.util.EntityDataHolderClient;
import untamedwilds.util.EntityUtils;

import java.util.HashMap;

public record SyncTextureData(Identifier entityName, String speciesName, int skinsData, int id) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncTextureData> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "sync_texture"));

    public static final StreamCodec<ByteBuf, SyncTextureData> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC,
        SyncTextureData::entityName,
        ByteBufCodecs.STRING_UTF8,
        SyncTextureData::speciesName,
        ByteBufCodecs.VAR_INT,
        SyncTextureData::skinsData,
        ByteBufCodecs.VAR_INT,
        SyncTextureData::id,
        SyncTextureData::new
    );

    public static void handleClient(SyncTextureData data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (UntamedWilds.DEBUG) {
                UntamedWilds.LOGGER.info("Handling texture data for entity: " + data.entityName + " with species: " + data.speciesName);
            }
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(data.entityName);
            if (!ComplexMob.CLIENT_DATA_HASH.containsKey(type)) {
                ComplexMob.CLIENT_DATA_HASH.put(type, new EntityDataHolderClient(new HashMap<>(), new HashMap<>()));
            }
            EntityUtils.buildSkinArrays(data.entityName.getPath(), data.speciesName, data.skinsData, data.id, ComplexMob.TEXTURES_COMMON, ComplexMob.TEXTURES_RARE);
            ComplexMob.CLIENT_DATA_HASH.get(type).addSpeciesName(data.id, data.speciesName);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}