package net.diaithir.diaithirfarmkitchen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(FarmkitchenMod.ID)
public final class FarmkitchenMod {
    public static final String ID = "diaithirfarmkitchen";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister<BlockEntityType<?>> BE_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static final Set<String> STORAGE_IDS = Set.of("fridge_iron", "fridge_white", "fridge_freezer", "shipping_bin_oak",
            "shipping_bin_iron", "pantry_cabinet_iron", "crate_oak", "shelf_oak");
    private static final java.util.List<DeferredHolder<Block, ? extends Block>> STORAGE_BLOCKS = new java.util.ArrayList<>();

    public static final Supplier<BlockEntityType<StorageBlockEntity>> STORAGE_BE = BE_TYPES.register("storage",
            () -> new BlockEntityType<>(StorageBlockEntity::new, Set.copyOf(STORAGE_BLOCKS.stream().map(h -> (Block) h.get()).toList())));

    static {
        try (var in = new InputStreamReader(FarmkitchenMod.class.getResourceAsStream("/data/" + ID + "/table.json"))) {
            JsonArray rows = JsonParser.parseReader(in).getAsJsonArray();
            for (JsonElement e : rows) {
                var o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                String kind = o.get("kind").getAsString();
                int light = o.get("light").getAsInt();
                if (kind.equals("item")) {
                    ITEMS.registerSimpleItem(id);
                    continue;
                }
                boolean storage = STORAGE_IDS.contains(id);
                boolean fridge = id.startsWith("fridge_");
                int sprinklerRadius = switch (id) {
                    case "sprinkler_basic" -> 2;
                    case "sprinkler_copper" -> 3;
                    case "sprinkler_wide" -> 4;
                    default -> 0;
                };
                boolean oven = id.equals("oven_stone") || id.equals("oven_brick") || id.equals("oven_iron");
                Function<BlockBehaviour.Properties, Block> make;
                if (storage) {
                    make = p -> new StorageBlock(p, fridge);
                } else if (sprinklerRadius > 0) {
                    final int r = sprinklerRadius;
                    make = p -> new SprinklerBlock(p, r);
                } else if (oven) {
                    make = OvenBlock::new;
                } else {
                    make = switch (kind) {
                        case "slab" -> SlabBlock::new;
                        case "stairs" -> p -> new StairBlock(Blocks.STONE.defaultBlockState(), p);
                        case "wall" -> WallBlock::new;
                        case "fence" -> FenceBlock::new;
                        case "pane" -> IronBarsBlock::new;
                        default -> Block::new;
                    };
                }
                var b = BLOCKS.registerBlock(id, make, () -> {
                    var p = BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).lightLevel(s -> light);
                    return kind.equals("pane") ? p.noOcclusion() : p;
                });
                if (storage) STORAGE_BLOCKS.add(b);
                ITEMS.registerSimpleBlockItem(b);
            }
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        registerKitchen();
    }

    private static void food(String name, int nutrition, float saturation) {
        ITEMS.registerSimpleItem(name, () -> new Item.Properties()
                .food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build()));
    }

    private static void registerKitchen() {
        food("ration", 4, 0.8f);
        food("hearty_stew", 10, 0.9f);
        food("offering_feast", 16, 1.2f);
        food("travelers_bread", 6, 0.7f);
        food("smoked_fish_plate", 8, 0.9f);
        var counter = BLOCKS.registerBlock("kitchen_counter", KitchenCounterBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
        ITEMS.registerSimpleBlockItem(counter);
        var pantry = BLOCKS.registerBlock("pantry_shelf", Block::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
        ITEMS.registerSimpleBlockItem(pantry);
        var spice = BLOCKS.registerBlock("spice_rack", Block::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
        ITEMS.registerSimpleBlockItem(spice);
    }

    private static void registerCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, STORAGE_BE.get(),
                (be, side) -> net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(be));
    }

    public FarmkitchenMod(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BE_TYPES.register(bus);
        bus.addListener(FarmkitchenMod::registerCaps);
    }
}
