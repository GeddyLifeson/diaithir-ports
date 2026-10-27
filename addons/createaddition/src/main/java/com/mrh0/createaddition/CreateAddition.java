package com.mrh0.createaddition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mrh0.createaddition.config.CommonConfig;
import com.mrh0.createaddition.index.*;
import com.mrh0.createaddition.network.ClientPayloadHandler;
import com.mrh0.createaddition.network.EnergyNetworkPacketPayload;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.network.ServerPayloadHandler;
import com.mrh0.createaddition.network.TimeRemainingPacketPayload;
import com.mrh0.createaddition.reg.R;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod("diaithir_createaddition")
public class CreateAddition {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "createaddition";

    public CreateAddition(IEventBus eventBus, ModContainer container) {
        eventBus.addListener(RegisterEvent.class, e -> {
            if (!e.getRegistryKey().equals(Registries.BLOCK)) return;
            CABlocks.register();
            CAItems.register();
            CABlockEntities.register();
            CARecipes.register();
            CASounds.register();
            CADataComponents.register();
            CAEffects.register();
            registerTab();
            LOGGER.info("Create Crafts & Additions (Diaithir port) registered");
        });
        eventBus.addListener(RegisterCapabilitiesEvent.class, CACapabilities::register);
        eventBus.addListener(RegisterPayloadHandlersEvent.class, CreateAddition::registerPackets);
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.COMMON_CONFIG);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
            com.mrh0.createaddition.client.CreateAdditionClient.init(eventBus);
        }
    }

    private static void registerTab() {
        ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, asResource("main"));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, CreativeModeTab.builder(null, -1)
                .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                .icon(() -> new ItemStack(CABlocks.ELECTRIC_MOTOR.get()))
                .title(Component.translatable("itemGroup.createaddition.main"))
                .displayItems((p, out) -> R.TAB.forEach(i -> {
                    if (i == CAItems.CAKE_BASE.get() || i == CAItems.CAKE_BASE_BAKED.get() || i == CAItems.BIOMASS.get())
                        out.accept(i, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                    else out.accept(i);
                })).build());
    }

    private static final String PROTOCOL = "1";

    public static void registerPackets(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL).executesOn(HandlerThread.MAIN);
        registrar.playBidirectional(ObservePacketPayload.TYPE, ObservePacketPayload.STREAM_CODEC,
                ServerPayloadHandler::handleObservePayload, ClientPayloadHandler::handleObservePayload);
        registrar.playBidirectional(EnergyNetworkPacketPayload.TYPE, EnergyNetworkPacketPayload.STREAM_CODEC,
                ServerPayloadHandler::handleEnergyNetworkPayload, ClientPayloadHandler::handleEnergyNetworkPayload);
        registrar.playBidirectional(TimeRemainingPacketPayload.TYPE, TimeRemainingPacketPayload.STREAM_CODEC,
                ServerPayloadHandler::handleTimeRemainingPayload, ClientPayloadHandler::handleTimeRemainingPayload);
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
