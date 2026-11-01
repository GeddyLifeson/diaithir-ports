package com.iafenvoy.jupiter._loader.neoforge;

import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter._loader.neoforge.network.NeoForgeClientNetworkHelper;
import com.iafenvoy.jupiter._loader.neoforge.network.NeoForgeServerNetworkHelper;
import com.iafenvoy.jupiter.compat.ExtraConfigManager;
import com.iafenvoy.jupiter.internal.ConfigButtonReplaceStrategy;
import com.iafenvoy.jupiter.internal.JupiterSettings;
import com.iafenvoy.jupiter.render.internal.JupiterConfigListScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(Dist.CLIENT)
public final class JupiterNeoForgeClient {
    @SubscribeEvent
    public static void processClient(FMLClientSetupEvent event) {
        Jupiter.processClient();
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (minecraft, parent) -> new JupiterConfigListScreen(parent));
        ExtraConfigManager.registerScanCallback(JupiterNeoForgeClient::fillExtensionPoints);
    }

    public static void fillExtensionPoints() {
        ConfigButtonReplaceStrategy strategy = JupiterSettings.INSTANCE.general.configButtonReplacement.getValue();
        if (strategy == ConfigButtonReplaceStrategy.NEVER) return;
        for (String id : ExtraConfigManager.getProvidedMods()) {
            Optional<? extends ModContainer> optional = ModList.get().getModContainerById(id);
            if (optional.isEmpty()) continue;
            ModContainer container = optional.get();
            if (strategy == ConfigButtonReplaceStrategy.UNAVAILABLE_ONLY && container.getCustomExtension(IConfigScreenFactory.class).isPresent())
                continue;
            container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> ExtraConfigManager.getScreen(id).apply(parent));
        }
    }

    @SubscribeEvent
    public static void registerClientListener(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Jupiter.MOD_ID, "client_config_reload"), ConfigManager.getInstance());
    }

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        for (Map.Entry<CustomPacketPayload.Type<CustomPacketPayload>, StreamCodec<FriendlyByteBuf, CustomPacketPayload>> entry : NeoForgeServerNetworkHelper.TYPES.entrySet())
            event.register(entry.getKey(), NeoForgeClientNetworkHelper::handleData);
    }
}