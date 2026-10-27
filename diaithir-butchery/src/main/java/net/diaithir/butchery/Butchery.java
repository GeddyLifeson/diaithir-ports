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

    /** Raw cuts: id to nutrition. Each has a cooked twin "cooked_<id>". */
    static final Map<String, Integer> CUTS = new LinkedHashMap<>();
    static final List<String> OFFAL = List.of("liver", "heart", "kidney", "tripe", "tongue", "brain");
    static final List<String> MATERIALS = List.of("raw_hide", "sinew", "tallow", "blood_flask", "marrow_bone", "scraps");
    static final List<String> DRESSED = List.of("fish_fillet", "bird_breast", "drumstick", "wing", "haunch", "shoulder", "loin", "ribs", "flank", "tenderloin", "neck", "tail", "hock");

    public static final List<DeferredItem<Item>> ALL_ITEMS = new ArrayList<>();
    public static final DeferredItem<Item> CARCASS = ITEMS.registerItem("carcass", Item::new, () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> KNIFE = ITEMS.registerItem("butcher_knife", Item::new, () -> new Item.Properties().durability(250));
    public static final DeferredItem<Item> CLEAVER = ITEMS.registerItem("butcher_cleaver", Item::new, () -> new Item.Properties().durability(400));
    public static final DeferredBlock<MeatHookBlock> HOOK = BLOCKS.registerBlock("meat_hook", MeatHookBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.CHAIN).noOcclusion());
    public static final DeferredItem<?> HOOK_ITEM = ITEMS.registerSimpleBlockItem(HOOK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MeatHookEntity>> HOOK_ENTITY = BLOCK_ENTITIES.register("meat_hook",
            () -> new BlockEntityType<>(MeatHookEntity::new, HOOK.get()));

    static {
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
