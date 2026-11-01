package com.iafenvoy.jupiter.network;

import com.iafenvoy.jupiter.JupiterProxies;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.network.payload.ConfigErrorPayload;
import com.iafenvoy.jupiter.network.payload.ConfigRequestPayload;
import com.iafenvoy.jupiter.network.payload.ConfigSyncPayload;
import com.iafenvoy.jupiter.util.Comment;
import com.iafenvoy.jupiter.util.MinecraftHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ClientConfigNetwork {
    private static final Map<Identifier, Consumer<CompoundTag>> CALLBACKS = new HashMap<>();

    public static void syncConfig(AbstractConfigContainer container) {
        syncConfig(container.getConfigId(), container::deserializeNbt);
    }

    @Comment("will pass null to nbt if not allowed")
    public static void syncConfig(Identifier id, Consumer<CompoundTag> callback) {
        CALLBACKS.put(id, callback);
        JupiterProxies.CLIENT_NETWORKING.sendToServer(new ConfigRequestPayload(id));
    }

    public static void init() {
        JupiterProxies.CLIENT_NETWORKING.registerReceiver(ConfigSyncPayload.TYPE, (_, payload) -> onConfigSync(payload.id(), payload.allow(), payload.compound()));
        JupiterProxies.CLIENT_NETWORKING.registerReceiver(ConfigErrorPayload.TYPE, (_, _) -> onConfigError());
    }

    private static Runnable onConfigSync(Identifier id, boolean allow, CompoundTag data) {
        Consumer<CompoundTag> callback = CALLBACKS.get(id);
        if (callback == null) return null;
        if (allow) return () -> callback.accept(data);
        else return () -> callback.accept(null);
    }

    private static Runnable onConfigError() {
        return () -> MinecraftHelper.getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_ACCESS_FAILURE, Component.translatable("jupiter.toast.upload_config_error_title", new Object[]{}), Component.translatable("jupiter.toast.upload_config_error_content", new Object[]{})));
    }
}
