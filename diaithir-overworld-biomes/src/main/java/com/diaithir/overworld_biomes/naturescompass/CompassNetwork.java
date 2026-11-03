package com.diaithir.overworld_biomes.naturescompass;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public final class CompassNetwork {
    private static CustomPacketPayload.Type<?> t(String p) {
        return null;
    }

    public record OpenList(List<String> ids, boolean mainHand) implements CustomPacketPayload {
        public static final Type<OpenList> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BiomeCompassMod.MOD_ID, "open_list"));
        public static final StreamCodec<ByteBuf, OpenList> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), OpenList::ids,
                ByteBufCodecs.BOOL, OpenList::mainHand, OpenList::new);
        @Override public Type<OpenList> type() { return TYPE; }
    }

    public record SelectBiome(Identifier biome, boolean mainHand) implements CustomPacketPayload {
        public static final Type<SelectBiome> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BiomeCompassMod.MOD_ID, "select_biome"));
        public static final StreamCodec<ByteBuf, SelectBiome> CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, SelectBiome::biome,
                ByteBufCodecs.BOOL, SelectBiome::mainHand, SelectBiome::new);
        @Override public Type<SelectBiome> type() { return TYPE; }
    }

    /** status: 0 searching, 1 found, 2 not found, 3 not locatable */
    public record SearchResult(int status, int distance) implements CustomPacketPayload {
        public static final Type<SearchResult> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BiomeCompassMod.MOD_ID, "search_result"));
        public static final StreamCodec<ByteBuf, SearchResult> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SearchResult::status,
                ByteBufCodecs.VAR_INT, SearchResult::distance, SearchResult::new);
        @Override public Type<SearchResult> type() { return TYPE; }
    }

    public record SearchProgress(int radius) implements CustomPacketPayload {
        public static final Type<SearchProgress> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BiomeCompassMod.MOD_ID, "search_progress"));
        public static final StreamCodec<ByteBuf, SearchProgress> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SearchProgress::radius, SearchProgress::new);
        @Override public Type<SearchProgress> type() { return TYPE; }
    }

    static void register(RegisterPayloadHandlersEvent e) {
        var r = e.registrar("1");
        r.playToClient(OpenList.TYPE, OpenList.CODEC, (p, c) -> ClientHooks.openList(p.ids(), p.mainHand()));
        r.playToServer(SelectBiome.TYPE, SelectBiome.CODEC, CompassNetwork::onSelect);
        r.playToClient(SearchResult.TYPE, SearchResult.CODEC, (p, c) -> ClientState.onResult(p));
        r.playToClient(SearchProgress.TYPE, SearchProgress.CODEC, (p, c) -> ClientState.onProgress(p));
    }

    private static void onSelect(SelectBiome p, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer sp) CompassSearch.start(sp, p.biome(), p.mainHand());
    }

    private CompassNetwork() {}
}
