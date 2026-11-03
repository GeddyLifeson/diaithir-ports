package net.diaithir.hostile.celestials;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Tells clients which moon is up and its tint (0 = none), so the sky and fog can follow. */
@EventBusSubscriber(modid = DiaithirHostile.ID)
public record LunarSync(String event, int rgb) implements CustomPacketPayload {
    public static final Type<LunarSync> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirHostile.ID, "lunar_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LunarSync> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, LunarSync::event, ByteBufCodecs.INT, LunarSync::rgb, LunarSync::new);

    @Override public Type<LunarSync> type() { return TYPE; }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent e) {
        e.registrar("1").playToClient(TYPE, CODEC, LunarSync::handle);
    }

    private void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientMoon.set(event, rgb));
    }

    /** Parses an event's hex colour, or 0 when the event has no tint of its own. */
    public static int tint(Lunar.Event ev) {
        if (ev.id.equals(Lunar.DEFAULT_ID)) return 0;
        try { return Integer.parseInt(ev.color, 16); } catch (NumberFormatException ex) { return 0; }
    }
}
