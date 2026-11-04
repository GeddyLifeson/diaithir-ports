package net.diaithir.diaithirfurniture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(FurnitureMod.ID)
public final class FurnitureMod {
    public static final String ID = "diaithirfurniture";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static final List<Supplier<? extends Block>> STORAGE_BLOCKS = new ArrayList<>();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FurnitureBlockEntity>> FURNITURE_BE =
        BLOCK_ENTITIES.register("furniture", () -> new BlockEntityType<>(
            FurnitureBlockEntity::new,
            STORAGE_BLOCKS.stream().map(Supplier::get).collect(java.util.stream.Collectors.toSet())));

    static {
        load();
    }

    private static void load() {
        try (InputStream in = FurnitureMod.class.getResourceAsStream("/data/" + ID + "/furniture.json")) {
            if (in == null) {
                throw new IllegalStateException("missing furniture.json");
            }
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String id = o.get("id").getAsString();
                String shape = o.has("shape") ? o.get("shape").getAsString() : "";
                int rows = o.has("rows") ? o.get("rows").getAsInt() : 0;
                List<double[]> boxes = new ArrayList<>();
                if (o.has("boxes")) {
                    for (JsonElement be : o.getAsJsonArray("boxes")) {
                        JsonArray b = be.getAsJsonArray();
                        double[] d = new double[6];
                        for (int i = 0; i < 6; i++) {
                            d[i] = b.get(i).getAsDouble();
                        }
                        boxes.add(d);
                    }
                }
                if (boxes.isEmpty()) {
                    boxes.add(new double[] {0, 0, 0, 16, 16, 16});
                }
                boolean storage = rows == 3 || rows == 6;
                int storageRows = storage ? rows : 0;
                var block = BLOCKS.registerBlock(id,
                    p -> new FurnitureBlock(p, boxes, storageRows),
                    () -> BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion());
                ITEMS.registerSimpleBlockItem(block);
                if (storage) {
                    STORAGE_BLOCKS.add(block);
                }
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public FurnitureMod(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
