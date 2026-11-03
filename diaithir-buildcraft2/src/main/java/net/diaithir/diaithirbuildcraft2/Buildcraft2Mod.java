package net.diaithir.diaithirbuildcraft2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.function.Function;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Buildcraft2Mod.ID)
public final class Buildcraft2Mod {
    public static final String ID = "diaithirbuildcraft2";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    static {
        try (var in = new InputStreamReader(Buildcraft2Mod.class.getResourceAsStream("/data/" + ID + "/table.json"))) {
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
    }

    public Buildcraft2Mod(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
