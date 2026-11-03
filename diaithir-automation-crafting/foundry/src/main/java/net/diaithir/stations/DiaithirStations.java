package net.diaithir.stations;

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
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Station framework: crafting grid only for simple everyday things; everything else is a part cast or forged at a station, an assembly of a few
 * major parts, or a process. Recipes are data (data/&lt;ns&gt;/diaithir_stations/station_recipe/*.json, see {@link StationRecipe}) tied to a station key from the
 * progression graph. Foundry stations here: hearth bench (Age I), wright bench (III), smeltery (IV), steam forge (V), fabricator (VIII).
 */
@Mod(DiaithirStations.ID)
public final class DiaithirStations {
    public static final String ID = "diaithir_stations";
    public static final Logger LOGGER = LoggerFactory.getLogger("DiaithirStations");

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    /** Block name to graph station key, in Age order. */
    public static final Map<String, String> STATIONS = new LinkedHashMap<>();
    static {
        STATIONS.put("foundry_hearth_bench", "station.hearth_bench");
        STATIONS.put("foundry_wright_bench", "station.wright_bench");
        STATIONS.put("foundry_smeltery", "station.smeltery");
        STATIONS.put("foundry_steam_forge", "station.steam_forge");
        STATIONS.put("foundry_fabricator", "station.fabricator");
    }

    public static final List<DeferredBlock<StationBlock>> BLOCK_LIST = new ArrayList<>();
    public static final List<DeferredItem<BlockItem>> ITEM_LIST = new ArrayList<>();
    static {
        STATIONS.forEach((name, key) -> {
            DeferredBlock<StationBlock> b = BLOCKS.registerBlock(name, p -> new StationBlock(p, key),
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
            BLOCK_LIST.add(b);
            ITEM_LIST.add(ITEMS.registerSimpleBlockItem(b));
        });
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StationBlockEntity>> STATION_BE = BLOCK_ENTITIES.register("station",
            () -> new BlockEntityType<>(StationBlockEntity::new, BLOCK_LIST.stream().map(DeferredBlock::get).toArray(net.minecraft.world.level.block.Block[]::new)));

    public DiaithirStations(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) for (DeferredItem<BlockItem> i : ITEM_LIST) e.accept(i.get());
        });
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, e ->
                e.addListener(StationRecipes.ID, new StationRecipes(e.getRegistryAccess())));
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal(ID)
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = StationsSelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Stations self test PASS" : "Stations self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                }))));
    }
}
