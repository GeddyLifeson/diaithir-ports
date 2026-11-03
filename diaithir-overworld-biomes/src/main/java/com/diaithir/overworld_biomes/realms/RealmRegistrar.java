package com.diaithir.overworld_biomes.realms;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the blocks and items of one realm from its manifest
 * (data/diaithir_overworld_biomes/realms/&lt;modid&gt;.json, written by tools/realms_gen.py).
 * Each realm keeps the modid and ids of the mod it replaces, so old worlds and recipes resolve.
 */
public final class RealmRegistrar {
    private RealmRegistrar() {}

    public static void register(String modId, IEventBus bus) {
        DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(modId);
        DeferredRegister.Items items = DeferredRegister.createItems(modId);
        Map<String, DeferredBlock<Block>> made = new HashMap<>();
        JsonObject m = load(modId);
        for (JsonElement e : m.getAsJsonArray("blocks")) {
            JsonObject b = e.getAsJsonObject();
            String id = b.get("id").getAsString();
            String kind = b.get("kind").getAsString();
            String mat = b.get("mat").getAsString();
            DeferredBlock<Block> base = b.has("base") ? made.get(b.get("base").getAsString()) : null;
            Function<BlockBehaviour.Properties, Block> f = kind.equals("portal")
                    ? props -> new RealmPortalBlock(props, b.get("target").getAsString(), b.get("back").getAsString(), b.get("pad").getAsString())
                    : props -> make(kind, mat, base, props);
            DeferredBlock<Block> db = blocks.registerBlock(id, f, () -> props(mat, kind));
            made.put(id, db);
            items.registerSimpleBlockItem(id, db);
        }
        if (m.has("portals")) for (JsonElement e : m.getAsJsonArray("portals")) {
            JsonObject r = e.getAsJsonObject();
            RealmPortals.add(modId, r.get("activator").getAsString(), r.get("frame").getAsString(), r.get("block").getAsString());
        }
        for (JsonElement e : m.getAsJsonArray("items")) items.registerSimpleItem(e.getAsString());
        blocks.register(bus);
        items.register(bus);
    }

    private static BlockBehaviour.Properties props(String mat, String kind) {
        BlockBehaviour.Properties p = switch (mat) {
            case "wood" -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS);
            case "log" -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG);
            case "leaves" -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES);
            case "soil" -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT);
            case "turf" -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK);
            case "plant" -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS);
            case "portal" -> BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL);
            case "glass" -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS);
            case "metal" -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK);
            case "ore" -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE);
            case "mushroom" -> BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM_BLOCK);
            default -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
        };
        if (kind.equals("door") || kind.equals("trapdoor")) p = p.noOcclusion();
        return p;
    }

    private static Block make(String kind, String mat, DeferredBlock<Block> base, BlockBehaviour.Properties p) {
        boolean wood = mat.equals("wood") || mat.equals("log");
        BlockSetType set = wood ? BlockSetType.OAK : BlockSetType.STONE;
        return switch (kind) {
            case "stairs" -> new StairBlock(base.get().defaultBlockState(), p);
            case "slab" -> new SlabBlock(p);
            case "wall" -> new WallBlock(p);
            case "fence" -> new FenceBlock(p);
            case "gate" -> new FenceGateBlock(WoodType.OAK, p);
            case "button" -> new ButtonBlock(set, wood ? 30 : 20, p);
            case "plate" -> new PressurePlateBlock(set, p);
            case "door" -> new DoorBlock(set, p);
            case "trapdoor" -> new TrapDoorBlock(set, p);
            case "pillar" -> new RotatedPillarBlock(p);
            case "leaves" -> new Block(p.noOcclusion());
            case "cross" -> new Block(p.noCollision().noOcclusion().instabreak());
            case "pane" -> new IronBarsBlock(p);
            default -> new Block(p);
        };
    }

    private static JsonObject load(String modId) {
        String path = "/data/diaithir_overworld_biomes/realms/" + modId + ".json";
        try (Reader r = new InputStreamReader(RealmRegistrar.class.getResourceAsStream(path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(r).getAsJsonObject();
        } catch (Exception ex) {
            throw new IllegalStateException("missing realm manifest " + path, ex);
        }
    }
}
