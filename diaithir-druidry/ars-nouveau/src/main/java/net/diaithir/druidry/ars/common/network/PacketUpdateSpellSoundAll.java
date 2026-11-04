package net.diaithir.druidry.ars.common.network;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.registry.SpellCasterRegistry;
import net.diaithir.druidry.ars.api.sound.ConfiguredSpellSound;
import net.diaithir.druidry.ars.api.spell.AbstractCaster;
import net.diaithir.druidry.ars.common.items.SpellBook;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class PacketUpdateSpellSoundAll extends PacketSetSound {

    public static final Type<PacketUpdateSpellSoundAll> TYPE = new Type<>(ArsNouveau.prefix("update_spell_sound_all"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateSpellSoundAll> CODEC = StreamCodec.ofMember(PacketUpdateSpellSoundAll::toBytes, PacketUpdateSpellSoundAll::new);

    public PacketUpdateSpellSoundAll(int castSlot, ConfiguredSpellSound sound, boolean mainHand) {
        super(castSlot, sound, mainHand);
    }

    public PacketUpdateSpellSoundAll(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        ItemStack stack = player.getItemInHand(mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (stack.getItem() instanceof SpellBook) {
            AbstractCaster<?> caster = SpellCasterRegistry.from(stack);
            for (int i = 0; i < caster.getMaxSlots(); i++) {
                caster = caster.setSound(sound, i);
            }
            caster.saveToStack(stack);
            Networking.sendToPlayerClient(new PacketUpdateBookGUI(stack), player);
            Networking.sendToPlayerClient(new PacketOpenSpellBook(mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND), player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
