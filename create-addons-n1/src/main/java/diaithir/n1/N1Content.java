package diaithir.n1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the plain items and blocks of the Create addons ported in slice N1 (data-driven from n1_content.json). */
@Mod("diaithir_create_addons_n1")
public class N1Content {
    public N1Content(IEventBus bus) throws Exception {
        JsonObject all;
        try (var in = new InputStreamReader(N1Content.class.getResourceAsStream("/n1_content.json"))) {
            all = JsonParser.parseReader(in).getAsJsonObject();
        }
        for (var e : all.entrySet()) {
            String ns = e.getKey();
            JsonObject c = e.getValue().getAsJsonObject();
            DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(ns);
            DeferredRegister.Items items = DeferredRegister.createItems(ns);
            for (var b : (JsonArray) c.get("blocks")) {
                String id = b.getAsString();
                blocks.registerSimpleBlock(id, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 6.0F).noOcclusion());
                items.registerSimpleBlockItem(id, blocks.getEntries().stream().filter(h -> h.getId().getPath().equals(id)).findFirst().get());
            }
            for (var i : (JsonArray) c.get("items")) items.registerSimpleItem(i.getAsString(), () -> new Item.Properties());
            blocks.register(bus);
            items.register(bus);
        }
    }
}
