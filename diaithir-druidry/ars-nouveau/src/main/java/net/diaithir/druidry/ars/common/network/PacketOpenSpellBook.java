package net.diaithir.druidry.ars.common.network;

import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;


public class PacketOpenSpellBook extends AbstractPacket{
    boolean isMainHand;

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenSpellBook> CODEC  = StreamCodec.ofMember(PacketOpenSpellBook::toBytes, PacketOpenSpellBook::new);
    public static final Type<PacketOpenSpellBook> TYPE = new Type<>(ArsNouveau.prefix("open_spell_book"));

    public PacketOpenSpellBook(boolean isMainHand) {
        this.isMainHand = isMainHand;
    }

    //Decoder
    public PacketOpenSpellBook(RegistryFriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    //Encoder
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(isMainHand);
    }
    
    public PacketOpenSpellBook(InteractionHand hand) {
        this.isMainHand = hand == InteractionHand.MAIN_HAND;
    }

    @Override
    public void onClientReceived(Minecraft minecraft, Player player) {
        InteractionHand hand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        // Port note: spell book GUI not ported; nothing to open client-side.
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
