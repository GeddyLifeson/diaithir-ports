/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.capabilities;

import li.cil.oc2.api.API;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;


public final class Capabilities {
    public static final class DeviceBusElement {
        public static final BlockCapability<li.cil.oc2.api.bus.DeviceBusElement, @Nullable Direction> BLOCK = BlockCapability.createSided(Identifier.fromNamespaceAndPath(API.MOD_ID, "device_bus_element"), li.cil.oc2.api.bus.DeviceBusElement.class);
    }
    public static final class Device {
        public static final BlockCapability<li.cil.oc2.api.bus.device.Device, @Nullable Direction> BLOCK = BlockCapability.createSided(Identifier.fromNamespaceAndPath(API.MOD_ID, "device"), li.cil.oc2.api.bus.device.Device.class);
        public static final ItemCapability<li.cil.oc2.api.bus.device.Device, @Nullable Void> ITEM = ItemCapability.createVoid(Identifier.fromNamespaceAndPath(API.MOD_ID, "device"), li.cil.oc2.api.bus.device.Device.class);
    }
    public static final class RedstoneEmitter {
        public static final BlockCapability<li.cil.oc2.api.capabilities.RedstoneEmitter, @Nullable Direction> BLOCK = BlockCapability.createSided(Identifier.fromNamespaceAndPath(API.MOD_ID, "redstone_emitter"), li.cil.oc2.api.capabilities.RedstoneEmitter.class);
    }
    public static final class NetworkInterface {
        public static final BlockCapability<li.cil.oc2.api.capabilities.NetworkInterface, @Nullable Direction> BLOCK = BlockCapability.createSided(Identifier.fromNamespaceAndPath(API.MOD_ID, "network_interface"), li.cil.oc2.api.capabilities.NetworkInterface.class);
    }
    public static final class TerminalUserProvider {
        public static final BlockCapability<li.cil.oc2.api.capabilities.TerminalUserProvider, @Nullable Void> BLOCK = BlockCapability.createVoid(Identifier.fromNamespaceAndPath(API.MOD_ID, "terminal_user_provider"), li.cil.oc2.api.capabilities.TerminalUserProvider.class);
        public static final EntityCapability<li.cil.oc2.api.capabilities.TerminalUserProvider, @Nullable Void> ENTITY = EntityCapability.createVoid(Identifier.fromNamespaceAndPath(API.MOD_ID, "terminal_user_provider"), li.cil.oc2.api.capabilities.TerminalUserProvider.class);
    }
    public static final class Robot {
        public static final EntityCapability<li.cil.oc2.api.capabilities.Robot, @Nullable Void> ENTITY = EntityCapability.createVoid(Identifier.fromNamespaceAndPath(API.MOD_ID, "robot"), li.cil.oc2.api.capabilities.Robot.class);
    }

    // NeoForge's legacy capability constants (EnergyStorage, ItemHandler, FluidHandler) no longer exist in
    // net.neoforged.neoforge.capabilities.Capabilities, as the transfer API was replaced. The legacy handler types
    // are kept, so they are exposed here as mod-owned capabilities with the same shape as before.
    public static final class EnergyStorage {
        public static final BlockCapability<IEnergyStorage, @Nullable Direction> BLOCK = BlockCapability.createSided(Identifier.fromNamespaceAndPath(API.MOD_ID, "energy_storage"), IEnergyStorage.class);
        public static final EntityCapability<IEnergyStorage, @Nullable Direction> ENTITY = EntityCapability.createSided(Identifier.fromNamespaceAndPath(API.MOD_ID, "energy_storage"), IEnergyStorage.class);
        public static final ItemCapability<IEnergyStorage, @Nullable Void> ITEM = ItemCapability.createVoid(Identifier.fromNamespaceAndPath(API.MOD_ID, "energy_storage"), IEnergyStorage.class);
    }

    public static final class ItemHandler {
        public static final BlockCapability<IItemHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(Identifier.fromNamespaceAndPath(API.MOD_ID, "item_handler"), IItemHandler.class);
        public static final EntityCapability<IItemHandler, @Nullable Void> ENTITY = EntityCapability.createVoid(Identifier.fromNamespaceAndPath(API.MOD_ID, "item_handler"), IItemHandler.class);
        public static final ItemCapability<IItemHandler, @Nullable Void> ITEM = ItemCapability.createVoid(Identifier.fromNamespaceAndPath(API.MOD_ID, "item_handler"), IItemHandler.class);
    }

    public static final class FluidHandler {
        public static final BlockCapability<IFluidHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(Identifier.fromNamespaceAndPath(API.MOD_ID, "fluid_handler"), IFluidHandler.class);
        public static final EntityCapability<IFluidHandler, @Nullable Direction> ENTITY = EntityCapability.createSided(Identifier.fromNamespaceAndPath(API.MOD_ID, "fluid_handler"), IFluidHandler.class);
        public static final ItemCapability<IFluidHandlerItem, @Nullable Void> ITEM = ItemCapability.createVoid(Identifier.fromNamespaceAndPath(API.MOD_ID, "fluid_handler_item"), IFluidHandlerItem.class);
    }
}
