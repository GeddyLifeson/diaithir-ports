package net.diaithir.carts;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** Rail tiers, coupling chains and a station rail. Clean-room: written from public descriptions of the ideas only (faster rails, chained minecarts, a cheaper rail tick). */
@Mod(DiaithirCarts.ID)
public final class DiaithirCarts {
    public static final String ID = "diaithircarts";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    private static BlockBehaviour.Properties rail() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noCollision().strength(0.7F).sound(SoundType.METAL);
    }

    public static final DeferredBlock<TierRail> BRONZE_RAIL = BLOCKS.registerBlock("bronze_rail", p -> new TierRail(p, Tier.BRONZE), DiaithirCarts::rail);
    public static final DeferredBlock<TierPoweredRail> BRONZE_POWERED_RAIL = BLOCKS.registerBlock("bronze_powered_rail", p -> new TierPoweredRail(p, Tier.BRONZE), DiaithirCarts::rail);
    public static final DeferredBlock<TierRail> STEEL_RAIL = BLOCKS.registerBlock("steel_rail", p -> new TierRail(p, Tier.STEEL), DiaithirCarts::rail);
    public static final DeferredBlock<TierPoweredRail> STEEL_POWERED_RAIL = BLOCKS.registerBlock("steel_powered_rail", p -> new TierPoweredRail(p, Tier.STEEL), DiaithirCarts::rail);
    public static final DeferredBlock<TierRail> FAST_RAIL = BLOCKS.registerBlock("fast_rail", p -> new TierRail(p, Tier.FAST), DiaithirCarts::rail);
    public static final DeferredBlock<TierPoweredRail> FAST_POWERED_RAIL = BLOCKS.registerBlock("fast_powered_rail", p -> new TierPoweredRail(p, Tier.FAST), DiaithirCarts::rail);
    public static final DeferredBlock<TierRail> NARROW_RAIL = BLOCKS.registerBlock("narrow_rail", p -> new TierRail(p, Tier.NARROW), DiaithirCarts::rail);
    public static final DeferredBlock<TierPoweredRail> NARROW_POWERED_RAIL = BLOCKS.registerBlock("narrow_powered_rail", p -> new TierPoweredRail(p, Tier.NARROW), DiaithirCarts::rail);
    public static final DeferredBlock<TierRail> DUAL_RAIL = BLOCKS.registerBlock("dual_rail", p -> new TierRail(p, Tier.DUAL), DiaithirCarts::rail);
    public static final DeferredBlock<TierPoweredRail> DUAL_POWERED_RAIL = BLOCKS.registerBlock("dual_powered_rail", p -> new TierPoweredRail(p, Tier.DUAL), DiaithirCarts::rail);
    public static final DeferredBlock<StationRail> STATION_RAIL = BLOCKS.registerBlock("station_rail", StationRail::new, DiaithirCarts::rail);

    public static final DeferredBlock<OneWayRail> ONE_WAY_RAIL = BLOCKS.registerBlock("one_way_rail", OneWayRail::new, DiaithirCarts::rail);
    public static final DeferredBlock<LockingRail> LOCKING_RAIL = BLOCKS.registerBlock("locking_rail", LockingRail::new, DiaithirCarts::rail);
    public static final DeferredBlock<DestinationRail> DESTINATION_RAIL = BLOCKS.registerBlock("destination_rail", DestinationRail::new, DiaithirCarts::rail);
    public static final DeferredBlock<CartLoader> CART_LOADER = BLOCKS.registerBlock("cart_loader", CartLoader::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3F, 6F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> CART_LOADER_ITEM = ITEMS.registerSimpleBlockItem(CART_LOADER);

    public static final DeferredBlock<Reaper> REAPER = BLOCKS.registerBlock("reaper", Reaper::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3F, 6F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> REAPER_ITEM = ITEMS.registerSimpleBlockItem(REAPER);

    public static final DeferredBlock<BlockSignal> BLOCK_SIGNAL = BLOCKS.registerBlock("block_signal", BlockSignal::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(SoundType.METAL).lightLevel(s -> s.getValue(BlockSignal.RED) ? 7 : 3));
    public static final DeferredItem<BlockItem> BLOCK_SIGNAL_ITEM = ITEMS.registerSimpleBlockItem(BLOCK_SIGNAL);

    public static final DeferredBlock<CartReader> CART_READER = BLOCKS.registerBlock("cart_reader", CartReader::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> CART_READER_ITEM = ITEMS.registerSimpleBlockItem(CART_READER);

    public static final List<DeferredBlock<? extends Block>> ALL_RAILS = List.of(BRONZE_RAIL, BRONZE_POWERED_RAIL, STEEL_RAIL, STEEL_POWERED_RAIL, FAST_RAIL,
            FAST_POWERED_RAIL, NARROW_RAIL, NARROW_POWERED_RAIL, DUAL_RAIL, DUAL_POWERED_RAIL, STATION_RAIL, ONE_WAY_RAIL, DESTINATION_RAIL, LOCKING_RAIL);
    public static final List<DeferredItem<BlockItem>> RAIL_ITEMS = ALL_RAILS.stream().map(b -> ITEMS.registerSimpleBlockItem(b)).toList();

    public static final java.util.Map<Roads, DeferredBlock<Block>> ROAD_BLOCKS = new java.util.EnumMap<>(Roads.class);
    static {
        for (Roads r : Roads.ALL) {
            ROAD_BLOCKS.put(r, BLOCKS.registerBlock(r.id, Block::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.0F, 3.0F)
                    .sound(r.ordinal() < 2 ? SoundType.GRAVEL : SoundType.STONE).speedFactor(r.speedFactor)));
        }
    }
    public static final List<DeferredItem<BlockItem>> ROAD_ITEMS = ROAD_BLOCKS.values().stream().map(b -> ITEMS.registerSimpleBlockItem(b)).toList();

    public static final DeferredItem<Item> COUPLING_CHAIN = ITEMS.registerSimpleItem("coupling_chain");
    public static final DeferredItem<Item> CHAIN_CUTTERS = ITEMS.registerItem("chain_cutters", Item::new, p -> p.durability(96));

    public static final DeferredItem<Item> MANA_FIREBOX = ITEMS.registerItem("mana_firebox", Item::new, p -> p.stacksTo(16));
    public static final DeferredItem<Item> MANA_CELL = ITEMS.registerItem("mana_cell", Item::new, p -> p.stacksTo(16));
    public static final DeferredItem<Item> TIMETABLE = ITEMS.registerItem("timetable", TimetableItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<Item> AUTONOMY_MODULE = ITEMS.registerItem("autonomy_module", Item::new, p -> p.stacksTo(16));

    public DiaithirCarts(IEventBus bus) {
        Autonomy.ATTACHMENTS.register(bus);
        Schedule.ATTACHMENTS.register(bus);
        GaugeData.ATTACHMENTS.register(bus);
        MagicFuel.ATTACHMENTS.register(bus);
        Schedule.COMPONENTS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        Coupling.ATTACHMENTS.register(bus);
        net.diaithir.yards.YardContent.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) { RAIL_ITEMS.forEach(i -> e.accept(i.get())); e.accept(BLOCK_SIGNAL_ITEM.get()); e.accept(CART_LOADER_ITEM.get()); e.accept(REAPER_ITEM.get()); }
            if (e.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) { RAIL_ITEMS.forEach(i -> e.accept(i.get())); e.accept(BLOCK_SIGNAL_ITEM.get()); e.accept(CART_LOADER_ITEM.get()); e.accept(CART_READER_ITEM.get()); }
            if (e.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) ROAD_ITEMS.forEach(i -> e.accept(i.get()));
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                e.accept(COUPLING_CHAIN.get());
                e.accept(CHAIN_CUTTERS.get());
                e.accept(TIMETABLE.get());
                e.accept(AUTONOMY_MODULE.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> CartCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Pre.class, e -> {
            if (e.getEntity() instanceof AbstractMinecart cart && cart.level() instanceof ServerLevel level) CartLogic.tick(cart, level);
        });
        NeoForge.EVENT_BUS.addListener(EntityLeaveLevelEvent.class, e -> {
            if (e.getEntity() instanceof AbstractMinecart cart && cart.level() instanceof ServerLevel level && cart.getRemovalReason() != null
                    && cart.getRemovalReason().shouldDestroy()) Coupling.onRemoved(level, cart);
        });
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> { CartSelfTest.tick(e.getServer()); net.diaithir.yards.YardSelfTest.tick(e.getServer()); SignalTest.tick(e.getServer()); RoutingTest.tick(e.getServer()); ScheduleTest.tick(e.getServer()); });
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, DiaithirCarts::interact);
    }

    private static void interact(PlayerInteractEvent.EntityInteract e) {
        if (!(e.getTarget() instanceof AbstractMinecart cart)) return;
        ItemStack held = e.getItemStack();
        if (e.getLevel() instanceof ServerLevel && (held.is(MagicFuel.FUEL) || held.is(MANA_FIREBOX.get()) || held.is(MANA_CELL.get()))) {
            var r = MagicFuel.use(cart, e.getEntity(), e.getHand(), held);
            if (r != null) { e.setCancellationResult(r); e.setCanceled(true); }
            return;
        }
        if (held.is(AUTONOMY_MODULE.get())) {
            e.setCancellationResult(InteractionResult.SUCCESS);
            e.setCanceled(true);
            if (e.getLevel() instanceof ServerLevel) {
                boolean ok = Autonomy.fit(cart);
                if (ok && !e.getEntity().isCreative()) held.shrink(1);
                e.getEntity().sendOverlayMessage(Component.literal(ok ? "Autonomy module fitted" : Autonomy.capable(cart) ? "Already has a module" : "This vehicle cannot be made autonomous"));
            }
            return;
        }
        if (held.is(TIMETABLE.get())) {
            e.setCancellationResult(InteractionResult.SUCCESS);
            e.setCanceled(true);
            if (e.getLevel() instanceof ServerLevel) {
                if (held.has(Schedule.ITEM.get()) && !held.get(Schedule.ITEM.get()).stops().isEmpty()) {
                    Schedule.set(cart, new Schedule(held.get(Schedule.ITEM.get()).stops(), 0));
                    e.getEntity().sendOverlayMessage(Component.literal("Schedule given: " + held.get(Schedule.ITEM.get()).stops().size() + " stops"));
                } else {
                    Schedule.clear(cart);
                    e.getEntity().sendOverlayMessage(Component.literal("Schedule cleared"));
                }
            }
            return;
        }
        boolean chain = held.is(COUPLING_CHAIN.get());
        boolean cutters = held.is(CHAIN_CUTTERS.get()) && e.getEntity().isShiftKeyDown();
        if (!chain && !cutters) return;
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        if (chain) {
            e.getEntity().sendOverlayMessage(Component.literal(Coupling.useChain(level, e.getEntity(), held, cart)));
        } else if (Coupling.cut(level, cart)) {
            held.hurtAndBreak(1, e.getEntity(), e.getHand());
            e.getEntity().sendOverlayMessage(Component.literal("Link cut."));
        } else {
            e.getEntity().sendOverlayMessage(Component.literal("This cart is not coupled."));
        }
    }
}
