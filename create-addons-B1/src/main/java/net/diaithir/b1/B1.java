package net.diaithir.b1;

import com.google.gson.*;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperItem;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyItem;
import java.io.InputStreamReader;
import java.util.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the plain items and blocks of several small Create addons from b1_manifest.json (registry namespaces kept). */
@Mod("diaithir_create_addons_b1")
public class B1 {
    public B1(IEventBus bus) {
        JsonObject m;
        try (var in = B1.class.getResourceAsStream("/b1_manifest.json")) {
            m = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
        } catch (Exception e) { throw new RuntimeException(e); }
        Map<String, DeferredRegister.Blocks> bregs = new HashMap<>();
        Map<String, DeferredRegister.Items> iregs = new HashMap<>();
        Map<String, DeferredBlock<Block>> blocks = new HashMap<>();
        for (JsonElement e : m.getAsJsonArray("blocks")) {
            JsonObject o = e.getAsJsonObject();
            String ns = o.get("ns").getAsString(), id = o.get("id").getAsString(), like = o.get("like").getAsString();
            Block base = like.equals("stone") ? Blocks.STONE : like.equals("iron_ore") ? Blocks.IRON_ORE : Blocks.DEEPSLATE_IRON_ORE;
            blocks.put(ns + ":" + id, bregs.computeIfAbsent(ns, DeferredRegister::createBlocks)
                .registerBlock(id, Block::new, () -> BlockBehaviour.Properties.ofFullCopy(base)));
        }
        for (JsonElement e : m.getAsJsonArray("items")) {
            JsonObject o = e.getAsJsonObject();
            String ns = o.get("ns").getAsString(), id = o.get("id").getAsString(), kind = o.get("kind").getAsString();
            DeferredRegister.Items r = iregs.computeIfAbsent(ns, DeferredRegister::createItems);
            DeferredBlock<Block> b = blocks.get(ns + ":" + id);
            if (b != null) r.registerSimpleBlockItem(id, b);
            else if (kind.equals("sequenced")) r.registerItem(id, SequencedAssemblyItem::new, () -> new Item.Properties().stacksTo(1));
            else if (kind.equals("sandpaper")) r.registerItem(id, SandPaperItem::new, () -> new Item.Properties().durability(8));
            else if (kind.equals("food")) r.registerItem(id, Item::new, () -> new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3f).build()));
            else r.registerSimpleItem(id);
        }
        bregs.values().forEach(r -> r.register(bus));
        iregs.values().forEach(r -> r.register(bus));
    }
}
