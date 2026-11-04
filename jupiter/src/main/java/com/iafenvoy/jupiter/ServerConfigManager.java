package com.iafenvoy.jupiter;

import com.iafenvoy.jupiter.compat.ExtraConfigManager;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerConfigManager implements ResourceManagerReloadListener {
    private static final Map<Identifier, ServerConfigHolder> CONFIGS = new ConcurrentHashMap<>();

    public static void registerServerConfig(AbstractConfigContainer data, PermissionChecker checker) {
        registerServerConfig(data, checker, false);
    }

    public static void registerServerConfig(AbstractConfigContainer data, PermissionChecker checker, boolean allowManualSync) {
        CONFIGS.put(data.getConfigId(), new ServerConfigHolder(data, checker, allowManualSync));
    }

    @Nullable
    public static AbstractConfigContainer getConfig(Identifier id) {
        ServerConfigHolder holder = CONFIGS.get(id);
        if (holder == null) return null;
        return holder.data;
    }

    public static boolean checkPermission(Identifier id, MinecraftServer server, ServerPlayer player, boolean modify) {
        ServerConfigHolder holder = CONFIGS.get(id);
        if (holder == null) return false;
        return !modify && holder.allowManualSync || holder.checker.check(server, player);
    }

    public static List<AbstractConfigContainer> getServerConfigs() {
        return CONFIGS.values().stream().map(ServerConfigHolder::data).toList();
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        CONFIGS.values().forEach(x -> x.data.load());
        ExtraConfigManager.scanConfigs();
        Jupiter.LOGGER.info("Successfully reload {} server config(s).", CONFIGS.size());
    }

    @FunctionalInterface
    public interface PermissionChecker {
        PermissionChecker ALWAYS_TRUE = (server, player) -> true;
        PermissionChecker ALWAYS_FALSE = (server, player) -> false;
        PermissionChecker IS_DEDICATE_SERVER = (server, player) -> server.isDedicatedServer();
        PermissionChecker IS_LOCAL_GAME = (server, player) -> server.isSingleplayerOwner(player.nameAndId());
        PermissionChecker IS_OPERATOR = (server, player) -> IS_LOCAL_GAME.check(server, player) || player.permissions().hasPermission(new Permission.HasCommandLevel(server.operatorUserPermissions().level()));

        boolean check(MinecraftServer server, ServerPlayer player);
    }

    private record ServerConfigHolder(AbstractConfigContainer data, PermissionChecker checker,
                                      boolean allowManualSync) {
    }
}
