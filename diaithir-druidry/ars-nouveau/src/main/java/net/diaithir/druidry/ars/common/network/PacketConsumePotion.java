package net.diaithir.druidry.ars.common.network;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.potion.IPotionProvider;
import net.diaithir.druidry.ars.api.registry.PotionProviderRegistry;
import net.diaithir.druidry.ars.common.items.PotionFlask;
import net.diaithir.druidry.ars.common.util.PotionUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;

public class PacketConsumePotion extends AbstractPacket{
    public static final Type<PacketConsumePotion> TYPE = new Type<>(ArsNouveau.prefix("consume_potion"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketConsumePotion> CODEC = StreamCodec.ofMember(PacketConsumePotion::toBytes, PacketConsumePotion::new);

    int inventorySlot;

    public PacketConsumePotion(RegistryFriendlyByteBuf buf) {
        this.inventorySlot = buf.readInt();
    }

    //Encoder
    public void toBytes(RegistryFriendlyByteBuf buf){
        buf.writeInt(inventorySlot);
    }

    public PacketConsumePotion(int inventorySlot) {
        this.inventorySlot = inventorySlot;
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        if (player == null)
            return;

        if(inventorySlot >= player.getInventory().getContainerSize())
            return;
        ItemStack stack = player.getInventory().getItem(inventorySlot);
        if(stack.getItem() instanceof PotionItem){
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if(contents == null){
                return;
            }
            PotionUtil.applyContents(contents, player, player, player);
            stack.shrink(1);
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5f, player.level().getRandom().nextFloat() * 0.1F + 0.9F);
        }else if(stack.getItem() instanceof PotionFlask){
            IPotionProvider data = PotionProviderRegistry.from(stack);
            if(data == null || data.getPotionData(stack) == PotionContents.EMPTY || data.usesRemaining(stack) <= 0)
                return;
            data.applyEffects(stack, player, player, player);
            data.consumeUses(stack, 1, player);
            player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5f, player.level().getRandom().nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
