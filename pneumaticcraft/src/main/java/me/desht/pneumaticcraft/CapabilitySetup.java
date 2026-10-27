package me.desht.pneumaticcraft;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.item.IFluidCapProvider;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.PlayerArmorInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

import java.util.List;
import me.desht.pneumaticcraft.compat.transfer.LegacyAdapters;

public class CapabilitySetup {
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        // entities
        List.of(ModEntityTypes.DRONE,
                ModEntityTypes.COLLECTOR_DRONE,
                ModEntityTypes.GUARD_DRONE,
                ModEntityTypes.LOGISTICS_DRONE,
                ModEntityTypes.HARVESTING_DRONE,
                ModEntityTypes.AMADRONE
        ).forEach(d -> {
            event.registerEntity(PNCCapabilities.AIR_HANDLER_ENTITY, d.get(), (drone, ctx) -> drone.getAirHandler());
            event.registerEntity(Capabilities.Item.ENTITY, d.get(), (drone, ctx) -> LegacyAdapters.item(drone.getDroneItemHandler()));
            event.registerEntity(Capabilities.Fluid.ENTITY, d.get(), (drone, ctx) -> LegacyAdapters.fluid(drone.getFluidTank()));
            event.registerEntity(Capabilities.Energy.ENTITY, d.get(), (drone, ctx) -> LegacyAdapters.energy(drone.getEnergyStorage()));
        });

        event.registerEntity(PNCCapabilities.HEAT_EXCHANGER_ENTITY, ModEntityTypes.HEAT_FRAME.get(), (frame, ctx) -> frame.getHeatExchangerLogic());

        event.registerEntity(PNCCapabilities.ENTITY_AUTOMATION, EntityType.PLAYER, (player, dir) -> {
            if (dir == null) {
                return new PlayerInvWrapper(player.getInventory());
            } else if (dir.getAxis().isVertical()) {
                return new PlayerMainInvWrapper(player.getInventory());
            } else if (dir.getAxis().isHorizontal()) {
                return new PlayerArmorInvWrapper(player.getInventory());
            } else {
                return null;
            }
        });

        // block entities
        ModBlockEntityTypes.streamBlockEntities().forEach(blockEntity -> {
            if (blockEntity instanceof AbstractPneumaticCraftBlockEntity pncBE) {
                if (pncBE.hasItemCapability()) {
                    event.registerBlockEntity(Capabilities.Item.BLOCK, pncBE.getType(),
                            (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be && be.getItemHandler(dir) != null ? LegacyAdapters.item(be.getItemHandler(dir)) : null);
                }
                if (pncBE.hasFluidCapability()) {
                    event.registerBlockEntity(Capabilities.Fluid.BLOCK, pncBE.getType(),
                            (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be && be.getFluidHandler(dir) != null ? LegacyAdapters.fluid(be.getFluidHandler(dir)) : null);
                }
                if (pncBE.hasEnergyCapability()) {
                    event.registerBlockEntity(Capabilities.Energy.BLOCK, pncBE.getType(),
                            (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be && be.getEnergyHandler(dir) != null ? LegacyAdapters.energy(be.getEnergyHandler(dir)) : null);
                }
                if (pncBE instanceof IHeatExchangingTE) {
                    event.registerBlockEntity(PNCCapabilities.HEAT_EXCHANGER_BLOCK, pncBE.getType(),
                            (object, dir) -> object instanceof IHeatExchangingTE heat ? heat.getHeatExchanger(dir) : null);
                }
                if (pncBE instanceof AbstractAirHandlingBlockEntity) {
                    event.registerBlockEntity(PNCCapabilities.AIR_HANDLER_MACHINE, pncBE.getType(),
                            (object, dir) -> object instanceof AbstractAirHandlingBlockEntity a ? a.getAirHandler(dir) : null);
                }
            }
        });

        // items
        ModItems.ITEMS.getEntries().forEach(entry -> {
            if (entry.get() instanceof IPressurizableItem) {
                event.registerItem(PNCCapabilities.AIR_HANDLER_ITEM, (stack, ctx) -> new AirHandlerItemStack(stack), entry.get());
            }
            if (entry.get() instanceof IFluidCapProvider f) {
                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> f.provideFluidCapability(stack), entry.get());
            }
        });
    }
}
