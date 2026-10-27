package net.diaithir.butchery;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registry of every butchery item, block and data component. Ids match tools/gen_butchery.py, which writes assets and recipes. */
public final class Butchery {
    public static final String MOD = "diaithirbutchery";
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD);
    static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD);

    /** Entity id of the animal a carcass or cut came from. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SPECIES = COMPONENTS.registerComponentType("species",
            b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    /** Body class id (see butchery/*.json) a carcass is cut by. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> BODY = COMPONENTS.registerComponentType("body",
            b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    /** Game time of the kill (spoilage clocks start here), and the work done on the carcass so far (0 whole, 1 skinned, 2 gutted). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> KILLED = COMPONENTS.registerComponentType("killed",
            b -> b.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    /** Shelf life as a percent of normal (100 = normal). Spoilage reads it; stations set it. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SHELF = COMPONENTS.registerComponentType("shelf",
            b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Raw cuts: id to nutrition. Each has a cooked twin "cooked_<id>". */
    static final Map<String, Integer> CUTS = new LinkedHashMap<>();
    static final List<String> OFFAL = List.of("liver", "heart", "kidney", "tripe", "tongue", "brain");
    static final List<String> MATERIALS = List.of("raw_hide", "sinew", "tallow", "blood_flask", "marrow_bone", "scraps");
    static final List<String> DRESSED = List.of("fish_fillet", "bird_breast", "drumstick", "wing", "haunch", "shoulder", "loin", "ribs", "flank", "tenderloin", "neck", "tail", "hock");

    public static final List<DeferredItem<Item>> ALL_ITEMS = new ArrayList<>();
    /** Anything that can work a hung carcass: our knife and cleaver, Farmer's Delight knives and any pack knife added to the tag. */
    public static final net.minecraft.tags.TagKey<Item> TOOLS = net.minecraft.tags.TagKey.create(Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD, "butcher_tools"));
    public static final DeferredItem<Item> CARCASS = ITEMS.registerItem("carcass", Item::new, () -> new Item.Properties().stacksTo(1));
    /** Knives by Age: id, durability. The yield bonus of each is data (butchery.json "tools"). The iron butcher_knife is the Age III iron tier. */
    static final Map<String, Integer> KNIVES = new LinkedHashMap<>();
    public static final DeferredItem<Item> KNIFE = ITEMS.registerItem("butcher_knife", Item::new, () -> new Item.Properties().durability(250));
    public static final DeferredItem<Item> CLEAVER = ITEMS.registerItem("butcher_cleaver", Item::new, () -> new Item.Properties().durability(400));
    /** Stations by id and Age; what each makes is data. */
    static final Map<String, Integer> STATIONS = new LinkedHashMap<>();
    static final List<DeferredBlock<StationBlock>> STATION_BLOCKS = new ArrayList<>();
    static final List<DeferredItem<?>> STATION_ITEMS = new ArrayList<>();
    public static final List<String> PRODUCTS = List.of("mince", "sausage", "cured_meat", "jerky", "lard", "canned_meat", "grown_cut");
    public static final DeferredBlock<MeatHookBlock> HOOK = BLOCKS.registerBlock("meat_hook", MeatHookBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.CHAIN).noOcclusion());
    public static final DeferredItem<?> HOOK_ITEM = ITEMS.registerSimpleBlockItem(HOOK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MeatHookEntity>> HOOK_ENTITY = BLOCK_ENTITIES.register("meat_hook",
            () -> new BlockEntityType<>(MeatHookEntity::new, HOOK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StationEntity>> STATION_ENTITY = BLOCK_ENTITIES.register("station",
            () -> new BlockEntityType<>(StationEntity::new, STATION_BLOCKS.stream().filter(h -> !h.getId().getPath().equals("cold_room") && !h.getId().getPath().equals("stasis_case")).map(DeferredBlock::get).collect(java.util.stream.Collectors.toSet()), false));

    /** Own types for the two cold containers so a spoilage mod can recognise them: diaithirbutchery:cold_room and diaithirbutchery:stasis_case (also in block tag diaithirbutchery:cold_containers). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColdStationEntity>> COLD_ROOM_ENTITY = BLOCK_ENTITIES.register("cold_room",
            () -> new BlockEntityType<>((p, s) -> new ColdStationEntity(Butchery.COLD_ROOM_ENTITY.get(), p, s, "cold_room", 400), BLOCKS.getEntries().stream()
                    .filter(h -> h.getId().getPath().equals("cold_room")).map(h -> h.get()).collect(java.util.stream.Collectors.toSet()), false));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColdStationEntity>> STASIS_ENTITY = BLOCK_ENTITIES.register("stasis_case",
            () -> new BlockEntityType<>((p, s) -> new ColdStationEntity(Butchery.STASIS_ENTITY.get(), p, s, "stasis_case", 100000), BLOCKS.getEntries().stream()
                    .filter(h -> h.getId().getPath().equals("stasis_case")).map(h -> h.get()).collect(java.util.stream.Collectors.toSet()), false));

    static {
        STATIONS.put("drying_rack", 2);
        STATIONS.put("salting_barrel", 2);
        STATIONS.put("meat_grinder", 3);
        STATIONS.put("sausage_stuffer", 3);
        STATIONS.put("rendering_cauldron", 3);
        STATIONS.put("smokehouse", 4);
        STATIONS.put("steam_cannery", 5);
        STATIONS.put("cold_room", 6);
        STATIONS.put("vacuum_packer", 7);
        STATIONS.put("vat_grower", 8);
        STATIONS.put("stasis_case", 9);
        STATIONS.keySet().forEach(id -> {
            DeferredBlock<StationBlock> blk = BLOCKS.registerBlock(id, p -> new StationBlock(p, id),
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL).noOcclusion());
            STATION_BLOCKS.add(blk);
            STATION_ITEMS.add(ITEMS.registerSimpleBlockItem(blk));
        });
        PRODUCTS.forEach(id -> ALL_ITEMS.add(ITEMS.registerItem(id, Item::new, () -> new Item.Properties().food(new FoodProperties(4, 0.5F, false)))));
        KNIVES.put("flint_knife", 60);
        KNIVES.put("copper_knife", 150);
        KNIVES.put("bronze_knife", 220);
        KNIVES.put("steel_knife", 500);
        KNIVES.put("tempered_knife", 800);
        KNIVES.put("ceramic_knife", 1500);
        KNIVES.put("diamond_edge_knife", 3000);
        KNIVES.put("crystal_thread_knife", 6000);
        KNIVES.put("shard_knife", 10000);
        KNIVES.forEach((id, uses) -> ALL_ITEMS.add(ITEMS.registerItem(id, Item::new, () -> new Item.Properties().durability(uses))));
        for (String id : DRESSED) CUTS.put(id, id.equals("fish_fillet") || id.equals("wing") ? 3 : 5);
        for (String id : OFFAL) CUTS.put(id, 3);
        CUTS.put("scraps", 2);
        CUTS.forEach((id, nutrition) -> {
            ALL_ITEMS.add(ITEMS.registerItem(id, Item::new, () -> new Item.Properties().food(new FoodProperties(1, 0.1F, false))));
            ALL_ITEMS.add(ITEMS.registerItem("cooked_" + id, Item::new, () -> new Item.Properties().food(new FoodProperties(nutrition + 3, nutrition * 1.2F, false))));
        });
        for (String id : MATERIALS) {
            if (!CUTS.containsKey(id)) ALL_ITEMS.add(ITEMS.registerItem(id, Item::new, () -> new Item.Properties()));
        }
    }

    private Butchery() {
    }

    static void register(IEventBus bus) {
        ITEMS.register(bus);
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        COMPONENTS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
                ALL_ITEMS.forEach(i -> e.accept(i.get()));
            } else if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                e.accept(KNIFE.get());
                e.accept(CLEAVER.get());
                e.accept(HOOK_ITEM.get());
                STATION_ITEMS.forEach(i -> e.accept(i.get()));
            }
        });
    }

    /** Resolves a yield key: a short id is ours, a namespaced one is taken as written. */
    static String qualify(String id) {
        return id.contains(":") ? id : MOD + ":" + id;
    }

    static Block hook() {
        return HOOK.get();
    }
}
