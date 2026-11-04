package net.diaithir.druidry.ars.common.event;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketSyncLitEntities;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ArsNouveau.HOST)
public class LightEvents {

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post e) {
        if (e.getEntity().level().isClientSide())
            return;
        if (e.getEntity().level().getGameTime() % 100 == 0 && e.getEntity().level().getServer() != null && e.getEntity() instanceof ServerPlayer serverPlayer) {
            List<Integer> litID = new ArrayList<>();
            for (ServerPlayer player : serverPlayer.level().getServer().getPlayerList().getPlayers()) {
                NonNullList<ItemStack> list = player.getInventory().getNonEquipmentItems();
                for (int i = 0; i < 9; i++) {
                    ItemStack jar = list.get(i);
                    if (jar.getItem() == ItemsRegistry.JAR_OF_LIGHT.asItem()) {
                        litID.add(player.getId());
                        break;
                    }
                }
            }
            Networking.sendToPlayerClient(new PacketSyncLitEntities(litID), serverPlayer);
        }
    }
}
