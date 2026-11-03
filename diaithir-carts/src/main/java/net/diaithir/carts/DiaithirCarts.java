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
    public static final DeferredBlock<StationRail> STATION_RAIL = BLOCKS.registerBlock("station_rail", StationRail::new, DiaithirCarts::rail);

    public static final DeferredBlock<OneWayRail> ONE_WAY_RAIL = BLOCKS.registerBlock("one_way_rail", OneWayRail::new, DiaithirCarts::rail);
    public static final DeferredBlock<DestinationRail> DESTINATION_RAIL = BLOCKS.registerBlock("destination_rail", DestinationRail::new, DiaithirCarts::rail);
    public static final DeferredBlock<CartLoader> CART_LOADER = BLOCKS.registerBlock("cart_loader", CartLoader::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3F, 6F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> CART_LOADER_ITEM = ITEMS.registerSimpleBlockItem(CART_LOADER);

    public static final DeferredBlock<BlockSignal> BLOCK_SIGNAL = BLOCKS.registerBlock("block_signal", BlockSignal::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(SoundType.METAL).lightLevel(s -> s.getValue(BlockSignal.RED) ? 7 : 3));
    public static final DeferredItem<BlockItem> BLOCK_SIGNAL_ITEM = ITEMS.registerSimpleBlockItem(BLOCK_SIGNAL);

    public static final List<DeferredBlock<? extends Block>> ALL_RAILS = List.of(BRONZE_RAIL, BRONZE_POWERED_RAIL, STEEL_RAIL, STEEL_POWERED_RAIL, FAST_RAIL,
            FAST_POWERED_RAIL, STATION_RAIL, ONE_WAY_RAIL, DESTINATION_RAIL);
    public static final List<DeferredItem<BlockItem>> RAIL_ITEMS = ALL_RAILS.stream().map(b -> ITEMS.registerSimpleBlockItem(b)).toList();

    public static final DeferredItem<Item> COUPLING_CHAIN = ITEMS.registerSimpleItem("coupling_chain");
    public static final DeferredItem<Item> CHAIN_CUTTERS = ITEMS.registerItem("chain_cutters", Item::new, p -> p.durability(96));

    public DiaithirCarts(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        Coupling.ATTACHMENTS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) { RAIL_ITEMS.forEach(i -> e.accept(i.get())); e.accept(BLOCK_SIGNAL_ITEM.get()); e.accept(CART_LOADER_ITEM.get()); }
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                e.accept(COUPLING_CHAIN.get());
                e.accept(CHAIN_CUTTERS.get());
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
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> { CartSelfTest.tick(e.getServer()); SignalTest.tick(e.getServer()); RoutingTest.tick(e.getServer()); });
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, DiaithirCarts::interact);
    }

    private static void interact(PlayerInteractEvent.EntityInteract e) {
        if (!(e.getTarget() instanceof AbstractMinecart cart)) return;
        ItemStack held = e.getItemStack();
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
