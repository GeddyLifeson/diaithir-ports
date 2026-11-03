package net.diaithir.worldsys.rankperms;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Ranks with permission nodes and formatted player names. */
public final class RankPermsFeature {
    private RankPermsFeature() {
    }

    public static boolean has(ServerPlayer player, String node) {
        Rank r = RankData.get(player.level().getServer().overworld()).rankOf(player.getUUID());
        return r != null && r.allows(node);
    }

    private static Component format(ServerPlayer player, Component name) {
        Rank r = RankData.get(player.level().getServer().overworld()).rankOf(player.getUUID());
        if (r == null) return name;
        ChatFormatting color = ChatFormatting.getByName(r.color());
        MutableComponent prefix = Component.literal("[" + r.prefix() + "] ");
        if (color != null && color.isColor()) prefix.withStyle(color);
        return prefix.append(name);
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> RankCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.NameFormat.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) e.setDisplayname(format(p, e.getDisplayname()));
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.TabListNameFormat.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) e.setDisplayName(format(p, e.getDisplayName() == null ? p.getName() : e.getDisplayName()));
        });
    }
}
