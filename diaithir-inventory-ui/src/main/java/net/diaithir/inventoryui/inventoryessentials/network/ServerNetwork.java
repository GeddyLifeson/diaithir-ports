package net.diaithir.inventoryui.inventoryessentials.network;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.diaithir.inventoryui.inventoryessentials.config.ServerConfig;
import net.diaithir.inventoryui.inventoryessentials.sort.ContainerSorter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerNetwork {
    private static final Map<UUID, long[]> STATE = new ConcurrentHashMap<>();

    private ServerNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToServer(SortRequest.TYPE, SortRequest.CODEC, ServerNetwork::handle);
    }

    private static void handle(SortRequest msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!ServerConfig.ALLOW_SORT.get()) return;
            var menu = player.containerMenu;
            if (menu.containerId != msg.containerId() || !menu.stillValid(player)) return;
            if (!rateOk(player)) return;
            try {
                var type = BuiltInRegistries.MENU.getKey(menu.getType());
                if (type != null && ServerConfig.BLACKLIST_MENUS.get().contains(type.toString())) return;
            } catch (UnsupportedOperationException ignored) {
            }
            IntOpenHashSet locked = new IntOpenHashSet();
            for (int s : msg.lockedSlots()) locked.add(s);
            ContainerSorter.sort(menu, player, locked);
        });
    }

    private static boolean rateOk(ServerPlayer player) {
        long tick = player.level().getGameTime();
        long[] st = STATE.computeIfAbsent(player.getUUID(), k -> new long[]{Long.MIN_VALUE / 2, 0, 0});
        if (tick - st[0] < ServerConfig.SORT_COOLDOWN_TICKS.get()) return false;
        if (tick - st[1] >= 20) {
            st[1] = tick;
            st[2] = 0;
        }
        if (st[2] >= ServerConfig.MAX_SORTS_PER_SECOND.get()) return false;
        st[2]++;
        st[0] = tick;
        return true;
    }
}
