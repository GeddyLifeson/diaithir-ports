package com.iafenvoy.jupiter._loader.neoforge;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.JupiterProxies;
import com.iafenvoy.jupiter.ServerConfigManager;
import com.iafenvoy.jupiter._loader.neoforge.network.NeoForgeClientNetworkHelper;
import com.iafenvoy.jupiter._loader.neoforge.network.NeoForgeServerNetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;

@Mod(Jupiter.MOD_ID)
@EventBusSubscriber
public final class JupiterNeoForge {
    public JupiterNeoForge() {
        JupiterProxies.PLATFORM = new NeoForgePlatform();
        JupiterProxies.CLIENT_NETWORKING = new NeoForgeClientNetworkHelper();
        JupiterProxies.SERVER_NETWORKING = new NeoForgeServerNetworkHelper();

        Jupiter.init(!FMLEnvironment.isProduction());
    }

    @SubscribeEvent
    public static void process(FMLCommonSetupEvent event) {
        Jupiter.process();
    }

    @SubscribeEvent
    public static void registerNetwork(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        for (Map.Entry<CustomPacketPayload.Type<CustomPacketPayload>, StreamCodec<FriendlyByteBuf, CustomPacketPayload>> entry : NeoForgeServerNetworkHelper.TYPES.entrySet())
            registrar.playBidirectional(entry.getKey(), entry.getValue(), NeoForgeServerNetworkHelper::handleData);
    }

    @SubscribeEvent
    public static void registerServerListener(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Jupiter.MOD_ID, "server_config_reload"), new ServerConfigManager());
    }
}
