package net.diaithir.forgeworks;

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

/** FE-powered machine family with augments. Clean-room: written from public descriptions of the idea only. */
@Mod(DiaithirForgeworks.ID)
public final class DiaithirForgeworks {
    public static final String ID = "diaithirforgeworks";

    public DiaithirForgeworks(IEventBus bus, ModContainer container) {
        Machines.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            e.registerBlockEntity(Capabilities.Energy.BLOCK, Machines.MACHINE_BE.get(), (be, side) -> be.energyFor(side));
            e.registerBlockEntity(Capabilities.Item.BLOCK, Machines.MACHINE_BE.get(), (be, side) -> be.itemsFor(side));
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, Machines.MACHINE_BE.get(), (be, side) -> be.fluidFor(side));
        });
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) Machines.ALL_BLOCK_ITEMS.forEach(i -> e.accept(i.get()));
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                Machines.AUGMENTS.forEach(i -> e.accept(i.get()));
                Machines.MATERIALS.forEach(i -> e.accept(i.get()));
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> Commands.register(e.getDispatcher()));
    }
}
