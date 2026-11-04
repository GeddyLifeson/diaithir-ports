package net.diaithir.units.fuel.station;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import net.diaithir.units.fuel.Fuel;
import net.diaithir.units.fuel.Fuels;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Fuel storage and pumping for every vehicle: barrel (Age II) and steel tank (Age VI), hand pump (Age III), pump station (Age VI) and loading rack (Age VII).
 * Targets are anything registered in {@link Targets}; Immersive Vehicles is bridged by reflection. Fuel data and maths: {@link Fuels}.
 */
@Mod(Station.ID)
public final class Station {
    public static final String ID = "diaithirunits";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties wood() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties iron() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 9.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<TankBlock> BARREL = BLOCKS.registerBlock("fuel_barrel", p -> new TankBlock(p, 200), Station::wood);
    public static final DeferredBlock<TankBlock> STEEL_TANK = BLOCKS.registerBlock("fuel_tank", p -> new TankBlock(p, 20_000), Station::iron);
    public static final DeferredBlock<PumpBlock> HAND_PUMP = BLOCKS.registerBlock("hand_pump", p -> new PumpBlock(p, 0, 5), Station::wood);
    public static final DeferredBlock<PumpBlock> PUMP_STATION = BLOCKS.registerBlock("fuel_pump_station", p -> new PumpBlock(p, 1, 0), Station::iron);
    public static final DeferredBlock<PumpBlock> LOADING_RACK = BLOCKS.registerBlock("loading_rack", p -> new PumpBlock(p, 20, 0), Station::iron);

