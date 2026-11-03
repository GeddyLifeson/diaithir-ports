// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import net.diaithir.units.fuel.FuelStore;
import net.diaithir.vehicles.FuelLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

import java.util.List;
import java.util.function.Supplier;

/** Fuel pump, trailer hitch (with a tank trailer) and vehicle health/crash rules, folded from MrCrayfish's Vehicle Mod (Unofficial). GPL-3.0-only. */
@Mod(CrayfishVehicles.ID)
public final class CrayfishVehicles {
    public static final String ID = "diaithirvehiclescrayfish";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BE_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);

    public static final DeferredBlock<GasPumpBlock> GAS_PUMP = BLOCKS.registerBlock("gas_pump", GasPumpBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion());
    public static final DeferredItem<BlockItem> GAS_PUMP_ITEM = ITEMS.registerSimpleBlockItem(GAS_PUMP);
    public static final DeferredItem<HitchItem> TRAILER_HITCH = ITEMS.registerItem("trailer_hitch", HitchItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<TrailerItem> TANK_TRAILER_ITEM = ITEMS.registerItem("tank_trailer", TrailerItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<Hammer> HAMMER = ITEMS.registerItem("repair_hammer", Hammer::new, p -> p.durability(250));
    public static final Supplier<BlockEntityType<GasPumpEntity>> GAS_PUMP_BE = BE_TYPES.register("gas_pump", () -> new BlockEntityType<>(GasPumpEntity::new, GAS_PUMP.get()));
    public static final DeferredHolder<EntityType<?>, EntityType<TankTrailer>> TANK_TRAILER = ENTITIES.register("tank_trailer", id -> EntityType.Builder
            .<TankTrailer>of(TankTrailer::new, MobCategory.MISC).sized(1.6F, 1.1F).clientTrackingRange(10).build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public CrayfishVehicles(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BE_TYPES.register(bus);
        ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            e.registerEntity(CrayfishCaps.HEALTH, TANK_TRAILER.get(), (t, ctx) -> t.health);
        });
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(GAS_PUMP_ITEM.get());
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                e.accept(TRAILER_HITCH.get());
                e.accept(HAMMER.get());
                e.accept(TANK_TRAILER_ITEM.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> CrayfishCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, CrayfishVehicles::interact);
    }

    private static void interact(PlayerInteractEvent.EntityInteract e) {
        Player p = e.getEntity();
        Entity target = e.getTarget();
        var stack = e.getItemStack();
        if (stack.is(HAMMER.get())) {
            if (!e.getLevel().isClientSide() && Hammer.strike(p, e.getHand(), stack, target)) done(e);
            else if (target.getCapability(CrayfishCaps.HEALTH, null) != null) done(e);
            return;
        }
        if (stack.is(TRAILER_HITCH.get())) {
            if (e.getLevel().isClientSide()) {
                if (target instanceof TankTrailer || target.getCapability(CrayfishCaps.TOW, null) != null) done(e);
                return;
            }
            if (target instanceof TankTrailer t) {
                if (t.pulling() == p) {
                    t.setPulling(null);
                    p.sendOverlayMessage(Component.literal("Trailer unhooked."));
                } else {
                    t.setPulling(p);
                    p.sendOverlayMessage(Component.literal("Trailer hooked on."));
                }
                done(e);
            } else if (target.getCapability(CrayfishCaps.TOW, null) != null) {
                for (TankTrailer t : e.getLevel().getEntitiesOfClass(TankTrailer.class, p.getBoundingBox().inflate(16))) {
                    if (t.pulling() == p) {
                        t.setPulling(target);
                        p.sendOverlayMessage(Component.literal("Trailer hooked to the vehicle."));
                        break;
                    }
                }
                done(e);
            }
            return;
        }
        // Nozzle: with the hose in hand (no item needed), use any entity that has a fuel store.
        if (stack.isEmpty() && !e.getLevel().isClientSide()) {
            FuelStore store = FuelLookup.entity(target);
            if (store == null) return;
            for (GasPumpEntity pump : nearbyPumps(e)) {
                if (p.getUUID().equals(pump.fueler())) {
                    double moved = pump.refuel(store);
                    p.sendOverlayMessage(Component.literal(moved > 0 ? "Pumped " + Math.round(moved * 10) / 10.0 + " L" : "Nothing pumped."));
                    done(e);
                    return;
                }
            }
        }
    }

    private static List<GasPumpEntity> nearbyPumps(PlayerInteractEvent.EntityInteract e) {
        int r = (int) Math.ceil(Config.MAX_HOSE_DISTANCE.get());
        var base = e.getEntity().blockPosition();
        List<GasPumpEntity> out = new java.util.ArrayList<>();
        for (var pos : net.minecraft.core.BlockPos.betweenClosed(base.offset(-r, -r, -r), base.offset(r, r, r)))
            if (e.getLevel().getBlockState(pos).is(GAS_PUMP.get()) && e.getLevel().getBlockEntity(pos) instanceof GasPumpEntity g) out.add(g);
        return out;
    }

    private static void done(PlayerInteractEvent.EntityInteract e) {
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
    }
}
