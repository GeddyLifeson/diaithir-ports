package net.diaithir.vehicles;

import net.diaithir.vehicles.fuel.FuelCaps;
import net.diaithir.vehicles.fuel.FuelStore;
import net.diaithir.vehicles.tracks.ChassisItem;
import net.diaithir.vehicles.tracks.TrackedChassis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Shared vehicle layer. First piece: the fuel capability (litres of a named fuel, SI energy per litre), a fuel tank block and a jerry can. Clean-room original, MIT. */
@Mod(DiaithirVehicles.ID)
public final class DiaithirVehicles {
    public static final String ID = "diaithirvehicles";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BE_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ID);

    public static final DeferredBlock<FuelTankBlock> FUEL_TANK = BLOCKS.registerBlock("fuel_tank", FuelTankBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> FUEL_TANK_ITEM = ITEMS.registerSimpleBlockItem(FUEL_TANK);
    public static final DeferredItem<JerryCan> JERRY_CAN = ITEMS.registerItem("jerry_can", JerryCan::new, p -> p.stacksTo(1));
    public static final DeferredItem<ChassisItem> TRACKED_CHASSIS_ITEM = ITEMS.registerItem("tracked_chassis", ChassisItem::new, p -> p.stacksTo(1));
    public static final DeferredHolder<EntityType<?>, EntityType<TrackedChassis>> TRACKED_CHASSIS = ENTITIES.register("tracked_chassis", id -> EntityType.Builder
            .<TrackedChassis>of(TrackedChassis::new, MobCategory.MISC).sized(3.0F, 1.6F).clientTrackingRange(10).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final Supplier<BlockEntityType<FuelTankEntity>> FUEL_TANK_BE = BE_TYPES.register("fuel_tank",
            () -> new BlockEntityType<>(FuelTankEntity::new, FUEL_TANK.get()));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CanContent>> CAN_CONTENT = COMPONENTS.registerComponentType("fuel",
            b -> b.persistent(CanContent.CODEC));

    public DiaithirVehicles(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BE_TYPES.register(bus);
        COMPONENTS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            e.registerBlockEntity(FuelCaps.BLOCK, FUEL_TANK_BE.get(), (be, side) -> be.tank);
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, FUEL_TANK_BE.get(), (be, side) -> be.fluid);
            e.registerEntity(FuelCaps.ENTITY, TRACKED_CHASSIS.get(), (c, ctx) -> c.tank);
            e.registerItem(FuelCaps.ITEM, (stack, ctx) -> new JerryCan.CanStore(stack), JERRY_CAN.get());
        });
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(FUEL_TANK_ITEM.get());
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(JERRY_CAN.get());
            if (e.getTabKey() == CreativeModeTabs.OP_BLOCKS || e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(TRACKED_CHASSIS_ITEM.get());
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> VehicleCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, DiaithirVehicles::refuelVehicle);
    }

    /** Jerry can on any entity that exposes a fuel store (a vehicle, boat or aircraft): pour in; sneak to draw out. */
    private static void refuelVehicle(PlayerInteractEvent.EntityInteract e) {
        if (!e.getItemStack().is(JERRY_CAN.get())) return;
        FuelStore target = FuelLookup.entity(e.getTarget());
        if (target == null) return;
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
        if (e.getLevel().isClientSide()) return;
        FuelStore can = new JerryCan.CanStore(e.getItemStack());
        boolean draw = e.getEntity().isShiftKeyDown();
        long moved = draw ? target.transferTo(can, JerryCan.CAPACITY_L) : can.transferTo(target, JerryCan.CAPACITY_L);
        e.getEntity().sendOverlayMessage(Component.literal(moved > 0 ? (draw ? "Drew " : "Poured ") + moved + " L" : "Nothing moved."));
    }
}