    public static final List<DeferredBlock<? extends Block>> ALL = List.of(BARREL, STEEL_TANK, HAND_PUMP, PUMP_STATION, LOADING_RACK);
    public static final List<DeferredItem<BlockItem>> ITEM_LIST = ALL.stream().map(b -> ITEMS.registerSimpleBlockItem(b)).toList();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TankBlockEntity>> TANK_BE = BLOCK_ENTITIES.register("fuel_tank",
            () -> new BlockEntityType<>(TankBlockEntity::new, BARREL.get(), STEEL_TANK.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PumpBlockEntity>> PUMP_BE = BLOCK_ENTITIES.register("fuel_pump",
            () -> new BlockEntityType<>(PumpBlockEntity::new, HAND_PUMP.get(), PUMP_STATION.get(), LOADING_RACK.get()));

    public Station(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) { IgnitionKey.init(bus); VehicleHud.init(bus); }
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) ITEM_LIST.forEach(i -> e.accept(i.get()));
        });
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.tick.ServerTickEvent.Post.class, e -> {
            if (fireSource != null && (fireLeft > 0 || !shots.isEmpty() || shotWatch > 0)) {
                var bp = fireSource.getEntity() != null ? fireSource.getEntity().position() : fireSource.getPosition();
                var now = IvControl.bullets(fireSource.getLevel(), bp, 150.0);
                for (var en : now.entrySet()) if (shots.put(en.getKey(), en.getValue()) == null) fireSource.sendSuccess(() -> Component.literal("shot fired " + en.getValue()), false);
                for (var it = shots.entrySet().iterator(); it.hasNext(); ) {
                    var en = it.next();
                    if (!now.containsKey(en.getKey())) { String last = en.getValue(); fireSource.sendSuccess(() -> Component.literal("shot ended " + last), false); it.remove(); }
                    else en.setValue(now.get(en.getKey()));
                }
                if (shotWatch > 0) shotWatch--;
            }
            if (fireLeft > 0 && fireSource != null) {
                var pos = fireSource.getEntity() != null ? fireSource.getEntity().position() : fireSource.getPosition();
                IvControl.trigger(fireSource.getLevel(), pos, 8.0, fireLeft-- == fireTotal);
            }
            if (watchLeft > 0 && --watchLeft % 20 == 0 && watchSource != null) {
                var pos = watchSource.getEntity() != null ? watchSource.getEntity().position() : watchSource.getPosition();
                String st = IvControl.status(watchSource.getLevel(), pos, 8.0);
                String line = "t-" + watchLeft / 20 + "s " + (st == null ? "no vehicle" : st);
                watchSource.sendSuccess(() -> Component.literal(line), false);
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal("diaithirvehicle")
                .then(Commands.literal("toggle").executes(c -> ignition(c.getSource(), null)))
                .then(Commands.literal("start").executes(c -> ignition(c.getSource(), true)))
                .then(Commands.literal("stop").executes(c -> ignition(c.getSource(), false)))
                .then(Commands.literal("watch").then(Commands.argument("seconds", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 60)).executes(c -> {
                    watchSource = c.getSource();
                    watchLeft = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "seconds") * 20;
                    c.getSource().sendSuccess(() -> Component.literal("Watching the nearest vehicle once a second; hold W now"), false);
                    return 1;
                })))
                .then(Commands.literal("fire").executes(c -> fire(c.getSource(), 40)).then(Commands.argument("ticks", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 1200)).executes(c -> fire(c.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "ticks")))))
                .then(Commands.literal("status").executes(c -> {
                    String st = IvControl.status(c.getSource().getLevel(), c.getSource().getPosition(), 8.0);
                    c.getSource().sendSuccess(() -> Component.literal(st == null ? "No vehicle within 8 blocks" : "Vehicle: " + st), false);
                    return st == null ? 0 : 1;
                }))
                .then(Commands.literal("drive").then(Commands.argument("throttle", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(0, 1)).executes(c -> {
                    boolean ok = IvControl.drive(c.getSource().getLevel(), c.getSource().getPosition(), 8.0, com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(c, "throttle"));
                    c.getSource().sendSuccess(() -> Component.literal(ok ? "Throttle set, brakes released" : "No vehicle within 8 blocks"), false);
                    return ok ? 1 : 0;
                })))));
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal("diaithirfuel")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.literal("fill").then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("fuel", StringArgumentType.word()).then(Commands.argument("litres", DoubleArgumentType.doubleArg(0)).executes(c -> {
                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(c, "pos");
                            Fuel f = Fuels.get(StringArgumentType.getString(c, "fuel"));
                            if (f == null || f.kind() != Fuel.Kind.LIQUID) { c.getSource().sendFailure(Component.literal("Unknown liquid fuel")); return 0; }
                            if (!(c.getSource().getLevel().getBlockEntity(pos) instanceof TankBlockEntity t)) { c.getSource().sendFailure(Component.literal("No tank there")); return 0; }
                            double put = t.store.insert(f, DoubleArgumentType.getDouble(c, "litres"));
                            t.setChanged();
                            c.getSource().sendSuccess(() -> Component.literal("Added " + put + " L of " + f.name()), false);
                            return 1;
                        })))))
                .then(Commands.literal("selftest").executes(c -> {
                    String err = StationTest.run(c.getSource().getServer());
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Fuel station test PASS" : "Fuel station test FAIL: " + err), false);
                    return err == null ? 1 : 0;
                }))));
    }

    private static net.minecraft.commands.CommandSourceStack watchSource;
    private static int watchLeft;
    private static net.minecraft.commands.CommandSourceStack fireSource;
    private static int fireLeft, fireTotal, shotWatch;
    private static final java.util.Map<Object, String> shots = new java.util.IdentityHashMap<>();

    /** Holds the trigger of the nearest vehicle's guns for the given ticks (same as holding IV's fire key from the controller seat). */
    private static int fire(net.minecraft.commands.CommandSourceStack src, int ticks) {
        var pos = src.getEntity() != null ? src.getEntity().position() : src.getPosition();
        int n = IvControl.trigger(src.getLevel(), pos, 8.0, true);
        if (n <= 0) { src.sendFailure(Component.literal(n < 0 ? "No vehicle within 8 blocks" : "That vehicle has no gun")); return 0; }
        fireSource = src; fireTotal = ticks; fireLeft = ticks - 1; shotWatch = ticks + 100;
        src.sendSuccess(() -> Component.literal("Trigger held on " + n + " gun(s) for " + ticks + " ticks"), false);
        return 1;
    }

    /** Start or stop the engines of the nearest Immersive Vehicles vehicle within 6 blocks of whoever ran the command. */
    private static int ignition(net.minecraft.commands.CommandSourceStack src, Boolean start) {
        int r = IvControl.toggle(src.getLevel(), src.getPosition(), 6.0, start);
        src.sendSuccess(() -> Component.literal(r < 0 ? "No vehicle with an engine within 6 blocks" : r == 1 ? "Engines starting" : "Engines stopped"), false);
        return r < 0 ? 0 : 1;
    }
}
