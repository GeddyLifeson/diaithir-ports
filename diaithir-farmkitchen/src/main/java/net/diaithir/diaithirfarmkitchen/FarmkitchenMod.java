package net.diaithir.diaithirfarmkitchen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.function.Function;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(FarmkitchenMod.ID)
public final class FarmkitchenMod {
    public static final String ID = "diaithirfarmkitchen";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

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
                Function<BlockBehaviour.Properties, Block> make = switch (kind) {
                    case "slab" -> SlabBlock::new;
                    case "stairs" -> p -> new StairBlock(Blocks.STONE.defaultBlockState(), p);
                    case "wall" -> WallBlock::new;
                    case "fence" -> FenceBlock::new;
                    case "pane" -> IronBarsBlock::new;
                    default -> Block::new;
                };
                var b = BLOCKS.registerBlock(id, make, () -> {
                    var p = BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).lightLevel(s -> light);
                    return kind.equals("pane") ? p.noOcclusion() : p;
                });
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

    public FarmkitchenMod(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
