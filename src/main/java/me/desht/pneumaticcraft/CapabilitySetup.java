package me.desht.pneumaticcraft;

import me.desht.pneumaticcraft.common.util.legacy.CapabilityTools;
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
            event.registerEntity(Capabilities.Item.ENTITY, d.get(), (drone, ctx) -> CapabilityTools.wrapItems(drone.getDroneItemHandler()));
            event.registerEntity(Capabilities.Fluid.ENTITY, d.get(), (drone, ctx) -> CapabilityTools.wrapFluids(drone.getFluidTank()));
            event.registerEntity(Capabilities.Energy.ENTITY, d.get(), (drone, ctx) -> CapabilityTools.wrapEnergy(drone.getEnergyStorage()));
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
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.getEntries().forEach(holder -> {
            var type = holder.get();
            event.registerBlockEntity(Capabilities.Item.BLOCK, type,
                    (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be && be.hasItemCapability() ? CapabilityTools.wrapItems(be.getItemHandler(dir)) : null);
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, type,
                    (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be && be.hasFluidCapability() ? CapabilityTools.wrapFluids(be.getFluidHandler(dir)) : null);
            event.registerBlockEntity(Capabilities.Energy.BLOCK, type,
                    (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be && be.hasEnergyCapability() ? CapabilityTools.wrapEnergy(be.getEnergyHandler(dir)) : null);
            event.registerBlockEntity(PNCCapabilities.HEAT_EXCHANGER_BLOCK, type,
                    (object, dir) -> object instanceof IHeatExchangingTE heat ? heat.getHeatExchanger(dir) : null);
            event.registerBlockEntity(PNCCapabilities.AIR_HANDLER_MACHINE, type,
                    (object, dir) -> object instanceof AbstractAirHandlingBlockEntity a ? a.getAirHandler(dir) : null);
        });

        // items
        ModItems.ITEMS.getEntries().forEach(entry -> {
            if (entry.get() instanceof IPressurizableItem) {
                event.registerItem(PNCCapabilities.AIR_HANDLER_ITEM, (stack, ctx) -> new AirHandlerItemStack(stack), entry.get());
            }
            if (entry.get() instanceof IFluidCapProvider f) {
                event.registerItem(Capabilities.Fluid.ITEM, (stack, ctx) -> CapabilityTools.wrapFluids(f.provideFluidCapability(stack)), entry.get());
            }
        });
    }
}
