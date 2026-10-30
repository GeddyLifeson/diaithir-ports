package com.otectus.immersivetanning.network;

import com.otectus.immersivetanning.recipe.LeatherworkingRecipe;
import com.otectus.immersivetanning.recipe.TanningData;
import com.otectus.immersivetanning.network.packet.CancelSessionPacket;
import com.otectus.immersivetanning.network.packet.CloseSessionPacket;
import com.otectus.immersivetanning.network.packet.FinishFleshingPacket;
import com.otectus.immersivetanning.network.packet.FleshNextPacket;
import com.otectus.immersivetanning.network.packet.FleshStrokePacket;
import com.otectus.immersivetanning.network.packet.OpenBenchPacket;
import com.otectus.immersivetanning.network.packet.OpenFinishPacket;
import com.otectus.immersivetanning.network.packet.OpenFleshingPacket;
import com.otectus.immersivetanning.network.packet.SelectBenchRecipePacket;
import com.otectus.immersivetanning.network.packet.SelectFinishPacket;
import com.otectus.immersivetanning.network.packet.SessionResultPacket;
import com.otectus.immersivetanning.network.packet.StationEffectPacket;
import com.otectus.immersivetanning.network.packet.StationNoticePacket;
import com.otectus.immersivetanning.network.packet.StitchPressPacket;
import com.otectus.immersivetanning.network.packet.StitchResultPacket;
import com.otectus.immersivetanning.network.packet.StitchStartPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

/** Small, purpose-specific packets. Clients report inputs only; the server owns every outcome. */
@EventBusSubscriber(modid = com.otectus.immersivetanning.ImmersiveTanning.MOD_ID)
public final class ModNetwork {
    public static final String VERSION = "3";

    /** The active bench recipes, sent to a player when they join and whenever data reloads. */
    public record RecipeListPacket(List<LeatherworkingRecipe> recipes) implements CustomPacketPayload {
        public static final Type<RecipeListPacket> TYPE = new Type<>(com.otectus.immersivetanning.ImmersiveTanning.id("recipe_list"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RecipeListPacket> STREAM_CODEC =
                LeatherworkingRecipe.STREAM_CODEC.apply(ByteBufCodecs.list()).map(RecipeListPacket::new, RecipeListPacket::recipes);

        @Override
        public Type<RecipeListPacket> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
            TanningData.setFromServer(new ArrayList<>(recipes));
        }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar client = event.registrar(VERSION);
        client.playToClient(RecipeListPacket.TYPE, RecipeListPacket.STREAM_CODEC, RecipeListPacket::handle);
        client.playToClient(CloseSessionPacket.TYPE, CloseSessionPacket.STREAM_CODEC, CloseSessionPacket::handle);
        client.playToClient(OpenBenchPacket.TYPE, OpenBenchPacket.STREAM_CODEC, OpenBenchPacket::handle);
        client.playToClient(OpenFinishPacket.TYPE, OpenFinishPacket.STREAM_CODEC, OpenFinishPacket::handle);
        client.playToClient(OpenFleshingPacket.TYPE, OpenFleshingPacket.STREAM_CODEC, OpenFleshingPacket::handle);
        client.playToClient(SessionResultPacket.TYPE, SessionResultPacket.STREAM_CODEC, SessionResultPacket::handle);
        client.playToClient(StationEffectPacket.TYPE, StationEffectPacket.STREAM_CODEC, StationEffectPacket::handle);
        client.playToClient(StationNoticePacket.TYPE, StationNoticePacket.STREAM_CODEC, StationNoticePacket::handle);
        client.playToClient(StitchResultPacket.TYPE, StitchResultPacket.STREAM_CODEC, StitchResultPacket::handle);
        client.playToClient(StitchStartPacket.TYPE, StitchStartPacket.STREAM_CODEC, StitchStartPacket::handle);
        PayloadRegistrar server = event.registrar(VERSION).executesOn(HandlerThread.NETWORK);
        server.playToServer(CancelSessionPacket.TYPE, CancelSessionPacket.STREAM_CODEC, CancelSessionPacket::handle);
        server.playToServer(FinishFleshingPacket.TYPE, FinishFleshingPacket.STREAM_CODEC, FinishFleshingPacket::handle);
        server.playToServer(FleshNextPacket.TYPE, FleshNextPacket.STREAM_CODEC, FleshNextPacket::handle);
        server.playToServer(FleshStrokePacket.TYPE, FleshStrokePacket.STREAM_CODEC, FleshStrokePacket::handle);
        server.playToServer(SelectBenchRecipePacket.TYPE, SelectBenchRecipePacket.STREAM_CODEC, SelectBenchRecipePacket::handle);
        server.playToServer(SelectFinishPacket.TYPE, SelectFinishPacket.STREAM_CODEC, SelectFinishPacket::handle);
        server.playToServer(StitchPressPacket.TYPE, StitchPressPacket.STREAM_CODEC, StitchPressPacket::handle);
    }

    public static void sendRecipes(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new RecipeListPacket(new ArrayList<>(TanningData.current().recipes())));
    }

    private ModNetwork() {}
}
