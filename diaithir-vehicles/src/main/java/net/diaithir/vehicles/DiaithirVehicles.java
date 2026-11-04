package net.diaithir.vehicles;

import net.diaithir.units.fuel.FuelStore;
import net.diaithir.vehicles.tracks.ChassisItem;
import net.diaithir.vehicles.tracks.TrackedChassis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Shared vehicle layer on the pack's fuel carriers: the jerry can, and the {@link FuelHolder} hook that vehicle entities implement so pumps and cans can fuel them.
 * Fuels, engines and the store interface are diaithir-units; the tank block and the block capability are the Carrier Tank in diaithir-bridges. Clean-room original, MIT.
 */
@Mod(DiaithirVehicles.ID)
public final class DiaithirVehicles {
    public static final String ID = "diaithirvehicles";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ID);

    public static final DeferredItem<JerryCan> JERRY_CAN = ITEMS.registerItem("jerry_can", JerryCan::new, p -> p.stacksTo(1));
    public static final DeferredItem<ChassisItem> TRACKED_CHASSIS_ITEM = ITEMS.registerItem("tracked_chassis", ChassisItem::new, p -> p.stacksTo(1));
    public static final DeferredHolder<EntityType<?>, EntityType<TrackedChassis>> TRACKED_CHASSIS = ENTITIES.register("tracked_chassis", id -> EntityType.Builder
            .<TrackedChassis>of(TrackedChassis::new, MobCategory.MISC).sized(3.0F, 1.6F).clientTrackingRange(10).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CanContent>> CAN_CONTENT = COMPONENTS.registerComponentType("fuel",
            b -> b.persistent(CanContent.CODEC));

    public DiaithirVehicles(IEventBus bus) {
        ITEMS.register(bus);
        COMPONENTS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                e.accept(JERRY_CAN.get());
                e.accept(TRACKED_CHASSIS_ITEM.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> VehicleCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, DiaithirVehicles::refuelVehicle);
    }

    /** Jerry can on any entity that is a {@link FuelHolder}: pour in; sneak to draw out. */
    private static void refuelVehicle(PlayerInteractEvent.EntityInteract e) {
        if (!e.getItemStack().is(JERRY_CAN.get())) return;
        FuelStore target = FuelLookup.entity(e.getTarget());
        if (target == null) return;
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
        if (e.getLevel().isClientSide()) return;
        FuelStore can = new JerryCan.CanStore(e.getItemStack());
        boolean draw = e.getEntity().isShiftKeyDown();
        double moved = draw ? FuelStore.transfer(target, can, JerryCan.CAPACITY_L) : FuelStore.transfer(can, target, JerryCan.CAPACITY_L);
        e.getEntity().sendOverlayMessage(Component.literal(moved > 0 ? (draw ? "Drew " : "Poured ") + Math.round(moved * 10) / 10.0 + " L" : "Nothing moved."));
    }
}
