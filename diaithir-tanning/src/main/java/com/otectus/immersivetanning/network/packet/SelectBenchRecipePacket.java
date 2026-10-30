package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.leather.Finish;
import com.otectus.immersivetanning.minigame.SessionManager;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;


/** The piece and leather group the player chose. The server re-checks it against what it offered. */
public record SelectBenchRecipePacket(int sessionId, Identifier recipe, Finish finish, @Nullable Identifier trait) implements CustomPacketPayload {
    public static final Type<SelectBenchRecipePacket> TYPE = new Type<>(ImmersiveTanning.id("select_bench_recipe"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectBenchRecipePacket> STREAM_CODEC = StreamCodec.ofMember(SelectBenchRecipePacket::encode, SelectBenchRecipePacket::decode);

    @Override
    public Type<SelectBenchRecipePacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
        buf.writeIdentifier(recipe);
        buf.writeEnum(finish);
        buf.writeBoolean(trait != null);
        if (trait != null) buf.writeIdentifier(trait);
    }

    public static SelectBenchRecipePacket decode(RegistryFriendlyByteBuf buf) {
        return new SelectBenchRecipePacket(buf.readVarInt(), buf.readIdentifier(), buf.readEnum(Finish.class),
                buf.readBoolean() ? buf.readIdentifier() : null);
    }

    public void handle(IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        ctx.enqueueWork(() -> SessionManager.selectBenchRecipe(player, sessionId, recipe, finish, trait));
    }
}
