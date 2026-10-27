package com.mrh0.createaddition.client;

import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.index.CAPartials;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

public class CreateAdditionClient {
    public static void init(IEventBus modBus) {
        CAPartials.init();
        modBus.addListener(CreateAdditionClient::registerRenderers);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> IWireNode.ClientHook.player = Minecraft.getInstance().player);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CABlockEntities.ELECTRIC_MOTOR.get(), ctx -> new ShaftMachineRenderer<>(ShaftMachineRenderer.Kind.MOTOR));
        event.registerBlockEntityRenderer(CABlockEntities.ALTERNATOR.get(), ctx -> new ShaftMachineRenderer<>(ShaftMachineRenderer.Kind.ALTERNATOR));
        event.registerBlockEntityRenderer(CABlockEntities.ROLLING_MILL.get(), ctx -> new ShaftMachineRenderer<>(ShaftMachineRenderer.Kind.ROLLING_MILL));
        event.registerBlockEntityRenderer(CABlockEntities.SMALL_CONNECTOR.get(), ctx -> new WireNodeRenderer<>());
        event.registerBlockEntityRenderer(CABlockEntities.MODULAR_ACCUMULATOR.get(), ctx -> new AccumulatorRenderer());
        event.registerBlockEntityRenderer(CABlockEntities.SMALL_LIGHT_CONNECTOR.get(), ctx -> new WireNodeRenderer<>());
        event.registerBlockEntityRenderer(CABlockEntities.LARGE_CONNECTOR.get(), ctx -> new WireNodeRenderer<>());
        event.registerBlockEntityRenderer(CABlockEntities.REDSTONE_RELAY.get(), ctx -> new WireNodeRenderer<>());
    }
}
