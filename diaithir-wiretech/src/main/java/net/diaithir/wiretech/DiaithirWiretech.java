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
        bus.addListener(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent.class, e -> {
            var r = e.registrar("1").optional();
            r.playToServer(net.diaithir.wiretech.circuit.CircuitPayloads.Set.TYPE, net.diaithir.wiretech.circuit.CircuitPayloads.Set.CODEC, (pl, ctx) -> {
                if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer sp && sp.containerMenu instanceof net.diaithir.wiretech.circuit.CircuitMenu m && m.pos.equals(pl.pos())
                        && sp.level().getBlockEntity(pl.pos()) instanceof net.diaithir.wiretech.circuit.CircuitNodeBE be) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, be.applyFrom(pl.program()));
                }
            });
            r.playToClient(net.diaithir.wiretech.circuit.CircuitPayloads.View.TYPE, net.diaithir.wiretech.circuit.CircuitPayloads.View.CODEC,
                    (pl, ctx) -> net.diaithir.wiretech.circuit.CircuitClient.onView(pl));
        });
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                Machines.GENERATOR_ITEMS.forEach(i -> e.accept(i.get()));
                Machines.CONNECTOR_ITEMS.values().forEach(i -> e.accept(i.get()));
                Machines.ALL_BLOCK_ITEMS.forEach(i -> e.accept(i.get()));
            }
            if (e.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
                Machines.CIRCUIT_ITEMS.forEach(i -> e.accept(i.get()));
                Machines.CIRCUIT_COILS.values().forEach(i -> e.accept(i.get()));
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
        if (Boolean.getBoolean("wiretech.demo")) {
            NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent.class, e -> {
                if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) Demo.show(sp);
            });
        }
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> {
            Commands.register(e.getDispatcher());
            net.diaithir.wiretech.circuit.CircuitCommands.register(e.getDispatcher());
        });
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.tick.LevelTickEvent.Post.class, e -> {
            if (e.getLevel() instanceof net.minecraft.server.level.ServerLevel sl) net.diaithir.wiretech.circuit.CircuitGrid.tick(sl);
        });
    }
}
