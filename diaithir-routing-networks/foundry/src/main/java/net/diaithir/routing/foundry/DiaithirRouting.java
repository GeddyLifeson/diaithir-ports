package net.diaithir.routing.foundry;

import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Foundry sorting parts for the routing-networks module: the Day Barrel, the Sieve and the Sorting Router.
 * Ported to NeoForge 26.1.2 from the barrel, sieve and shifter ideas in Factorization (MIT, (c) 2016 neptunepink / purpleposeidon),
 * rewritten for the transfer API. No original code or art is kept. See NOTICE.
 */
@Mod(DiaithirRouting.ID)
public final class DiaithirRouting {
    public static final String ID = "diaithirrouting";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<BarrelBlock> BARREL = BLOCKS.registerBlock("foundry_barrel", BarrelBlock::new, DiaithirRouting::metal);
    public static final DeferredBlock<SieveBlock> SIEVE = BLOCKS.registerBlock("foundry_sieve", SieveBlock::new, DiaithirRouting::metal);
    public static final DeferredBlock<RouterBlock> ROUTER = BLOCKS.registerBlock("foundry_router", RouterBlock::new, DiaithirRouting::metal);

    public static final DeferredItem<BlockItem> BARREL_ITEM = ITEMS.registerSimpleBlockItem(BARREL);
    public static final DeferredItem<BlockItem> SIEVE_ITEM = ITEMS.registerSimpleBlockItem(SIEVE);
    public static final DeferredItem<BlockItem> ROUTER_ITEM = ITEMS.registerSimpleBlockItem(ROUTER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BarrelBlockEntity>> BARREL_BE = BLOCK_ENTITIES.register("foundry_barrel",
            () -> new BlockEntityType<>(BarrelBlockEntity::new, BARREL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SieveBlockEntity>> SIEVE_BE = BLOCK_ENTITIES.register("foundry_sieve",
            () -> new BlockEntityType<>(SieveBlockEntity::new, SIEVE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RouterBlockEntity>> ROUTER_BE = BLOCK_ENTITIES.register("foundry_router",
            () -> new BlockEntityType<>(RouterBlockEntity::new, ROUTER.get()));

    public DiaithirRouting(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            e.registerBlockEntity(Capabilities.Item.BLOCK, BARREL_BE.get(), (be, side) -> be.handler(side));
            e.registerBlockEntity(Capabilities.Item.BLOCK, SIEVE_BE.get(), (be, side) -> be.handler(side));
            e.registerBlockEntity(Capabilities.Item.BLOCK, ROUTER_BE.get(), (be, side) -> side == null ? null : be.handler(side));
        });
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (DeferredItem<BlockItem> i : List.of(BARREL_ITEM, SIEVE_ITEM, ROUTER_ITEM)) e.accept(i.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal("diaithirrouting")
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = RoutingSelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Routing self test PASS" : "Routing self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                }))));
    }
}
