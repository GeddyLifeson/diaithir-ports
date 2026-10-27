package net.diaithir.wiretech;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Visible power wires, generators and wire-fed machines. Clean-room: written from the general idea of the genre only, no code or assets of any other mod. */
@Mod(DiaithirWiretech.ID)
public final class DiaithirWiretech {
    public static final String ID = "diaithir_wiretech";

    public DiaithirWiretech(IEventBus bus, ModContainer container) {
        Machines.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            e.registerBlockEntity(Capabilities.Energy.BLOCK, Machines.MACHINE_BE.get(), (be, side) -> be.energyFor(side));
            e.registerBlockEntity(Capabilities.Item.BLOCK, Machines.MACHINE_BE.get(), (be, side) -> be.itemsFor(side));
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, Machines.MACHINE_BE.get(), (be, side) -> be.fluidFor(side));
            e.registerBlockEntity(Capabilities.Energy.BLOCK, Machines.CONNECTOR_BE.get(), (be, side) -> be.energyFor(side));
            e.registerBlockEntity(Capabilities.Energy.BLOCK, Machines.GENERATOR_BE.get(), (be, side) -> be.energyFor(side));
            e.registerBlockEntity(Capabilities.Energy.BLOCK, Machines.TRANSFORMER_BE.get(), (be, side) -> be.energyFor(side));
            e.registerBlockEntity(Capabilities.Energy.BLOCK, Machines.DYNAMO_BE.get(), (be, side) -> be.energyFor(side));
        });
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                Machines.GENERATOR_ITEMS.forEach(i -> e.accept(i.get()));
                Machines.CONNECTOR_ITEMS.values().forEach(i -> e.accept(i.get()));
                Machines.ALL_BLOCK_ITEMS.forEach(i -> e.accept(i.get()));
            }
            if (e.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) Machines.BUILDING_ITEMS.forEach(i -> e.accept(i.get()));
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) Machines.COILS.values().forEach(i -> e.accept(i.get()));
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                e.accept(Machines.DIESEL_CANISTER.get());
                e.accept(Machines.BIODIESEL_CANISTER.get());
            }
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                Machines.AUGMENTS.forEach(i -> e.accept(i.get()));
                Machines.MATERIALS.forEach(i -> e.accept(i.get()));
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> Commands.register(e.getDispatcher()));
    }
}
